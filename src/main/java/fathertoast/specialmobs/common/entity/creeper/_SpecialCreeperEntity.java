package fathertoast.specialmobs.common.entity.creeper;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.entity.SpecialMobData;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class _SpecialCreeperEntity extends CreeperEntity implements ISpecialMob<_SpecialCreeperEntity> {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0x000000 );
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return CreeperEntity.createAttributes();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Creeper",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void addBaseLoot( LootTableBuilder loot ) {
        loot.addLootTable( "main", EntityType.CREEPER.getDefaultLootTable() );
    }
    
    @SpecialMob.Constructor
    public _SpecialCreeperEntity( EntityType<? extends _SpecialCreeperEntity> entityType, World world ) {
        super( entityType, world );
        specialData.initialize();
    }
    
    
    //--------------- Variant-Specific Breakouts ----------------
    
    /** Called in the MobEntity.class constructor to initialize AI goals. */
    @Override
    protected void registerGoals() {
        super.registerGoals();
        registerVariantGoals();
    }
    
    /** Override to change this entity's AI goals. */
    protected void registerVariantGoals() { }
    
    /** Called to perform this creeper's explosion 'attack'. */
    @Override
    protected void explodeCreeper() {
        if( !level.isClientSide ) {
            final Explosion.Mode explosionMode = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent( level, this ) ?
                    Explosion.Mode.DESTROY : Explosion.Mode.NONE;
            final float explosionPower = getVariantExplosionPower();
            dead = true;
            makeVariantExplosion( explosionPower, explosionMode );
            remove();
            spawnLingeringCloud();
        }
    }
    
    /** Override to change this creeper's explosion power multiplier. */
    protected float getVariantExplosionPower() { return isPowered() ? 2.0F : 1.0F; }
    
    /** Override to change this creeper's explosion. */
    protected void makeVariantExplosion( float explosionPower, Explosion.Mode explosionMode ) {
        level.explode( this, getX(), getY(), getZ(), (float) explosionRadius * explosionPower, explosionMode );
    }
    
    /** Called to create a lingering effect cloud as part of this creeper's explosion 'attack'. */
    @Override
    protected void spawnLingeringCloud() {
        final List<EffectInstance> effects = new ArrayList<>( getActiveEffects() );
        modifyVariantLingeringCloudEffects( effects );
        
        if( !effects.isEmpty() ) {
            final AreaEffectCloudEntity potionCloud = new AreaEffectCloudEntity( level, getX(), getY(), getZ() );
            potionCloud.setRadius( (explosionRadius - 0.5F) * getVariantExplosionPower() );
            potionCloud.setRadiusOnUse( -0.5F );
            potionCloud.setWaitTime( 10 );
            potionCloud.setDuration( potionCloud.getDuration() / 2 );
            potionCloud.setRadiusPerTick( -potionCloud.getRadius() / (float) potionCloud.getDuration() );
            for( EffectInstance effect : effects ) {
                potionCloud.addEffect( new EffectInstance( effect ) );
            }
            modifyVariantLingeringCloud( potionCloud );
            level.addFreshEntity( potionCloud );
        }
    }
    
    /**
     * Override to change effects applied by the lingering cloud left by this creeper's explosion.
     * If this list is empty, the lingering cloud is not created.
     */
    protected void modifyVariantLingeringCloudEffects( List<EffectInstance> potions ) { }
    
    /** Override to change stats of the lingering cloud left by this creeper's explosion. */
    protected void modifyVariantLingeringCloud( AreaEffectCloudEntity potionCloud ) { }
    
    
    //--------------- Family-Specific Implementations ----------------
    
    /** The parameter for special mob render scale. */
    private static final DataParameter<Float> SCALE = EntityDataManager.defineId( _SpecialCreeperEntity.class, DataSerializers.FLOAT );
    
    /** Called from the Entity.class constructor to define data watcher variables. */
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        specialData = new SpecialMobData<>( this, SCALE, 1.0F );
        entityData.define( EXPLODE_FLAGS, (byte) 0 );
    }
    
    /** Called each tick to update this entity. */
    @Override
    public void tick() {
        if( isAlive() ) {
            // Implement "cannot explode while wet" property
            if( isInWaterRainOrBubble() && cannotExplodeWhileWet() ) {
                if( isIgnited() ) entityData.set( DATA_IS_IGNITED, false );
                setSwellDir( -1 );
            }
            // Implement "explodes while burning" property
            else if( isOnFire() && explodesWhileBurning() ) {
                setSwellDir( 1 );
            }
        }
        super.tick();
    }
    
    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt( DamageSource damageSource, float damage ) {
        if( super.hurt( damageSource, damage ) ) {
            // Implement "explodes when shot" property
            if( damageSource.getDirectEntity() != damageSource.getEntity() && explodesWhenShot() ) ignite();
            return true;
        }
        return false;
    }
    
    /** Called when this entity is struck by lightning. */
    @Override
    public void thunderHit( ServerWorld world, LightningBoltEntity lightningBolt ) {
        super.thunderHit( world, lightningBolt );
        
        // Make it less likely for charged "explode while burning" creepers to immediately explode
        if( explodesWhileBurning() ) clearFire();
    }
    
    // Called when this entity is first spawned to initialize it. TODO chance to be charged on spawn
    //    @Override
    //    @Nullable
    //    public IEntityLivingData onInitialSpawn( DifficultyInstance difficulty, @Nullable IEntityLivingData data ) {
    //        data = super.onInitialSpawn( difficulty, data );
    //
    //        if( Entity_SpecialCreeper.POWERED != null && world.isThundering() && rand.nextDouble() < Config.get().CREEPERS.CHARGED_CHANCE ) {
    //            dataManager.set( Entity_SpecialCreeper.POWERED, true );
    //        }
    //
    //        return data;
    //    }
    
    
    //--------------- Creeper Explosion Property Setters/Getters ----------------
    
    /** The parameter for creeper explosion properties. This is a combination of boolean flags. */
    private static final DataParameter<Byte> EXPLODE_FLAGS = EntityDataManager.defineId( _SpecialCreeperEntity.class, DataSerializers.BYTE );
    
    /** The bit for "cannot explode while wet". */
    private static final byte EXPLODE_FLAG_DEFUSE_IN_WATER = 0b0001;
    /** The bit for "explodes while burning". */
    private static final byte EXPLODE_FLAG_ON_FIRE = 0b0010;
    /** The bit for "explodes when shot". */
    private static final byte EXPLODE_FLAG_WHEN_SHOT = 0b0100;
    
    /** @return True if this creeper is unable to explode while wet. */
    public boolean cannotExplodeWhileWet() { return getExplodeFlag( EXPLODE_FLAG_DEFUSE_IN_WATER ); }
    
    /** Sets this creeper's capability to explode while wet. */
    public void setCannotExplodeWhileWet( boolean value ) {
        setExplodeFlag( EXPLODE_FLAG_DEFUSE_IN_WATER, value );
        setPathfindingMalus( PathNodeType.WATER, value ? PathNodeType.LAVA.getMalus() : PathNodeType.WATER.getMalus() );
    }
    
    /** @return True if this creeper explodes while burning. */
    public boolean explodesWhileBurning() { return getExplodeFlag( EXPLODE_FLAG_ON_FIRE ); }
    
    /** Sets this creeper's property to explode while burning. */
    public void setExplodesWhileBurning( boolean value ) {
        setExplodeFlag( EXPLODE_FLAG_ON_FIRE, value );
        if( value ) {
            setPathfindingMalus( PathNodeType.DANGER_FIRE, PathNodeType.DAMAGE_FIRE.getMalus() );
            setPathfindingMalus( PathNodeType.DAMAGE_FIRE, PathNodeType.BLOCKED.getMalus() );
        }
        else {
            setPathfindingMalus( PathNodeType.DANGER_FIRE, PathNodeType.DANGER_FIRE.getMalus() );
            setPathfindingMalus( PathNodeType.DAMAGE_FIRE, PathNodeType.DAMAGE_FIRE.getMalus() );
        }
    }
    
    /** @return True if this creeper explodes when shot. */
    public boolean explodesWhenShot() { return getExplodeFlag( EXPLODE_FLAG_WHEN_SHOT ); }
    
    /** Sets this creeper's property to explode when shot. */
    public void setExplodesWhenShot( boolean value ) { setExplodeFlag( EXPLODE_FLAG_WHEN_SHOT, value ); }
    
    /** @return The value for a specific explode flag. */
    private boolean getExplodeFlag( byte flag ) { return (entityData.get( EXPLODE_FLAGS ) & flag) != 0; }
    
    /** Sets the value for a specific explode flag. */
    private void setExplodeFlag( byte flag, boolean value ) {
        final byte allFlags = entityData.get( EXPLODE_FLAGS );
        if( value == ((allFlags & flag) == 0) ) {
            entityData.set( EXPLODE_FLAGS, (byte) (allFlags ^ flag) );
        }
    }
    
    
    //TODO--------------- ISpecialMob Implementation ----------------
    
    private SpecialMobData<_SpecialCreeperEntity> specialData;
    
    /** @return This mob's special data. */
    @Override
    public SpecialMobData<_SpecialCreeperEntity> getSpecialData() { return specialData; }
    
    //    /**
    //     * Applies changes to this entity's attributes, applied on creation and after copying replacement data.
    //     */
    //    @Override
    //    public final void applyAttributeAdjustments() {
    //        float prevMax = getMaxHealth();
    //        adjustTypeAttributes();
    //        setHealth( getMaxHealth() + getHealth() - prevMax );
    //    }
    
    /** @return The experience that should be dropped by this entity. */
    @Override
    public final int getExperience() { return xpReward; }
    
    /** Sets the experience that should be dropped by this entity. */
    @Override
    public final void setExperience( int xp ) { xpReward = xp; }
    
    static String GET_TEXTURE_PATH( String type ) { return SpecialMobs.TEXTURE_PATH + "creeper/" + type + ".png"; }
    
    private static final ResourceLocation[] TEXTURES = { new ResourceLocation( "textures/entity/creeper/creeper.png" ) };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
    
    
    //TODO--------------- SpecialMobData Hooks ----------------
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        super.aiStep();
        getSpecialData().tick();
    }
    
    //    // Called to attack the target.
    //    @Override
    //    public boolean attackEntityAsMob( Entity target ) {
    //        if( super.attackEntityAsMob( target ) ) {
    //            onTypeAttack( target );
    //            return true;
    //        }
    //        return false;
    //    }
    
    //    @Override
    //    public float getEyeHeight() { return super.getEyeHeight(); } // Uses boundingbox-scaled eye height
    
    /** @return Whether this entity is immune to fire damage. */
    @Override
    public boolean fireImmune() { return specialData.isImmuneToFire(); }
    
    /** Sets this entity on fire for a specific duration. */
    @Override
    public void setRemainingFireTicks( int ticks ) {
        if( !getSpecialData().isImmuneToBurning() ) super.setRemainingFireTicks( ticks );
    }
    
    //    @Override
    //    public boolean canBeLeashedTo( EntityPlayer player ) { return !getLeashed() && getSpecialData().allowLeashing(); }
    
    /** Sets this entity 'stuck' inside a block, such as a cobweb or sweet berry bush. Mod blocks could use this as a speed boost. */
    @Override
    public void makeStuckInBlock( BlockState block, Vector3d speedMulti ) {
        if( specialData.canBeStuckIn( block ) ) super.makeStuckInBlock( block, speedMulti );
    }
    
    //    // Called when this mob falls. Calculates and applies fall damage.
    //    @Override
    //    public void fall( float distance, float damageMultiplier ) { super.fall( distance, damageMultiplier * getSpecialData().getFallDamageMultiplier() ); }
    
    //    // Return whether this entity should NOT trigger a pressure plate or a tripwire.
    //    @Override
    //    public boolean doesEntityNotTriggerPressurePlate() { return getSpecialData().ignorePressurePlates(); }
    
    /** @return True if this entity can breathe underwater. */
    @Override
    public boolean canBreatheUnderwater() { return getSpecialData().canBreatheInWater(); }
    
    /** @return True if this entity can be pushed by (flowing) fluids. */
    @Override
    public boolean isPushedByFluid() { return !getSpecialData().ignoreWaterPush(); }
    
    /** @return True if this entity takes damage while wet. */
    @Override
    public boolean isSensitiveToWater() { return getSpecialData().isDamagedByWater(); }
    
    /** @return True if the effect can be applied to this entity. */
    @Override
    public boolean canBeAffected( EffectInstance effect ) { return getSpecialData().isPotionApplicable( effect ); }
    
    /** Saves data to this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void addAdditionalSaveData( CompoundNBT tag ) {
        super.addAdditionalSaveData( tag );
        
        final CompoundNBT saveTag = SpecialMobData.getSaveLocation( tag );
        
        saveTag.putBoolean( References.TAG_DRY_EXPLODE, cannotExplodeWhileWet() );
        saveTag.putBoolean( References.TAG_WHEN_BURNING_EXPLODE, explodesWhileBurning() );
        saveTag.putBoolean( References.TAG_WHEN_SHOT_EXPLODE, explodesWhenShot() );
        
        getSpecialData().writeToNBT( saveTag );
    }
    
    /** Loads data from this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void readAdditionalSaveData( CompoundNBT tag ) {
        super.readAdditionalSaveData( tag );
        
        final CompoundNBT saveTag = SpecialMobData.getSaveLocation( tag );
        
        if( saveTag.contains( References.TAG_DRY_EXPLODE, References.NBT_TYPE_NUMERICAL ) )
            setCannotExplodeWhileWet( saveTag.getBoolean( References.TAG_DRY_EXPLODE ) );
        if( saveTag.contains( References.TAG_WHEN_BURNING_EXPLODE, References.NBT_TYPE_NUMERICAL ) )
            setExplodesWhileBurning( saveTag.getBoolean( References.TAG_WHEN_BURNING_EXPLODE ) );
        if( saveTag.contains( References.TAG_WHEN_SHOT_EXPLODE, References.NBT_TYPE_NUMERICAL ) )
            setExplodesWhenShot( saveTag.getBoolean( References.TAG_WHEN_SHOT_EXPLODE ) );
        
        getSpecialData().readFromNBT( saveTag );
    }
}