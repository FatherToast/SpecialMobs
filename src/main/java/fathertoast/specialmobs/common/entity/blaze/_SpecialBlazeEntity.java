package fathertoast.specialmobs.common.entity.blaze;

import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.BlazeSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.SpecialMobData;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.entity.ai.goal.SpecialBlazeAttackGoal;
import fathertoast.specialmobs.common.entity.ai.goal.SpecialHurtByTargetGoal;
import fathertoast.specialmobs.common.event.NaturalSpawnManager;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

@SpecialMob
public class _SpecialBlazeEntity extends Blaze implements RangedAttackMob, ISpecialMob<_SpecialBlazeEntity> {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<_SpecialBlazeEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xFFF87E )
                .vanillaTextureBaseOnly( "textures/entity/blaze.png" )
                .experience( 10 )
                .waterSensitive()
                .fallImmune()
                .fireballAttack( 1.0, 60, 100, 48.0 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( ConfigManager manager, MobFamily.Species<?> species ) {
        return new BlazeSpeciesConfig( manager, species, 3, 6 );
    }
    
    /** @return This entity's species config. */
    public BlazeSpeciesConfig getConfig() { return (BlazeSpeciesConfig) getSpecies().config; }
    
    @SpecialMob.AttributeSupplier
    public static AttributeSupplier.Builder createAttributes() { return Blaze.createAttributes(); }
    
    @SpecialMob.SpawnPlacementRegistrar
    public static void registerSpawnPlacement( MobFamily.Species<? extends _SpecialBlazeEntity> species ) {
        NaturalSpawnManager.registerSpawnPlacement( species, NaturalSpawnManager::checkSpawnRulesIgnoreLight );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        //noinspection SpellCheckingInspection
        return References.translations( langKey, "Blaze",
                "Blaze", "Blaze", "Blaze", "Blaze", "Lohe", "Blisterin' Blaze" );
    }
    
    @SpecialMob.LootTableProvider
    public static void addBaseLoot( LootTableBuilder loot ) {
        loot.addLootTable( "main", EntityType.BLAZE.getDefaultLootTable() );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<_SpecialBlazeEntity> getFactory() { return _SpecialBlazeEntity::new; }
    
    
    //--------------- Variant-Specific Breakouts ----------------
    
    /** Called in the MobEntity.class constructor to initialize AI goals. */
    @Override
    protected void registerGoals() {
        super.registerGoals();
        AIHelper.removeGoals( goalSelector, 4 ); // BlazeEntity.FireballAttackGoal
        goalSelector.addGoal( 4, new SpecialBlazeAttackGoal( this ) );
        AIHelper.replaceHurtByTarget( this, new SpecialHurtByTargetGoal( this, Blaze.class ).setAlertOthers() );
        
        registerVariantGoals();
    }
    
    /** Override to change this entity's AI goals. */
    protected void registerVariantGoals() { }
    
    /** Override to change starting equipment or stats. */
    @SuppressWarnings( "unused" )
    public void finalizeVariantSpawn( ServerLevelAccessor level, DifficultyInstance difficulty, @Nullable MobSpawnType spawnType,
                                     @Nullable SpawnGroupData groupData ) { }
    
    /** Called when this entity successfully damages a target to apply on-hit effects. */
    @Override
    public void doEnchantDamageEffects( LivingEntity attacker, Entity target ) {
        if( target instanceof LivingEntity ) onVariantAttack( (LivingEntity) target );
        super.doEnchantDamageEffects( attacker, target );
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    protected void onVariantAttack( LivingEntity target ) { }
    
    /** Called to attack the target with a ranged attack. */
    @Override
    public void performRangedAttack( LivingEntity target, float damageMulti ) {
        References.LevelEvent.BLAZE_SHOOT.play( this );
        
        final float accelVariance = Mth.sqrt( distanceTo( target ) ) * 0.5F * getSpecialData().getRangedAttackSpread();
        final double dX = target.getX() - getX() + getRandom().nextGaussian() * accelVariance;
        final double dY = target.getY( 0.5 ) - getY( 0.5 );
        final double dZ = target.getZ() - getZ() + getRandom().nextGaussian() * accelVariance;
        
        final SmallFireball fireball = new SmallFireball( level(), this, dX, dY, dZ );
        fireball.setPos( getX(), getY( 0.5 ) + 0.5, getZ() );
        level().addFreshEntity( fireball );
    }
    
    /** Override to save data to this entity's NBT data. */
    public void addVariantSaveData( CompoundTag saveTag ) { }
    
    /** Override to load data from this entity's NBT data. */
    public void readVariantSaveData( CompoundTag saveTag ) { }
    
    
    //--------------- Family-Specific Implementations ----------------
    
    /** The parameter for special mob render scale. */
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId( _SpecialBlazeEntity.class, EntityDataSerializers.FLOAT );
    
    /** The amount of fireballs in each burst. */
    public int fireballBurstCount;
    /** The ticks between each shot in a burst. */
    public int fireballBurstDelay;
    
    public _SpecialBlazeEntity( EntityType<? extends _SpecialBlazeEntity> entityType, Level level ) {
        super( entityType, level );
        fireballBurstCount = getConfig().BLAZES.fireballBurstCount.get();
        fireballBurstDelay = getConfig().BLAZES.fireballBurstDelay.get();
        
        getSpecialData().initialize();
    }
    
    /** Called from the Entity.class constructor to define data watcher variables. */
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        specialData = new SpecialMobData<>( this, SCALE );
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return EntityType.BLAZE.getDefaultLootTable();
    }



    //--------------- ISpecialMob Implementation ----------------
    
    private SpecialMobData<_SpecialBlazeEntity> specialData;
    
    /** @return This mob's special data. */
    @Override
    public final SpecialMobData<_SpecialBlazeEntity> getSpecialData() { return specialData; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends _SpecialBlazeEntity> getSpecies() { return SPECIES; }
    
    /** @return The experience that should be dropped by this entity. */
    @Override
    public final int getExperience() { return xpReward; }
    
    /** Sets the experience that should be dropped by this entity. */
    @Override
    public final void setExperience( int xp ) { xpReward = xp; }
    
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
        finalizeVariantSpawn( level, difficulty, spawnType, groupData );
    }
    
    @Override
    public void setSpecialPathfindingMalus( BlockPathTypes type, float malus ) {
        this.setPathfindingMalus( type, malus );
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
    public boolean causeFallDamage( float distance, float damageMultiplier, DamageSource damageSource ) {
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
        if( source.getDirectEntity() instanceof Snowball ) {
            if( isSensitiveToWater() ) amount = Math.max( 3.0F, amount );
            else amount = 0.0F; // Allow blazes to be insensitive to snowballs
        }
        return super.hurt( source, amount );
    }
    
    /** @return True if the effect can be applied to this entity. */
    @Override
    public boolean canBeAffected( MobEffectInstance effect ) { return getSpecialData().isPotionApplicable( effect ); }
    
    /** Saves data to this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void addAdditionalSaveData( CompoundTag tag ) {
        super.addAdditionalSaveData( tag );
        
        final CompoundTag saveTag = SpecialMobData.getSaveLocation( tag );
        
        saveTag.putByte( References.TAG_BURST_COUNT, (byte) fireballBurstCount );
        saveTag.putByte( References.TAG_BURST_DELAY, (byte) fireballBurstDelay );
        
        getSpecialData().writeToNBT( saveTag );
        addVariantSaveData( saveTag );
    }
    
    /** Loads data from this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void readAdditionalSaveData( CompoundTag tag ) {
        super.readAdditionalSaveData( tag );
        
        final CompoundTag saveTag = SpecialMobData.getSaveLocation( tag );
        
        if( saveTag.contains( References.TAG_BURST_COUNT, References.NBT_TYPE_NUMERICAL ) )
            fireballBurstCount = saveTag.getByte( References.TAG_BURST_COUNT );
        if( saveTag.contains( References.TAG_BURST_DELAY, References.NBT_TYPE_NUMERICAL ) )
            fireballBurstDelay = saveTag.getByte( References.TAG_BURST_DELAY );
        
        getSpecialData().readFromNBT( saveTag );
        readVariantSaveData( saveTag );
    }
}