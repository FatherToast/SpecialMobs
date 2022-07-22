package fathertoast.specialmobs.common.entity.creeper;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.CreeperSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.SpecialMobData;
import fathertoast.specialmobs.common.entity.ai.IExplodingMob;
import fathertoast.specialmobs.common.util.ExplosionHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SnowballEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SpecialMob
public class _SpecialCreeperEntity extends CreeperEntity implements IExplodingMob, ISpecialMob<_SpecialCreeperEntity> {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<_SpecialCreeperEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x000000 )
                .vanillaTextureBaseOnly( "textures/entity/creeper/creeper.png" )
                .experience( 5 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( MobFamily.Species<?> species ) {
        return new CreeperSpeciesConfig( species, false, false, false );
    }
    
    /** @return This entity's species config. */
    public CreeperSpeciesConfig getConfig() { return (CreeperSpeciesConfig) getSpecies().config; }
    
    @SpecialMob.AttributeSupplier
    public static AttributeModifierMap.MutableAttribute createAttributes() { return CreeperEntity.createAttributes(); }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Creeper",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void addBaseLoot( LootTableBuilder loot ) {
        loot.addLootTable( "main", EntityType.CREEPER.getDefaultLootTable() );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<_SpecialCreeperEntity> getFactory() { return _SpecialCreeperEntity::new; }
    
    
    //--------------- Variant-Specific Breakouts ----------------
    
    /** Called in the MobEntity.class constructor to initialize AI goals. */
    @Override
    protected void registerGoals() {
        super.registerGoals();
        registerVariantGoals();
    }
    
    /** Override to change this entity's AI goals. */
    protected void registerVariantGoals() { }
    
    /** Override to change starting equipment or stats. */
    @SuppressWarnings( "unused" )
    public void finalizeVariantSpawn( IServerWorld world, DifficultyInstance difficulty, @Nullable SpawnReason spawnReason,
                                      @Nullable ILivingEntityData groupData ) { }
    
    /** Called when this entity successfully damages a target to apply on-hit effects. */
    @Override
    public void doEnchantDamageEffects( LivingEntity attacker, Entity target ) {
        onVariantAttack( target );
        super.doEnchantDamageEffects( attacker, target );
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @SuppressWarnings( "unused" ) // Not normally used for creepers
    protected void onVariantAttack( Entity target ) { }
    
    /** Called to perform this creeper's explosion 'attack'. */
    @Override
    protected void explodeCreeper() {
        if( !level.isClientSide ) {
            dead = true;
            makeVariantExplosion( getVariantExplosionPower( explosionRadius ) );
            remove();
            spawnLingeringCloud();
        }
    }
    
    /** Override to change this creeper's explosion power multiplier. */
    protected float getVariantExplosionPower( float radius ) { return radius * (isSupercharged() ? 3.5F : (isPowered() ? 2.0F : 1.0F)); }
    
    /** Override to change this creeper's explosion. */
    protected void makeVariantExplosion( float explosionPower ) {
        ExplosionHelper.explode( this, explosionPower, true, false );
    }
    
    /** Called to create a lingering effect cloud as part of this creeper's explosion 'attack'. */
    @Override
    protected void spawnLingeringCloud() {
        final List<EffectInstance> effects = new ArrayList<>( getActiveEffects() );
        modifyVariantLingeringCloudEffects( effects );
        
        if( !effects.isEmpty() ) {
            final AreaEffectCloudEntity potionCloud = new AreaEffectCloudEntity( level, getX(), getY(), getZ() );
            potionCloud.setRadius( getVariantExplosionPower( explosionRadius - 0.5F ) );
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
    
    /** Override to save data to this entity's NBT data. */
    public void addVariantSaveData( CompoundNBT saveTag ) { }
    
    /** Override to load data from this entity's NBT data. */
    public void readVariantSaveData( CompoundNBT saveTag ) { }
    
    
    //--------------- Family-Specific Implementations ----------------
    
    /** The parameter for special mob render scale. */
    private static final DataParameter<Float> SCALE = EntityDataManager.defineId( _SpecialCreeperEntity.class, DataSerializers.FLOAT );
    
    public _SpecialCreeperEntity( EntityType<? extends _SpecialCreeperEntity> entityType, World world ) {
        super( entityType, world );
        setCannotExplodeWhileWet( !getConfig().CREEPERS.canExplodeWhileWet.get() );
        setExplodesWhileBurning( getConfig().CREEPERS.explodesWhileBurning.get() );
        setExplodesWhenShot( getConfig().CREEPERS.explodesWhenShot.get() );
        
        getSpecialData().initialize();
    }
    
    /** Called from the Entity.class constructor to define data watcher variables. */
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        specialData = new SpecialMobData<>( this, SCALE );
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
    
    /** Called when this entity is struck by lightning. */
    @Override
    public void thunderHit( ServerWorld world, LightningBoltEntity lightningBolt ) {
        charge();
        super.thunderHit( world, lightningBolt );
        
        // Make it less likely for charged "explode while burning" creepers to immediately explode
        if( explodesWhileBurning() ) clearFire();
    }
    
    
    //--------------- Creeper Explosion Property Setters/Getters ----------------
    
    /** The parameter for creeper explosion properties. This is a combination of boolean flags. */
    private static final DataParameter<Byte> EXPLODE_FLAGS = EntityDataManager.defineId( _SpecialCreeperEntity.class, DataSerializers.BYTE );
    
    /** The bit for "is supercharged". */
    private static final byte EXPLODE_FLAG_SUPERCHARGED = 0b0001;
    /** The bit for "cannot explode while wet". */
    private static final byte EXPLODE_FLAG_DEFUSE_IN_WATER = 0b0010;
    /** The bit for "explodes while burning". */
    private static final byte EXPLODE_FLAG_ON_FIRE = 0b0100;
    /** The bit for "explodes when shot". */
    private static final byte EXPLODE_FLAG_WHEN_SHOT = 0b1000;
    
    /** Called to charge this creeper, potentially supercharging it. */
    public void charge() {
        if( !isPowered() ) {
            setPowered( true );
            if( MobFamily.CREEPER.config.CREEPERS.superchargeChance.rollChance( random ) )
                setSupercharged( true );
        }
    }
    
    /** Copy another creeper's charged state. */
    public void copyChargedState( _SpecialCreeperEntity other ) {
        setPowered( other.isPowered() );
        setSupercharged( other.isSupercharged() );
    }
    
    /** Sets this creeper's charged state to the given value. */
    private void setPowered( boolean charged ) { entityData.set( DATA_IS_POWERED, charged ); }
    
    /** @return True if this creeper is super charged. */
    public boolean isSupercharged() { return getExplodeFlag( EXPLODE_FLAG_SUPERCHARGED ); }
    
    /** Sets this creeper's supercharged state to the given value. */
    private void setSupercharged( boolean value ) {
        if( value && !isPowered() ) setPowered( true );
        setExplodeFlag( EXPLODE_FLAG_SUPERCHARGED, value );
    }
    
    /** @return True if this creeper is unable to explode while wet. */
    public boolean cannotExplodeWhileWet() { return getExplodeFlag( EXPLODE_FLAG_DEFUSE_IN_WATER ); }
    
    /** Sets this creeper's capability to explode while wet. */
    private void setCannotExplodeWhileWet( boolean value ) {
        setExplodeFlag( EXPLODE_FLAG_DEFUSE_IN_WATER, value );
        setPathfindingMalus( PathNodeType.WATER, value ? PathNodeType.LAVA.getMalus() : PathNodeType.WATER.getMalus() );
    }
    
    /** @return True if this creeper explodes while burning. */
    public boolean explodesWhileBurning() { return getExplodeFlag( EXPLODE_FLAG_ON_FIRE ); }
    
    /** Sets this creeper's property to explode while burning. */
    private void setExplodesWhileBurning( boolean value ) {
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
    private void setExplodesWhenShot( boolean value ) { setExplodeFlag( EXPLODE_FLAG_WHEN_SHOT, value ); }
    
    /** @return The value for a specific explode flag. */
    private boolean getExplodeFlag( byte flag ) { return (entityData.get( EXPLODE_FLAGS ) & flag) != 0; }
    
    /** Sets the value for a specific explode flag. */
    private void setExplodeFlag( byte flag, boolean value ) {
        final byte allFlags = entityData.get( EXPLODE_FLAGS );
        if( value == ((allFlags & flag) == 0) ) {
            entityData.set( EXPLODE_FLAGS, (byte) (allFlags ^ flag) );
        }
    }
    
    
    //--------------- ISpecialMob Implementation ----------------
    
    private SpecialMobData<_SpecialCreeperEntity> specialData;
    
    /** @return This mob's special data. */
    @Override
    public SpecialMobData<_SpecialCreeperEntity> getSpecialData() { return specialData; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends _SpecialCreeperEntity> getSpecies() { return SPECIES; }
    
    /** @return The experience that should be dropped by this entity. */
    @Override
    public final int getExperience() { return xpReward; }
    
    /** Sets the experience that should be dropped by this entity. */
    @Override
    public final void setExperience( int xp ) { xpReward = xp; }
    
    /** Called on spawn to initialize properties based on the world, difficulty, and the group it spawns with. */
    @Nullable
    @Override
    public final ILivingEntityData finalizeSpawn( IServerWorld world, DifficultyInstance difficulty, SpawnReason spawnReason,
                                                  @Nullable ILivingEntityData groupData, @Nullable CompoundNBT eggTag ) {
        return MobHelper.finalizeSpawn( this, world, difficulty, spawnReason,
                super.finalizeSpawn( world, difficulty, spawnReason, groupData, eggTag ) );
    }
    
    /** Called on spawn to set starting equipment. */
    @Override // Seal method to force spawn equipment changes through ISpecialMob
    protected final void populateDefaultEquipmentSlots( DifficultyInstance difficulty ) { super.populateDefaultEquipmentSlots( difficulty ); }
    
    /** Called on spawn to initialize properties based on the world, difficulty, and the group it spawns with. */
    @Override
    public void finalizeSpecialSpawn( IServerWorld world, DifficultyInstance difficulty, @Nullable SpawnReason spawnReason,
                                      @Nullable ILivingEntityData groupData ) {
        if( world.getLevelData().isThundering() ) {
            final double chargedChance = getConfig().CREEPERS.stormChargeChance.get() < 0.0 ?
                    MobFamily.CREEPER.config.CREEPERS.familyStormChargeChance.get() :
                    getConfig().CREEPERS.stormChargeChance.get();
            
            if( random.nextDouble() < chargedChance ) charge();
        }
        
        finalizeVariantSpawn( world, difficulty, spawnReason, groupData );
    }
    
    
    //--------------- SpecialMobData Hooks ----------------
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        super.aiStep();
        getSpecialData().tick();
    }
    
    //    /** @return The eye height of this entity when standing. */ - Creepers use auto-scaled eye height
    //    @Override
    //    protected float getStandingEyeHeight( Pose pose, EntitySize size ) {
    //        return super.getStandingEyeHeight( pose, size ) * getSpecialData().getBaseScale() * (isBaby() ? 0.53448F : 1.0F);
    //    }
    
    /** @return Whether this entity is immune to fire damage. */
    @Override
    public boolean fireImmune() { return getSpecialData().isImmuneToFire(); }
    
    /** Sets this entity on fire for a specific duration. */
    @Override
    public void setRemainingFireTicks( int ticks ) {
        if( !getSpecialData().isImmuneToBurning() ) super.setRemainingFireTicks( ticks );
    }
    
    /** @return True if this entity can be leashed. */
    @Override
    public boolean canBeLeashed( PlayerEntity player ) { return !isLeashed() && getSpecialData().allowLeashing(); }
    
    /** Sets this entity 'stuck' inside a block, such as a cobweb or sweet berry bush. Mod blocks could use this as a speed boost. */
    @Override
    public void makeStuckInBlock( BlockState block, Vector3d speedMulti ) {
        if( getSpecialData().canBeStuckIn( block ) ) super.makeStuckInBlock( block, speedMulti );
    }
    
    /** @return Called when this mob falls. Calculates and applies fall damage. Returns false if canceled. */
    @Override
    public boolean causeFallDamage( float distance, float damageMultiplier ) {
        return super.causeFallDamage( distance, damageMultiplier * getSpecialData().getFallDamageMultiplier() );
    }
    
    /** @return True if this entity should NOT trigger pressure plates or tripwires. */
    @Override
    public boolean isIgnoringBlockTriggers() { return getSpecialData().ignorePressurePlates(); }
    
    /** @return True if this entity can breathe underwater. */
    @Override
    public boolean canBreatheUnderwater() { return getSpecialData().canBreatheInWater(); }
    
    /** @return True if this entity can be pushed by (flowing) fluids. */
    @Override
    public boolean isPushedByFluid() { return !getSpecialData().ignoreWaterPush(); }
    
    /** @return True if this entity takes damage while wet. */
    @Override
    public boolean isSensitiveToWater() { return getSpecialData().isDamagedByWater(); }
    
    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt( DamageSource source, float amount ) {
        if( isSensitiveToWater() && source.getDirectEntity() instanceof SnowballEntity ) {
            amount = Math.max( 3.0F, amount );
        }
        if( super.hurt( source, amount ) ) {
            // Implement "explodes when shot" property
            if( source.getDirectEntity() != source.getEntity() && explodesWhenShot() ) ignite();
            return true;
        }
        return false;
    }
    
    /** @return True if the effect can be applied to this entity. */
    @Override
    public boolean canBeAffected( EffectInstance effect ) { return getSpecialData().isPotionApplicable( effect ); }
    
    /** Saves data to this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void addAdditionalSaveData( CompoundNBT tag ) {
        super.addAdditionalSaveData( tag );
        
        final CompoundNBT saveTag = SpecialMobData.getSaveLocation( tag );
        
        saveTag.putBoolean( References.TAG_SUPERCHARGED, isSupercharged() );
        
        saveTag.putBoolean( References.TAG_DRY_EXPLODE, cannotExplodeWhileWet() );
        saveTag.putBoolean( References.TAG_WHILE_BURNING_EXPLODE, explodesWhileBurning() );
        saveTag.putBoolean( References.TAG_WHEN_SHOT_EXPLODE, explodesWhenShot() );
        
        getSpecialData().writeToNBT( saveTag );
        addVariantSaveData( saveTag );
    }
    
    /** Loads data from this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void readAdditionalSaveData( CompoundNBT tag ) {
        super.readAdditionalSaveData( tag );
        
        final CompoundNBT saveTag = SpecialMobData.getSaveLocation( tag );
        
        if( saveTag.contains( References.TAG_SUPERCHARGED, References.NBT_TYPE_NUMERICAL ) )
            setSupercharged( saveTag.getBoolean( References.TAG_SUPERCHARGED ) );
        
        if( saveTag.contains( References.TAG_DRY_EXPLODE, References.NBT_TYPE_NUMERICAL ) )
            setCannotExplodeWhileWet( saveTag.getBoolean( References.TAG_DRY_EXPLODE ) );
        if( saveTag.contains( References.TAG_WHILE_BURNING_EXPLODE, References.NBT_TYPE_NUMERICAL ) )
            setExplodesWhileBurning( saveTag.getBoolean( References.TAG_WHILE_BURNING_EXPLODE ) );
        if( saveTag.contains( References.TAG_WHEN_SHOT_EXPLODE, References.NBT_TYPE_NUMERICAL ) )
            setExplodesWhenShot( saveTag.getBoolean( References.TAG_WHEN_SHOT_EXPLODE ) );
        
        getSpecialData().readFromNBT( saveTag );
        readVariantSaveData( saveTag );
    }
}