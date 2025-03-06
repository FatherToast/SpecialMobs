package fathertoast.specialmobs.common.entity.creeper;

import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.CreeperSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.core.register.SMTags;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.SpecialMobData;
import fathertoast.specialmobs.common.entity.ai.IExplodingMob;
import fathertoast.specialmobs.common.event.NaturalSpawnManager;
import fathertoast.specialmobs.common.util.ExplosionHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SpecialMob
public class _SpecialCreeperEntity extends Creeper implements IExplodingMob, ISpecialMob<_SpecialCreeperEntity> {
    
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
    public static SpeciesConfig createConfig( ConfigManager manager, MobFamily.Species<?> species ) {
        return new CreeperSpeciesConfig( manager, species, false, false, false );
    }
    
    /** @return This entity's species config. */
    public CreeperSpeciesConfig getConfig() { return (CreeperSpeciesConfig) getSpecies().config; }
    
    @SpecialMob.AttributeSupplier
    public static AttributeSupplier.Builder createAttributes() { return Creeper.createAttributes(); }
    
    @SpecialMob.SpawnPlacementRegistrar
    public static void registerSpawnPlacement( MobFamily.Species<? extends _SpecialCreeperEntity> species ) {
        NaturalSpawnManager.registerSpawnPlacement( species );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Creeper",
                "Creeper", "Creeper", "Creeper", "Creeper", "Creeper", "Creeper" );
    }
    
    @SpecialMob.LootTableProvider
    public static void addBaseLoot( LootTableBuilder loot ) {
        loot.addLootTable( "main", EntityType.CREEPER.getDefaultLootTable() );
    }

    @SpecialMob.EntityTagProvider
    public static List<TagKey<EntityType<?>>> getEntityTags() {
        return Collections.singletonList( SMTags.EntityTypes.CREEPERS );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<_SpecialCreeperEntity> getFactory() { return _SpecialCreeperEntity::new; }
    
    
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
    public void finalizeVariantSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, @Nullable MobSpawnType spawnType,
                                     @Nullable SpawnGroupData groupData ) { }
    
    /** Called when this entity successfully damages a target to apply on-hit effects. */
    @Override
    public void doEnchantDamageEffects(LivingEntity attacker, Entity target ) {
        if( target instanceof LivingEntity ) onVariantAttack( (LivingEntity) target );
        super.doEnchantDamageEffects( attacker, target );
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @SuppressWarnings( "unused" ) // Not normally used for creepers
    protected void onVariantAttack( LivingEntity target ) { }
    
    /** Called to perform this creeper's explosion 'attack'. */
    @Override
    protected void explodeCreeper() {
        if( !level().isClientSide ) {
            dead = true;
            makeVariantExplosion( getVariantExplosionPower( explosionRadius ) );
            discard();
            spawnLingeringCloud();
        }
    }
    
    /** Override to change this creeper's explosion power multiplier. */
    protected float getVariantExplosionPower( float radius ) { return radius * (isSupercharged() ? 3.5F : isPowered() ? 2.0F : 1.0F); }
    
    /** Override to change this creeper's explosion. */
    protected void makeVariantExplosion( float explosionPower ) {
        ExplosionHelper.explode( this, explosionPower, true, false );
    }
    
    /** Called to create a lingering effect cloud as part of this creeper's explosion 'attack'. */
    @Override
    protected void spawnLingeringCloud() {
        final List<MobEffectInstance> effects = new ArrayList<>( getActiveEffects() );
        modifyVariantLingeringCloudEffects( effects );
        
        if( !effects.isEmpty() ) {
            final AreaEffectCloud potionCloud = new AreaEffectCloud( level(), getX(), getY(), getZ() );
            potionCloud.setRadius( getVariantExplosionPower( explosionRadius - 0.5F ) );
            potionCloud.setRadiusOnUse( -0.5F );
            potionCloud.setWaitTime( 10 );
            potionCloud.setDuration( potionCloud.getDuration() / 2 );
            potionCloud.setRadiusPerTick( -potionCloud.getRadius() / (float) potionCloud.getDuration() );
            for( MobEffectInstance effect : effects ) {
                potionCloud.addEffect( new MobEffectInstance( effect ) );
            }
            modifyVariantLingeringCloud( potionCloud );
            level().addFreshEntity( potionCloud );
        }
    }
    
    /**
     * Override to change effects applied by the lingering cloud left by this creeper's explosion.
     * If this list is empty, the lingering cloud is not created.
     */
    protected void modifyVariantLingeringCloudEffects( List<MobEffectInstance> potions ) { }
    
    /** Override to change stats of the lingering cloud left by this creeper's explosion. */
    protected void modifyVariantLingeringCloud( AreaEffectCloud potionCloud ) { }
    
    /** Override to save data to this entity's NBT data. */
    public void addVariantSaveData( CompoundTag saveTag ) { }
    
    /** Override to load data from this entity's NBT data. */
    public void readVariantSaveData( CompoundTag saveTag ) { }
    
    
    //--------------- Family-Specific Implementations ----------------
    
    /** The parameter for special mob render scale. */
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId( _SpecialCreeperEntity.class, EntityDataSerializers.FLOAT );
    
    public _SpecialCreeperEntity( EntityType<? extends _SpecialCreeperEntity> entityType, Level level ) {
        super( entityType, level );
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
    public void thunderHit( ServerLevel level, LightningBolt lightningBolt ) {
        charge();
        super.thunderHit( level, lightningBolt );
        
        // Make it less likely for charged "explode while burning" creepers to immediately explode
        if( explodesWhileBurning() ) clearFire();
    }
    
    
    //--------------- Creeper Explosion Property Setters/Getters ----------------
    
    /** The parameter for creeper explosion properties. This is a combination of boolean flags. */
    private static final EntityDataAccessor<Byte> EXPLODE_FLAGS = SynchedEntityData.defineId( _SpecialCreeperEntity.class, EntityDataSerializers.BYTE );
    
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
        setPathfindingMalus( BlockPathTypes.WATER, value ? BlockPathTypes.LAVA.getMalus() : BlockPathTypes.WATER.getMalus() );
    }
    
    /** @return True if this creeper explodes while burning. */
    public boolean explodesWhileBurning() { return getExplodeFlag( EXPLODE_FLAG_ON_FIRE ); }
    
    /** Sets this creeper's property to explode while burning. */
    private void setExplodesWhileBurning( boolean value ) {
        setExplodeFlag( EXPLODE_FLAG_ON_FIRE, value );
        if( value ) {
            setPathfindingMalus( BlockPathTypes.DANGER_FIRE, BlockPathTypes.DAMAGE_FIRE.getMalus() );
            setPathfindingMalus( BlockPathTypes.DAMAGE_FIRE, BlockPathTypes.BLOCKED.getMalus() );
        }
        else {
            setPathfindingMalus( BlockPathTypes.DANGER_FIRE, BlockPathTypes.DANGER_FIRE.getMalus() );
            setPathfindingMalus( BlockPathTypes.DAMAGE_FIRE, BlockPathTypes.DAMAGE_FIRE.getMalus() );
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
    
    @Override
    public void setSpecialPathfindingMalus( BlockPathTypes type, float malus ) {
        this.setPathfindingMalus( type, malus );
    }
    
    
    /** Converts this entity to one of another type. */
    @Nullable
    @Override
    public <T extends Mob> T convertTo( EntityType<T> entityType, boolean keepEquipment ) {
        final T replacement = super.convertTo( entityType, keepEquipment );
        if( replacement instanceof ISpecialMob && level() instanceof ServerLevelAccessor serverLevel ) {
            MobHelper.finalizeSpawn( replacement, serverLevel, level().getCurrentDifficultyAt( blockPosition() ),
                    MobSpawnType.CONVERSION, null );
        }
        return replacement;
    }
    
    /** Called on spawn to initialize properties based on the world, difficulty, and the group it spawns with. */
    @Nullable
    @Override
    public final SpawnGroupData finalizeSpawn( ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                                  @Nullable SpawnGroupData groupData, @Nullable CompoundTag eggTag ) {
        return MobHelper.finalizeSpawn( this, level, difficulty, spawnType,
                super.finalizeSpawn( level, difficulty, spawnType, groupData, eggTag ) );
    }
    
    /** Called on spawn to set starting equipment. */
    @Override // Seal method to force spawn equipment changes through ISpecialMob
    protected final void populateDefaultEquipmentSlots( RandomSource random, DifficultyInstance difficulty ) { super.populateDefaultEquipmentSlots( random, difficulty ); }
    
    /** Called on spawn to initialize properties based on the world, difficulty, and the group it spawns with. */
    @Override
    public void finalizeSpecialSpawn( ServerLevelAccessor level, DifficultyInstance difficulty, @Nullable MobSpawnType spawnType,
                                      @Nullable SpawnGroupData groupData ) {
        if( level.getLevelData().isThundering() ) {
            final double chargedChance = getConfig().CREEPERS.stormChargeChance.get() < 0.0 ?
                    MobFamily.CREEPER.config.CREEPERS.familyStormChargeChance.get() :
                    getConfig().CREEPERS.stormChargeChance.get();
            
            if( random.nextDouble() < chargedChance ) charge();
        }
        
        finalizeVariantSpawn( level, difficulty, spawnType, groupData );
    }
    
    
    //--------------- SpecialMobData Hooks ----------------
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        super.aiStep();
        getSpecialData().tick();
    }
    
    //    /** @return The eye height of this entity when standing. */
    //    @Override
    //    protected float getStandingEyeHeight( Pose pose, EntitySize size ) {
    //        return super.getStandingEyeHeight( pose, size ) * getSpecialData().getHeightScaleByAge();
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
    public boolean canBeLeashed( Player player ) { return !isLeashed() && getSpecialData().allowLeashing(); }
    
    /** Sets this entity 'stuck' inside a block, such as a cobweb or sweet berry bush. Mod blocks could use this as a speed boost. */
    @Override
    public void makeStuckInBlock( BlockState block, Vec3 speedMulti ) {
        if( getSpecialData().canBeStuckIn( block ) ) super.makeStuckInBlock( block, speedMulti );
    }
    
    /** @return Called when this mob falls. Calculates and applies fall damage. Returns false if canceled. */
    @Override
    public boolean causeFallDamage( float distance, float damageMultiplier, DamageSource damageSource) {
        return super.causeFallDamage( distance, damageMultiplier * getSpecialData().getFallDamageMultiplier(), damageSource );
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
        if( isSensitiveToWater() && source.getDirectEntity() instanceof Snowball ) {
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
    public boolean canBeAffected( MobEffectInstance effect ) { return getSpecialData().isPotionApplicable( effect ); }
    
    /** Saves data to this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void addAdditionalSaveData( CompoundTag tag ) {
        super.addAdditionalSaveData( tag );
        
        final CompoundTag saveTag = SpecialMobData.getSaveLocation( tag );
        
        saveTag.putBoolean( References.TAG_SUPERCHARGED, isSupercharged() );
        
        saveTag.putBoolean( References.TAG_DRY_EXPLODE, cannotExplodeWhileWet() );
        saveTag.putBoolean( References.TAG_WHILE_BURNING_EXPLODE, explodesWhileBurning() );
        saveTag.putBoolean( References.TAG_WHEN_SHOT_EXPLODE, explodesWhenShot() );
        
        getSpecialData().writeToNBT( saveTag );
        addVariantSaveData( saveTag );
    }
    
    /** Loads data from this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void readAdditionalSaveData( CompoundTag tag ) {
        super.readAdditionalSaveData( tag );
        
        final CompoundTag saveTag = SpecialMobData.getSaveLocation( tag );
        
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