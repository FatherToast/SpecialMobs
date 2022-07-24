package fathertoast.specialmobs.common.entity;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;

import static fathertoast.specialmobs.common.util.References.*;

public class SpecialMobData<T extends LivingEntity & ISpecialMob<T>> {
    
    /**
     * @param tag The mob's base nbt tag.
     * @return The nbt tag to save special mob data to.
     */
    public static CompoundNBT getSaveLocation( CompoundNBT tag ) {
        if( !tag.contains( TAG_FORGE_DATA, NBT_TYPE_COMPOUND ) ) {
            tag.put( TAG_FORGE_DATA, new CompoundNBT() );
        }
        final CompoundNBT forgeTag = tag.getCompound( TAG_FORGE_DATA );
        
        if( !forgeTag.contains( TAG_SPECIAL_MOB_DATA, NBT_TYPE_COMPOUND ) ) {
            forgeTag.put( TAG_SPECIAL_MOB_DATA, new CompoundNBT() );
        }
        return forgeTag.getCompound( TAG_SPECIAL_MOB_DATA );
    }
    
    
    /** The entity this data is for. */
    private final T theEntity;
    /** Data manager parameter for render scale. */
    private final DataParameter<Float> renderScale;
    
    //    /** The base texture of the entity. */
    //    private ResourceLocation texture;
    //    /** The glowing eyes texture of the entity. */
    //    private ResourceLocation textureEyes;
    //    /** The overlay texture of the entity. */
    //    private ResourceLocation textureOverlay;
    //    /** True if the textures need to be sent to the client. */
    //    private boolean updateTextures;
    
    /** The rate this mob regenerates health (ticks per 1 health). Off if 0 or less. */
    private int healTimeMax;
    /** Counter to the next heal, if healTimeMax is greater than 0. */
    private int healTime;
    
    /** Proportion of fall damage taken. */
    private float fallDamageMultiplier;
    /** Whether the entity is immune to fire damage. */
    private boolean isImmuneToFire;
    /** Whether the entity is immune to being set on fire. */
    private boolean isImmuneToBurning;
    /** Whether the entity can breathe under water. */
    private boolean canBreatheInWater;
    /** Whether the entity can ignore pushing from flowing water. */
    private boolean ignoreWaterPush;
    /** Whether the entity is damaged when wet. */
    private boolean isDamagedByWater;
    
    /** Whether the entity can be leashed. */
    private boolean allowLeashing;
    /** Whether the entity does not trigger pressure plates. */
    private boolean ignorePressurePlates;
    /** Set of blocks that the entity cannot be stuck in. */
    private final HashSet<Block> immuneToStickyBlocks = new HashSet<>();
    /** Set of potions that cannot be applied to the entity. */
    private final HashSet<Effect> immuneToPotions = new HashSet<>();
    
    /** The damage the entity uses for its ranged attacks, when applicable. */
    private float rangedAttackDamage;
    /** The spread (inaccuracy) of the entity's ranged attacks. */
    private float rangedAttackSpread;
    /** The movement speed multiplier the entity uses during its ranged attack ai. Requires an AI reload to take effect. */
    private float rangedWalkSpeed;
    /** The delay (in ticks) before a ranged attack can be used. Requires an AI reload to take effect. */
    private int rangedAttackCooldown;
    /**
     * The delay (in ticks) between each ranged attack at maximum delay. Requires an AI reload to take effect.
     * Unused for bow attacks. For fireball attacks, this is "refire" time.
     * For spit attacks, this is the cooldown at maximum range (scaled down to the minimum cooldown at point-blank).
     */
    private int rangedAttackMaxCooldown;
    /**
     * The maximum distance (in blocks) the entity can fire ranged attacks from. Requires an ai reload to take effect.
     * Ranged ai can only be used if this stat is greater than 0. Does not change aggro range.
     */
    private float rangedAttackMaxRange;
    
    /**
     * Constructs a SpecialMobData to store generic data about a mob.
     * <p>
     * This constructor should be called during data watcher definitions, and defining the 'render scale' data watcher
     * parameter and setting up AI stats are the only things actually done while constructing.
     * <p>
     * The #initialize() method must be called later on to complete initialization (in the entity constructor).
     *
     * @param entity The entity to store data for.
     * @param scale  Data parameter for storing the render scale.
     */
    public SpecialMobData( T entity, DataParameter<Float> scale ) {
        theEntity = entity;
        renderScale = scale;
        entity.getEntityData().define( renderScale, nextScale() );
        
        final SpeciesConfig.General config = theEntity.getSpecies().config.GENERAL;
        setRangedAttackDamage( config.rangedAttackDamage == null ? -1.0F : (float) config.rangedAttackDamage.get() );
        setRangedAttackSpread( config.rangedAttackSpread == null ? -1.0F : (float) config.rangedAttackSpread.get() );
        setRangedWalkSpeed( config.rangedWalkSpeed == null ? -1.0F : (float) config.rangedWalkSpeed.get() );
        setRangedAttackCooldown( config.rangedAttackCooldown == null ? -1 : config.rangedAttackCooldown.get() );
        setRangedAttackMaxCooldown( config.rangedAttackMaxCooldown == null ? -1 : config.rangedAttackMaxCooldown.get() );
        setRangedAttackMaxRange( config.rangedAttackMaxRange == null ? -1.0F : (float) config.rangedAttackMaxRange.get() );
    }
    
    public void initialize() {
        //        final BestiaryInfo info = theEntity.getSpecies().bestiaryInfo;
        //        texture = info.texture;
        //        textureEyes = info.eyesTexture;
        //        textureOverlay = info.overlayTexture;
        
        final SpeciesConfig.General config = theEntity.getSpecies().config.GENERAL;
        theEntity.setExperience( config.experience.get() );
        setRegenerationTime( config.healTime.get() );
        setFallDamageMultiplier( (float) config.fallDamageMultiplier.get() );
        setImmuneToFire( config.isImmuneToFire.get() );
        setImmuneToBurning( config.isImmuneToBurning.get() );
        setCanBreatheInWater( config.canBreatheInWater.get() );
        setIgnoreWaterPush( config.ignoreWaterPush.get() );
        setDamagedByWater( config.isDamagedByWater.get() );
        setAllowLeashing( config.allowLeashing.get() );
        setIgnorePressurePlates( config.ignorePressurePlates.get() );
        addStickyBlockImmunity( config.immuneToStickyBlocks.get().getEntries() );
        addPotionImmunity( config.immuneToPotions.get().getEntries() );
    }
    
    //    /** Copies all of the data from another mob, optionally copying texture(s). */
    //    public void copyDataFrom( LivingEntity entity, boolean copyTextures ) {
    //        if( entity instanceof ISpecialMob ) {
    //            CompoundNBT tag = new CompoundNBT();
    //
    //            ((ISpecialMob<?>) entity).getSpecialData().writeToNBT( tag );
    //            if( !copyTextures ) {
    //                tag.remove( TAG_TEXTURE );
    //                tag.remove( TAG_TEXTURE_EYES );
    //                tag.remove( TAG_TEXTURE_OVER );
    //            }
    //            readFromNBT( tag );
    //        }
    //    }
    
    /** Called each tick for every living special mob. */
    public void tick() {
        if( !theEntity.level.isClientSide && theEntity.isAlive() ) {
            // Send texture to client
            //            if( updateTextures && theEntity.tickCount > 1 ) {
            //                updateTextures = false;
            //                SpecialMobs.network().sendToDimension( new MessageTexture( theEntity ), theEntity.dimension ); TODO
            //            }
            
            // Update natural regen
            if( healTimeMax > 0 && ++healTime >= healTimeMax ) {
                healTime = 0;
                theEntity.heal( 1.0F );
            }
        }
    }
    
    /** @return The base texture for the entity. */
    public ResourceLocation getTexture() { return theEntity.getSpecies().bestiaryInfo.texture; }
    
    /** @return The glowing eyes texture for the entity. */
    @Nullable
    public ResourceLocation getTextureEyes() { return theEntity.getSpecies().bestiaryInfo.eyesTexture; }
    
    /** @return The overlay texture for the entity. */
    @Nullable
    public ResourceLocation getTextureOverlay() { return theEntity.getSpecies().bestiaryInfo.overlayTexture; }
    
    //    /** @param textures The new texture(s) to set for the entity. */
    //    private void setTextures( ResourceLocation[] textures ) {
    //        texture = textures[0];
    //        textureEyes = textures.length > 1 ? textures[1] : null;
    //        textureOverlay = textures.length > 2 ? textures[2] : null;
    //    }
    
    //    /** @param textures The new texture(s) to load for the entity. Called when loaded from a packet. */
    //    public void loadTextures( String[] textures ) {
    //        try {
    //            loadTexture( textures[0] );
    //            loadTextureEyes( textures.length > 1 ? textures[1] : "" );
    //            loadTextureOverlay( textures.length > 2 ? textures[2] : "" );
    //        }
    //        catch( Exception ex ) {
    //            SpecialMobs.LOG.warn( "Failed to load textures for {}! ({})", theEntity, textures );
    //            ex.printStackTrace();
    //        }
    //    }
    
    //    private void loadTexture( String tex ) {
    //        if( tex.isEmpty() ) throw new IllegalArgumentException( "Entity must have a base texture" );
    //        final ResourceLocation newTexture = new ResourceLocation( tex );
    //        if( !newTexture.toString().equals( texture.toString() ) ) {
    //            texture = newTexture;
    //            updateTextures = true;
    //        }
    //    }
    
    //    private void loadTextureEyes( String tex ) {
    //        if( tex.isEmpty() ) {
    //            if( textureEyes != null ) {
    //                textureEyes = null;
    //                updateTextures = true;
    //            }
    //        }
    //        else if( textureEyes == null ) {
    //            textureEyes = new ResourceLocation( tex );
    //            updateTextures = true;
    //        }
    //        else {
    //            final ResourceLocation newTexture = new ResourceLocation( tex );
    //            if( !newTexture.toString().equals( textureEyes.toString() ) ) {
    //                texture = newTexture;
    //                updateTextures = true;
    //            }
    //        }
    //    }
    
    //    private void loadTextureOverlay( String tex ) {
    //        if( tex.isEmpty() ) {
    //            if( textureOverlay != null ) {
    //                textureOverlay = null;
    //                updateTextures = true;
    //            }
    //        }
    //        else if( textureOverlay == null ) {
    //            textureOverlay = new ResourceLocation( tex );
    //            updateTextures = true;
    //        }
    //        else {
    //            final ResourceLocation newTexture = new ResourceLocation( tex );
    //            if( !newTexture.toString().equals( textureOverlay.toString() ) ) {
    //                texture = newTexture;
    //                updateTextures = true;
    //            }
    //        }
    //    }
    
    /** @return The render scale for the entity, including any applied random scaling. */
    public float getRenderScale() { return theEntity.getEntityData().get( renderScale ); }
    
    /** Sets the overall render scale for the entity. */
    public void setRenderScale( float scale ) {
        if( !theEntity.level.isClientSide ) theEntity.getEntityData().set( renderScale, scale );
    }
    
    //** @return The base render scale for the entity's mob family. */
    //public float getFamilyBaseScale() { return familyScale; }
    
    //** @return The render scale for the entity without its family scale factored in; used to correct scaling for pre-scaled vanilla values. */
    //public float getBaseScaleForPreScaledValues() { return getBaseScale() / getFamilyBaseScale(); }
    
    /** @return The base render scale for the entity, which is a property of the mob species. */
    public float getBaseScale() { return theEntity.getSpecies().bestiaryInfo.baseScale; }
    
    /** @return A random render scale based on config settings. */
    private float nextScale() {
        // Don't do random on client side stuff
        if( theEntity.level == null || theEntity.level.isClientSide() ) return getBaseScale();
        
        // Prioritize most specific value available
        final MobFamily.Species<? extends T> species = theEntity.getSpecies();
        final double randomScaling;
        if( species.config.GENERAL.randomScaling.get() >= 0.0 )
            randomScaling = species.config.GENERAL.randomScaling.get();
        else if( species.family.config.GENERAL.familyRandomScaling.get() >= 0.0 )
            randomScaling = species.family.config.GENERAL.familyRandomScaling.get();
        else
            randomScaling = Config.MAIN.GENERAL.masterRandomScaling.get();
        
        return randomScaling <= 0.0 ? getBaseScale() :
                getBaseScale() * (1.0F + (theEntity.getRandom().nextFloat() - 0.5F) * 2.0F * (float) randomScaling);
    }
    
    private void setRegenerationTime( int ticks ) { healTimeMax = ticks; }
    
    public float getFallDamageMultiplier() { return fallDamageMultiplier; }
    
    private void setFallDamageMultiplier( float value ) { fallDamageMultiplier = value; }
    
    public boolean isImmuneToFire() { return isImmuneToFire; }
    
    private void setImmuneToFire( boolean value ) {
        isImmuneToFire = value;
        if( value ) {
            theEntity.setPathfindingMalus( PathNodeType.LAVA, PathNodeType.WATER.getMalus() );
            theEntity.setPathfindingMalus( PathNodeType.DANGER_FIRE, PathNodeType.OPEN.getMalus() );
            theEntity.setPathfindingMalus( PathNodeType.DAMAGE_FIRE, PathNodeType.OPEN.getMalus() );
        }
        else {
            theEntity.setPathfindingMalus( PathNodeType.LAVA, PathNodeType.LAVA.getMalus() );
            theEntity.setPathfindingMalus( PathNodeType.DANGER_FIRE, PathNodeType.DANGER_FIRE.getMalus() );
            theEntity.setPathfindingMalus( PathNodeType.DAMAGE_FIRE, PathNodeType.DAMAGE_FIRE.getMalus() );
        }
    }
    
    public boolean isImmuneToBurning() { return isImmuneToBurning; }
    
    private void setImmuneToBurning( boolean value ) {
        theEntity.clearFire();
        isImmuneToBurning = value;
        if( value ) {
            theEntity.setPathfindingMalus( PathNodeType.DANGER_FIRE, PathNodeType.OPEN.getMalus() );
            theEntity.setPathfindingMalus( PathNodeType.DAMAGE_FIRE, PathNodeType.DANGER_FIRE.getMalus() );
        }
        else {
            theEntity.setPathfindingMalus( PathNodeType.DANGER_FIRE, PathNodeType.DANGER_FIRE.getMalus() );
            theEntity.setPathfindingMalus( PathNodeType.DAMAGE_FIRE, PathNodeType.DAMAGE_FIRE.getMalus() );
        }
    }
    
    public boolean allowLeashing() { return allowLeashing; }
    
    private void setAllowLeashing( boolean value ) { allowLeashing = value; }
    
    public boolean ignorePressurePlates() { return ignorePressurePlates; }
    
    private void setIgnorePressurePlates( boolean value ) { ignorePressurePlates = value; }
    
    public boolean canBreatheInWater() { return canBreatheInWater; }
    
    private void setCanBreatheInWater( boolean value ) { canBreatheInWater = value; }
    
    public boolean ignoreWaterPush() { return ignoreWaterPush; }
    
    private void setIgnoreWaterPush( boolean value ) { ignoreWaterPush = value; }
    
    public boolean isDamagedByWater() { return isDamagedByWater; }
    
    private void setDamagedByWater( boolean value ) {
        isDamagedByWater = value;
        theEntity.setPathfindingMalus( PathNodeType.WATER, value ? PathNodeType.LAVA.getMalus() : PathNodeType.WATER.getMalus() );
    }
    
    /**
     * Tests a block state to see if the entity can be 'stuck' inside.
     *
     * @param block The block state to test.
     * @return True if the block is allowed to apply its stuck speed multiplier.
     */
    public boolean canBeStuckIn( BlockState block ) { return !immuneToStickyBlocks.contains( block.getBlock() ); }
    
    /** @param blocks The sticky block(s) to grant immunity from. */
    private void addStickyBlockImmunity( Collection<Block> blocks ) { immuneToStickyBlocks.addAll( blocks ); }
    
    /**
     * Tests a potion effect to see if it is applicable to the entity.
     *
     * @param effect The potion effect to test.
     * @return True if the potion is allowed to be applied.
     */
    public boolean isPotionApplicable( EffectInstance effect ) {
        final PotionEvent.PotionApplicableEvent event = new PotionEvent.PotionApplicableEvent( theEntity, effect );
        MinecraftForge.EVENT_BUS.post( event );
        switch( event.getResult() ) {
            case DENY: return false;
            case ALLOW: return true;
            default: return !immuneToPotions.contains( effect.getEffect() );
        }
    }
    
    /** @param effects The effect(s) to grant immunity from. */
    private void addPotionImmunity( Collection<Effect> effects ) { immuneToPotions.addAll( effects ); }
    
    public float getRangedAttackDamage() { return rangedAttackDamage; }
    
    public void setRangedAttackDamage( float value ) { rangedAttackDamage = value; }
    
    public float getRangedAttackSpread() { return rangedAttackSpread; }
    
    public void setRangedAttackSpread( float value ) { rangedAttackSpread = value; }
    
    public float getRangedWalkSpeed() { return rangedWalkSpeed; }
    
    public void setRangedWalkSpeed( float value ) { rangedWalkSpeed = value; }
    
    public int getRangedAttackCooldown() { return rangedAttackCooldown; }
    
    public void setRangedAttackCooldown( int value ) { rangedAttackCooldown = value; }
    
    public int getRangedAttackMaxCooldown() { return rangedAttackMaxCooldown; }
    
    public void setRangedAttackMaxCooldown( int value ) { rangedAttackMaxCooldown = value; }
    
    public float getRangedAttackMaxRange() { return rangedAttackMaxRange; }
    
    public void setRangedAttackMaxRange( float value ) { rangedAttackMaxRange = value; }
    
    public void disableRangedAttack() { setRangedAttackMaxRange( 0.0F ); }
    
    /**
     * Saves this data to NBT.
     *
     * @param tag The tag to save to.
     */
    public void writeToNBT( CompoundNBT tag ) {
        tag.putFloat( TAG_RENDER_SCALE, getRenderScale() );
        
        //        tag.putString( TAG_TEXTURE, texture.toString() );
        //        tag.putString( TAG_TEXTURE_EYES, textureEyes == null ? "" : textureEyes.toString() );
        //        tag.putString( TAG_TEXTURE_OVER, textureOverlay == null ? "" : textureOverlay.toString() );
        
        // Capabilities
        tag.putInt( TAG_EXPERIENCE, theEntity.getExperience() );
        tag.putByte( TAG_REGENERATION, (byte) healTimeMax );
        tag.putFloat( TAG_FALL_MULTI, getFallDamageMultiplier() );
        tag.putBoolean( TAG_FIRE_IMMUNE, isImmuneToFire() );
        tag.putBoolean( TAG_BURN_IMMUNE, isImmuneToBurning() );
        tag.putBoolean( TAG_LEASHABLE, allowLeashing() );
        tag.putBoolean( TAG_TRAP_IMMUNE, ignorePressurePlates() );
        tag.putBoolean( TAG_DROWN_IMMUNE, canBreatheInWater() );
        tag.putBoolean( TAG_WATER_PUSH_IMMUNE, ignoreWaterPush() );
        tag.putBoolean( TAG_WATER_DAMAGE, isDamagedByWater() );
        
        final ListNBT stickyBlocksTag = new ListNBT();
        for( Block block : immuneToStickyBlocks ) {
            final ResourceLocation regKey = ForgeRegistries.BLOCKS.getKey( block );
            if( regKey != null ) stickyBlocksTag.add( StringNBT.valueOf( SpecialMobs.toString( regKey ) ) );
        }
        tag.put( TAG_STICKY_IMMUNE, stickyBlocksTag );
        
        final ListNBT potionsTag = new ListNBT();
        for( Effect effect : immuneToPotions ) {
            final ResourceLocation regKey = ForgeRegistries.POTIONS.getKey( effect );
            if( regKey != null ) potionsTag.add( StringNBT.valueOf( SpecialMobs.toString( regKey ) ) );
        }
        tag.put( TAG_POTION_IMMUNE, potionsTag );
        
        // Ranged attack stats (optional)
        if( getRangedAttackDamage() >= 0.0F )
            tag.putFloat( TAG_ARROW_DAMAGE, getRangedAttackDamage() );
        if( getRangedAttackSpread() >= 0.0F )
            tag.putFloat( TAG_ARROW_SPREAD, getRangedAttackSpread() );
        if( getRangedWalkSpeed() >= 0.0F )
            tag.putFloat( TAG_ARROW_WALK_SPEED, getRangedWalkSpeed() );
        if( getRangedAttackCooldown() >= 0 )
            tag.putShort( TAG_ARROW_REFIRE_MIN, (short) getRangedAttackCooldown() );
        if( getRangedAttackMaxCooldown() >= 0 )
            tag.putShort( TAG_ARROW_REFIRE_MAX, (short) getRangedAttackMaxCooldown() );
        if( getRangedAttackMaxRange() >= 0.0F )
            tag.putFloat( TAG_ARROW_RANGE, getRangedAttackMaxRange() );
    }
    
    /**
     * Loads this data from NBT.
     *
     * @param tag The tag to load from.
     */
    public void readFromNBT( CompoundNBT tag ) {
        if( tag.contains( TAG_RENDER_SCALE, NBT_TYPE_NUMERICAL ) ) {
            setRenderScale( tag.getFloat( TAG_RENDER_SCALE ) );
        }
        
        //        try {
        //            if( tag.contains( TAG_TEXTURE, NBT_TYPE_STRING ) ) {
        //                loadTexture( tag.getString( TAG_TEXTURE ) );
        //            }
        //            if( tag.contains( TAG_TEXTURE_EYES, NBT_TYPE_STRING ) ) {
        //                loadTextureEyes( tag.getString( TAG_TEXTURE_EYES ) );
        //            }
        //            if( tag.contains( TAG_TEXTURE_OVER, NBT_TYPE_STRING ) ) {
        //                loadTextureOverlay( tag.getString( TAG_TEXTURE_OVER ) );
        //            }
        //        }
        //        catch( Exception ex ) {
        //            SpecialMobs.LOG.warn( "Failed to load textures from NBT! " + theEntity.toString() );
        //        }
        
        // Capabilities
        if( tag.contains( TAG_EXPERIENCE, NBT_TYPE_NUMERICAL ) ) {
            theEntity.setExperience( tag.getInt( TAG_EXPERIENCE ) );
        }
        if( tag.contains( TAG_REGENERATION, NBT_TYPE_NUMERICAL ) ) {
            healTimeMax = tag.getByte( TAG_REGENERATION );
        }
        if( tag.contains( TAG_FALL_MULTI, NBT_TYPE_NUMERICAL ) ) {
            setFallDamageMultiplier( tag.getFloat( TAG_FALL_MULTI ) );
        }
        if( tag.contains( TAG_FIRE_IMMUNE, NBT_TYPE_NUMERICAL ) ) {
            setImmuneToFire( tag.getBoolean( TAG_FIRE_IMMUNE ) );
        }
        if( tag.contains( TAG_BURN_IMMUNE, NBT_TYPE_NUMERICAL ) ) {
            setImmuneToBurning( tag.getBoolean( TAG_BURN_IMMUNE ) );
        }
        if( tag.contains( TAG_DROWN_IMMUNE, NBT_TYPE_NUMERICAL ) ) {
            setCanBreatheInWater( tag.getBoolean( TAG_DROWN_IMMUNE ) );
        }
        if( tag.contains( TAG_WATER_PUSH_IMMUNE, NBT_TYPE_NUMERICAL ) ) {
            setIgnoreWaterPush( tag.getBoolean( TAG_WATER_PUSH_IMMUNE ) );
        }
        if( tag.contains( TAG_WATER_DAMAGE, NBT_TYPE_NUMERICAL ) ) {
            setDamagedByWater( tag.getBoolean( TAG_WATER_DAMAGE ) );
        }
        if( tag.contains( TAG_LEASHABLE, NBT_TYPE_NUMERICAL ) ) {
            setAllowLeashing( tag.getBoolean( TAG_LEASHABLE ) );
        }
        if( tag.contains( TAG_TRAP_IMMUNE, NBT_TYPE_NUMERICAL ) ) {
            setIgnorePressurePlates( tag.getBoolean( TAG_TRAP_IMMUNE ) );
        }
        if( tag.contains( TAG_STICKY_IMMUNE, NBT_TYPE_LIST ) ) {
            final ListNBT stickyBlocksTag = tag.getList( TAG_STICKY_IMMUNE, NBT_TYPE_STRING );
            immuneToStickyBlocks.clear();
            for( int i = 0; i < stickyBlocksTag.size(); i++ ) {
                final Block block = ForgeRegistries.BLOCKS.getValue( new ResourceLocation( stickyBlocksTag.getString( i ) ) );
                if( block != null && !block.is( Blocks.AIR ) )
                    immuneToStickyBlocks.add( block );
            }
        }
        if( tag.contains( TAG_POTION_IMMUNE, NBT_TYPE_LIST ) ) {
            final ListNBT potionsTag = tag.getList( TAG_POTION_IMMUNE, NBT_TYPE_STRING );
            immuneToPotions.clear();
            for( int i = 0; i < potionsTag.size(); i++ ) {
                final Effect effect = ForgeRegistries.POTIONS.getValue( new ResourceLocation( potionsTag.getString( i ) ) );
                if( effect != null )
                    immuneToPotions.add( effect );
            }
        }
        
        // Ranged attack stats
        if( tag.contains( TAG_ARROW_DAMAGE, NBT_TYPE_NUMERICAL ) ) {
            setRangedAttackDamage( tag.getFloat( TAG_ARROW_DAMAGE ) );
        }
        if( tag.contains( TAG_ARROW_SPREAD, NBT_TYPE_NUMERICAL ) ) {
            setRangedAttackSpread( tag.getFloat( TAG_ARROW_SPREAD ) );
        }
        if( tag.contains( TAG_ARROW_WALK_SPEED, NBT_TYPE_NUMERICAL ) ) {
            setRangedWalkSpeed( tag.getFloat( TAG_ARROW_WALK_SPEED ) );
        }
        if( tag.contains( TAG_ARROW_REFIRE_MIN, NBT_TYPE_NUMERICAL ) ) {
            setRangedAttackCooldown( tag.getShort( TAG_ARROW_REFIRE_MIN ) );
        }
        if( tag.contains( TAG_ARROW_REFIRE_MAX, NBT_TYPE_NUMERICAL ) ) {
            setRangedAttackMaxCooldown( tag.getShort( TAG_ARROW_REFIRE_MAX ) );
        }
        if( tag.contains( TAG_ARROW_RANGE, NBT_TYPE_NUMERICAL ) ) {
            setRangedAttackMaxRange( tag.getFloat( TAG_ARROW_RANGE ) );
        }
    }
}