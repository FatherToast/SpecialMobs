package fathertoast.specialmobs.common.entity;

import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.PotionEvent;

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
    
    /** The base collision box scale of this variant's family. */
    private final float familyScale;
    /** The base collision box scale of this variant. */
    private float baseScale;
    
    /** The base texture of the entity. */
    private ResourceLocation texture;
    /** The glowing eyes texture of the entity. */
    private ResourceLocation textureEyes;
    /** The overlay texture of the entity. */
    private ResourceLocation textureOverlay;
    /** True if the textures need to be sent to the client. */
    private boolean updateTextures;
    
    /** The damage the entity uses for its ranged attacks, when applicable. */
    public float rangedAttackDamage;
    /** The spread (inaccuracy) of the entity's ranged attacks. */
    public float rangedAttackSpread;
    /** The movement speed multiplier the entity uses during its ranged attack ai. Requires an ai reload to take effect. */
    public float rangedWalkSpeed = 1.0F;
    /** The delay (in ticks) before a new ranged attack can begin after firing. Requires an ai reload to take effect. */
    public int rangedAttackCooldown;
    /**
     * The delay (in ticks) between each ranged attack at maximum delay. Requires an ai reload to take effect.
     * Unused for bow attacks. For fireball attacks, this is the cooldown + charge time.
     * For all other attacks, this is the cooldown at maximum range (scaled down to the minimum cooldown at point-blank).
     */
    public int rangedAttackMaxCooldown;
    /**
     * The maximum distance (in blocks) the entity can fire ranged attacks from. Requires an ai reload to take effect.
     * Ranged ai can only be used if this stat is greater than 0. Does not change aggro range.
     */
    public float rangedAttackMaxRange;
    
    /** The rate this mob regenerates health (ticks per 1 health). Off if 0 or less. */
    private int healTimeMax;
    /** Counter to the next heal, if healTimeMax is greater than 0. */
    private int healTime;
    
    /** Proportion of fall damage taken. */
    private float fallDamageMultiplier = 1.0F;
    
    /** Whether the entity is immune to fire damage. */
    private boolean isImmuneToFire;
    /** Whether the entity is immune to being set on fire. */
    private boolean isImmuneToBurning;
    /** Whether the entity can be leashed. */
    private boolean allowLeashing;
    /** Whether the entity does not trigger pressure plates. */
    private boolean ignorePressurePlates;
    
    /** Whether the entity can breathe under water. */
    private boolean canBreatheInWater;
    /** Whether the entity can ignore pushing from flowing water. */
    private boolean ignoreWaterPush;
    /** Whether the entity is damaged when wet. */
    private boolean isDamagedByWater;
    
    /** List of blocks that the entity cannot be stuck in. */
    private final HashSet<String> immuneToStickyBlocks = new HashSet<>();
    /** List of potions that cannot be applied to the entity. */
    private final HashSet<String> immuneToPotions = new HashSet<>();
    
    /**
     * Constructs a SpecialMobData to store generic data about a mob.
     *
     * @param entity The entity to store data for.
     * @param scale  Data parameter for storing the render scale.
     */
    public SpecialMobData( T entity, DataParameter<Float> scale ) {
        this( entity, scale, 1.0F );
    }
    
    /**
     * Constructs a SpecialMobData to store generic data about a mob.
     * <p>
     * This constructor should be called during data watcher definitions, and defining the 'render scale' data watcher
     * parameter is the only thing actually done while constructing.
     * <p>
     * The #initialize() method must be called later on to complete initialization (e.g. in the entity constructor).
     *
     * @param entity          The entity to store data for.
     * @param scale           Data parameter for storing the render scale.
     * @param familyBaseScale Base render scale. Typically 1.0F.
     */
    public SpecialMobData( T entity, DataParameter<Float> scale, float familyBaseScale ) {
        theEntity = entity;
        renderScale = scale;
        
        familyScale = baseScale = familyBaseScale;
        
        setTextures( entity.getDefaultTextures() );
        
        entity.getEntityData().define( renderScale, nextScale() );
    }
    
    /** Called to finish initialization, since we can only define data watcher params in the constructor. */
    public void initialize() {
        setImmuneToFire( theEntity.getType().fireImmune() );
        if( theEntity.getMobType() == CreatureAttribute.UNDEAD ) {
            addPotionImmunity( Effects.REGENERATION, Effects.POISON );
        }
        if( theEntity instanceof SpiderEntity ) {
            addStickyBlockImmunity( Blocks.COBWEB );
            addPotionImmunity( Effects.POISON );
        }
    }
    
    /** Copies all of the data from another mob, optionally copying texture(s). */
    public void copyDataFrom( LivingEntity entity, boolean copyTextures ) {
        if( entity instanceof ISpecialMob ) {
            CompoundNBT tag = new CompoundNBT();
            
            ((ISpecialMob<?>) entity).getSpecialData().writeToNBT( tag );
            if( !copyTextures ) {
                tag.remove( TAG_TEXTURE );
                tag.remove( TAG_TEXTURE_EYES );
                tag.remove( TAG_TEXTURE_OVER );
            }
            readFromNBT( tag );
        }
    }
    
    /** Called each tick for every living special mob. */
    public void tick() {
        if( !theEntity.level.isClientSide && theEntity.isAlive() ) {
            // Send texture to client
            if( updateTextures && theEntity.tickCount > 1 ) {
                updateTextures = false;
                //SpecialMobs.network().sendToDimension( new MessageTexture( theEntity ), theEntity.dimension ); TODO
            }
            
            // Update natural regen
            if( healTimeMax > 0 && ++healTime >= healTimeMax ) {
                healTime = 0;
                theEntity.heal( 1.0F );
            }
        }
    }
    
    /**
     * Alters the entity's base attribute by adding an amount to it.
     * Do NOT use this for move speed, instead use {@link SpecialMobData#multAttribute(Attribute, double)}
     *
     * @param attribute the attribute to modify
     * @param amount    the amount to add to the attribute
     */
    public void addAttribute( Attribute attribute, double amount ) {
        final ModifiableAttributeInstance attributeInstance = theEntity.getAttribute( attribute );
        if( attributeInstance == null )
            throw new IllegalStateException( "Special mob '" + theEntity + "' does not have registered attribute " +
                    attribute.getDescriptionId() );
        attributeInstance.setBaseValue( attributeInstance.getBaseValue() + amount );
    }
    
    /**
     * Alters the entity's base attribute by multiplying it by an amount.
     * Only use this for move speed, for other attributes use {@link SpecialMobData#addAttribute(Attribute, double)}
     *
     * @param attribute the attribute to modify
     * @param amount    the amount to multiply the attribute by
     */
    public void multAttribute( Attribute attribute, double amount ) {
        final ModifiableAttributeInstance attributeInstance = theEntity.getAttribute( attribute );
        if( attributeInstance == null )
            throw new IllegalStateException( "Special mob '" + theEntity + "' does not have registered attribute " +
                    attribute.getDescriptionId() );
        attributeInstance.setBaseValue( attributeInstance.getBaseValue() * amount );
    }
    
    /**
     * @return Whether this entity has a glowing eyes texture.
     */
    public boolean hasEyesTexture() { return textureEyes != null; }
    
    /**
     * @return Whether this entity has an overlay texture.
     */
    public boolean hasOverlayTexture() { return textureOverlay != null; }
    
    /**
     * @return The base texture for the entity.
     */
    public ResourceLocation getTexture() { return texture; }
    
    /**
     * @return The glowing eyes texture for the entity.
     */
    public ResourceLocation getTextureEyes() { return textureEyes; }
    
    /**
     * @return The overlay texture for the entity.
     */
    public ResourceLocation getTextureOverlay() { return textureOverlay; }
    
    /**
     * @param textures The new texture(s) to set for the entity.
     */
    private void setTextures( ResourceLocation[] textures ) {
        texture = textures[0];
        if( textures.length > 1 ) {
            textureEyes = textures[1];
        }
        if( textures.length > 2 ) {
            textureOverlay = textures[2];
        }
    }
    
    /**
     * @param textures The new texture(s) to load for the entity. Called when loaded from a packet.
     */
    public void loadTextures( String[] textures ) {
        try {
            loadTexture( textures[0] );
            loadTextureEyes( textures.length > 1 ? textures[1] : "" );
            loadTextureOverlay( textures.length > 2 ? textures[2] : "" );
        }
        catch( Exception ex ) {
            SpecialMobs.LOG.warn( "Failed to load textures for {}! ({})", theEntity, textures );
            ex.printStackTrace();
        }
    }
    
    private void loadTexture( String tex ) {
        if( tex.isEmpty() ) throw new IllegalArgumentException( "Entity must have a base texture" );
        final ResourceLocation newTexture = new ResourceLocation( tex );
        if( !newTexture.toString().equals( texture.toString() ) ) {
            texture = newTexture;
            updateTextures = true;
        }
    }
    
    private void loadTextureEyes( String tex ) {
        if( tex.isEmpty() ) {
            if( textureEyes != null ) {
                textureEyes = null;
                updateTextures = true;
            }
        }
        else if( textureEyes == null ) {
            textureEyes = new ResourceLocation( tex );
            updateTextures = true;
        }
        else {
            final ResourceLocation newTexture = new ResourceLocation( tex );
            if( !newTexture.toString().equals( textureEyes.toString() ) ) {
                texture = newTexture;
                updateTextures = true;
            }
        }
    }
    
    private void loadTextureOverlay( String tex ) {
        if( tex.isEmpty() ) {
            if( textureOverlay != null ) {
                textureOverlay = null;
                updateTextures = true;
            }
        }
        else if( textureOverlay == null ) {
            textureOverlay = new ResourceLocation( tex );
            updateTextures = true;
        }
        else {
            final ResourceLocation newTexture = new ResourceLocation( tex );
            if( !newTexture.toString().equals( textureOverlay.toString() ) ) {
                texture = newTexture;
                updateTextures = true;
            }
        }
    }
    
    /** @return The render scale for the entity. */
    public float getRenderScale() { return theEntity.getEntityData().get( renderScale ); }
    
    public void setRenderScale( float scale ) {
        if( !theEntity.level.isClientSide ) {
            theEntity.getEntityData().set( renderScale, scale );
        }
    }
    
    public float getFamilyBaseScale() { return familyScale; }
    
    public float getBaseScaleForPreScaledValues() { return getBaseScale() / getFamilyBaseScale(); }
    
    public float getBaseScale() { return baseScale; }
    
    public void setBaseScale( float newBaseScale ) {
        baseScale = newBaseScale;
        setRenderScale( nextScale() );
    }
    
    private float nextScale() {
        //        if( Config.get().GENERAL.RANDOM_SCALING > 0.0F ) { TODO configs
        //            return baseScale * (1.0F + (theEntity.getRNG().nextFloat() - 0.5F) * Config.get().GENERAL.RANDOM_SCALING);
        //        }
        return baseScale;
    }
    
    public double getRangedDamage( float distanceFactor ) {
        final int powerEnchant = EnchantmentHelper.getEnchantmentLevel( Enchantments.POWER_ARROWS, theEntity );
        return rangedAttackDamage * (distanceFactor +
                theEntity.getRandom().nextGaussian() * 0.125 +
                theEntity.level.getDifficulty().getId() * 0.055F) +
                (powerEnchant > 0 ? powerEnchant * 0.5 + 0.5 : 0.0);
    }
    
    public void setRegenerationTime( int ticks ) { healTimeMax = ticks; }
    
    public float getFallDamageMultiplier() { return fallDamageMultiplier; }
    
    public void setFallDamageMultiplier( float value ) { fallDamageMultiplier = value; }
    
    public boolean isImmuneToFire() { return isImmuneToFire; }
    
    public void setImmuneToFire( boolean value ) {
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
    
    public void setImmuneToBurning( boolean value ) {
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
    
    public void setAllowLeashing( boolean value ) { allowLeashing = value; }
    
    public boolean ignorePressurePlates() { return ignorePressurePlates; }
    
    public void setIgnorePressurePlates( boolean value ) { ignorePressurePlates = value; }
    
    public boolean canBreatheInWater() { return canBreatheInWater; }
    
    public void setCanBreatheInWater( boolean value ) { canBreatheInWater = value; }
    
    public boolean ignoreWaterPush() { return ignoreWaterPush; }
    
    public void setIgnoreWaterPush( boolean value ) { ignoreWaterPush = value; }
    
    public boolean isDamagedByWater() { return isDamagedByWater; }
    
    public void setDamagedByWater( boolean value ) {
        isDamagedByWater = value;
        theEntity.setPathfindingMalus( PathNodeType.WATER, value ? PathNodeType.LAVA.getMalus() : PathNodeType.WATER.getMalus() );
    }
    
    /**
     * Tests a block state to see if the entity can be 'stuck' inside.
     *
     * @param block The block state to test.
     * @return True if the block is allowed to apply its stuck speed multiplier.
     */
    public boolean canBeStuckIn( BlockState block ) { return !immuneToStickyBlocks.contains( block.getBlock().getDescriptionId() ); }
    
    /** @param blocks The sticky block(s) to grant immunity from. */
    public void addStickyBlockImmunity( Block... blocks ) {
        for( Block block : blocks ) immuneToStickyBlocks.add( block.getDescriptionId() );
    }
    
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
            default: return !immuneToPotions.contains( effect.getDescriptionId() );
        }
    }
    
    /** @param effects The effect(s) to grant immunity from. */
    public void addPotionImmunity( Effect... effects ) {
        for( Effect effect : effects ) immuneToPotions.add( effect.getDescriptionId() );
    }
    
    /**
     * Saves this data to NBT.
     *
     * @param tag The tag to save to.
     */
    public void writeToNBT( CompoundNBT tag ) {
        tag.putFloat( TAG_RENDER_SCALE, getRenderScale() );
        tag.putInt( TAG_EXPERIENCE, theEntity.getExperience() );
        tag.putByte( TAG_REGENERATION, (byte) healTimeMax );
        
        tag.putString( TAG_TEXTURE, texture.toString() );
        tag.putString( TAG_TEXTURE_EYES, textureEyes == null ? "" : textureEyes.toString() );
        tag.putString( TAG_TEXTURE_OVER, textureOverlay == null ? "" : textureOverlay.toString() );
        
        // Arrow AI
        tag.putFloat( TAG_ARROW_DAMAGE, rangedAttackDamage );
        tag.putFloat( TAG_ARROW_SPREAD, rangedAttackSpread );
        tag.putFloat( TAG_ARROW_WALK_SPEED, rangedWalkSpeed );
        tag.putShort( TAG_ARROW_REFIRE_MIN, (short) rangedAttackCooldown );
        tag.putShort( TAG_ARROW_REFIRE_MAX, (short) rangedAttackMaxCooldown );
        tag.putFloat( TAG_ARROW_RANGE, rangedAttackMaxRange );
        
        // Abilities
        tag.putFloat( TAG_FALL_MULTI, getFallDamageMultiplier() );
        tag.putBoolean( TAG_FIRE_IMMUNE, isImmuneToFire() );
        tag.putBoolean( TAG_BURN_IMMUNE, isImmuneToBurning() );
        tag.putBoolean( TAG_LEASHABLE, allowLeashing() );
        tag.putBoolean( TAG_TRAP_IMMUNE, ignorePressurePlates() );
        tag.putBoolean( TAG_DROWN_IMMUNE, canBreatheInWater() );
        tag.putBoolean( TAG_WATER_PUSH_IMMUNE, ignoreWaterPush() );
        tag.putBoolean( TAG_WATER_DAMAGE, isDamagedByWater() );
        
        final ListNBT stickyBlocksTag = new ListNBT();
        for( String blockName : immuneToStickyBlocks ) {
            stickyBlocksTag.add( StringNBT.valueOf( blockName ) );
        }
        tag.put( TAG_STICKY_IMMUNE, stickyBlocksTag );
        
        final ListNBT potionsTag = new ListNBT();
        for( String potionName : immuneToPotions ) {
            potionsTag.add( StringNBT.valueOf( potionName ) );
        }
        tag.put( TAG_POTION_IMMUNE, potionsTag );
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
        if( tag.contains( TAG_EXPERIENCE, NBT_TYPE_NUMERICAL ) ) {
            theEntity.setExperience( tag.getInt( TAG_EXPERIENCE ) );
        }
        if( tag.contains( TAG_REGENERATION, NBT_TYPE_NUMERICAL ) ) {
            healTimeMax = tag.getByte( TAG_REGENERATION );
        }
        
        try {
            if( tag.contains( TAG_TEXTURE, NBT_TYPE_STRING ) ) {
                loadTexture( tag.getString( TAG_TEXTURE ) );
            }
            if( tag.contains( TAG_TEXTURE_EYES, NBT_TYPE_STRING ) ) {
                loadTextureEyes( tag.getString( TAG_TEXTURE_EYES ) );
            }
            if( tag.contains( TAG_TEXTURE_OVER, NBT_TYPE_STRING ) ) {
                loadTextureOverlay( tag.getString( TAG_TEXTURE_OVER ) );
            }
        }
        catch( Exception ex ) {
            SpecialMobs.LOG.warn( "Failed to load textures from NBT! " + theEntity.toString() );
        }
        
        // Arrow AI
        if( tag.contains( TAG_ARROW_DAMAGE, NBT_TYPE_NUMERICAL ) ) {
            rangedAttackDamage = tag.getFloat( TAG_ARROW_DAMAGE );
        }
        if( tag.contains( TAG_ARROW_SPREAD, NBT_TYPE_NUMERICAL ) ) {
            rangedAttackSpread = tag.getFloat( TAG_ARROW_SPREAD );
        }
        if( tag.contains( TAG_ARROW_WALK_SPEED, NBT_TYPE_NUMERICAL ) ) {
            rangedWalkSpeed = tag.getFloat( TAG_ARROW_WALK_SPEED );
        }
        if( tag.contains( TAG_ARROW_REFIRE_MIN, NBT_TYPE_NUMERICAL ) ) {
            rangedAttackCooldown = tag.getShort( TAG_ARROW_REFIRE_MIN );
        }
        if( tag.contains( TAG_ARROW_REFIRE_MAX, NBT_TYPE_NUMERICAL ) ) {
            rangedAttackMaxCooldown = tag.getShort( TAG_ARROW_REFIRE_MAX );
        }
        if( tag.contains( TAG_ARROW_RANGE, NBT_TYPE_NUMERICAL ) ) {
            rangedAttackMaxRange = tag.getFloat( TAG_ARROW_RANGE );
        }
        
        // Abilities
        if( tag.contains( TAG_FALL_MULTI, NBT_TYPE_NUMERICAL ) ) {
            setFallDamageMultiplier( tag.getFloat( TAG_FALL_MULTI ) );
        }
        if( tag.contains( TAG_FIRE_IMMUNE, NBT_TYPE_NUMERICAL ) ) {
            setImmuneToFire( tag.getBoolean( TAG_FIRE_IMMUNE ) );
        }
        if( tag.contains( TAG_BURN_IMMUNE, NBT_TYPE_NUMERICAL ) ) {
            setImmuneToBurning( tag.getBoolean( TAG_BURN_IMMUNE ) );
        }
        if( tag.contains( TAG_LEASHABLE, NBT_TYPE_NUMERICAL ) ) {
            setAllowLeashing( tag.getBoolean( TAG_LEASHABLE ) );
        }
        if( tag.contains( TAG_TRAP_IMMUNE, NBT_TYPE_NUMERICAL ) ) {
            setIgnorePressurePlates( tag.getBoolean( TAG_TRAP_IMMUNE ) );
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
        if( tag.contains( TAG_STICKY_IMMUNE, NBT_TYPE_LIST ) ) {
            final ListNBT stickyBlocksTag = tag.getList( TAG_STICKY_IMMUNE, NBT_TYPE_STRING );
            immuneToStickyBlocks.clear();
            for( int i = 0; i < stickyBlocksTag.size(); i++ ) {
                immuneToStickyBlocks.add( stickyBlocksTag.getString( i ) );
            }
        }
        if( tag.contains( TAG_POTION_IMMUNE, NBT_TYPE_LIST ) ) {
            final ListNBT potionsTag = tag.getList( TAG_POTION_IMMUNE, NBT_TYPE_STRING );
            immuneToPotions.clear();
            for( int i = 0; i < potionsTag.size(); i++ ) {
                immuneToPotions.add( potionsTag.getString( i ) );
            }
        }
    }
}