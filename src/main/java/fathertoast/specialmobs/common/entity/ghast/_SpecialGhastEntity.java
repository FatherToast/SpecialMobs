package fathertoast.specialmobs.common.entity.ghast;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.SpecialMobData;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.entity.ai.SimpleFlyingMovementController;
import fathertoast.specialmobs.common.entity.ai.goal.SpecialGhastFireballAttackGoal;
import fathertoast.specialmobs.common.entity.ai.goal.SpecialGhastLookAroundGoal;
import fathertoast.specialmobs.common.entity.ai.goal.SpecialGhastMeleeAttackGoal;
import fathertoast.specialmobs.common.event.NaturalSpawnManager;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

@SpecialMob
public class _SpecialGhastEntity extends Ghast implements RangedAttackMob, ISpecialMob<_SpecialGhastEntity> {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<_SpecialGhastEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xBCBCBC )
                .vanillaTextureWithAnimation( "textures/entity/ghast/ghast.png", "textures/entity/ghast/ghast_shooting.png" )
                .experience( 5 )
                .fireballAttack( 0.0, 20, 40, 64.0 );
    }
    
    @SpecialMob.AttributeSupplier
    public static AttributeSupplier.Builder createAttributes() {
        return Ghast.createAttributes().add( Attributes.ATTACK_DAMAGE, 4.0 );
    }
    
    @SpecialMob.SpawnPlacementRegistrar
    public static void registerSpawnPlacement( MobFamily.Species<? extends _SpecialGhastEntity> species ) {
        NaturalSpawnManager.registerSpawnPlacement( species, _SpecialGhastEntity::checkFamilySpawnRules );
    }
    
    public static boolean checkFamilySpawnRules(EntityType<? extends Ghast> type, ServerLevelAccessor level,
                                                MobSpawnType spawnType, BlockPos pos, RandomSource random ) {
        //noinspection unchecked
        return Ghast.checkGhastSpawnRules( (EntityType<Ghast>) type, level, spawnType, pos, random ) &&
                NaturalSpawnManager.checkSpawnRulesConfigured( type, level, spawnType, pos, random );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Ghast",
                "Ghast", "Ghast", "Ghast", "Ghast", "Ghast", "Ghast" );
    }
    
    @SpecialMob.LootTableProvider
    public static void addBaseLoot( LootTableBuilder loot ) {
        loot.addLootTable( "main", EntityType.GHAST.getDefaultLootTable() );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<_SpecialGhastEntity> getFactory() { return _SpecialGhastEntity::new; }
    
    
    //--------------- Variant-Specific Breakouts ----------------
    
    /** Called in the MobEntity.class constructor to initialize AI goals. */
    @Override
    protected void registerGoals() {
        super.registerGoals();
        // We actually do want to replace both of these, even though we don't have the option of only replacing one
        AIHelper.removeGoals( goalSelector, 7 ); // GhastEntity.LookAroundGoal & GhastEntity.FireballAttackGoal
        goalSelector.addGoal( 7, new SpecialGhastLookAroundGoal( this ) );
        
        // Allow ghasts to target things not directly horizontal to them (why was this ever added?)
        if( MobFamily.GHAST.config.GHASTS.allowVerticalTargeting.get() ) {
            AIHelper.removeGoals( targetSelector, NearestAttackableTargetGoal.class );
            targetSelector.addGoal( 1, new NearestAttackableTargetGoal<>( this, Player.class, true ) );
        }
        
        registerVariantGoals();
    }
    
    /** Override to change this entity's AI goals. */
    protected void registerVariantGoals() { }
    
    /** Override to change this entity's attack goal priority. */
    protected int getVariantAttackPriority() { return 4; }
    
    /** Override to change starting equipment or stats. */
    @SuppressWarnings( "unused" )
    public void finalizeVariantSpawn( ServerLevelAccessor level, DifficultyInstance difficulty, @Nullable MobSpawnType spawnType,
                                      @Nullable SpawnGroupData groupData ) { }
    
    /** Called to attack the target with a ranged attack. */
    @Override
    public void performRangedAttack(LivingEntity target, float damageMulti ) {
        References.LevelEvent.GHAST_SHOOT.play( this );
        
        final float accelVariance = Mth.sqrt( distanceTo( target ) ) * 0.5F * getSpecialData().getRangedAttackSpread();
        final Vec3 lookVec = getViewVector( 1.0F ).scale( getBbWidth() );
        double dX = target.getX() - (getX() + lookVec.x) + getRandom().nextGaussian() * accelVariance;
        double dY = target.getY( 0.5 ) - (0.5 + getY( 0.5 ));
        double dZ = target.getZ() - (getZ() + lookVec.z) + getRandom().nextGaussian() * accelVariance;
        
        final LargeFireball fireball = new LargeFireball( level(), this, dX, dY, dZ, getVariantExplosionPower( getExplosionPower() ) );
        fireball.setPos(
                getX() + lookVec.x,
                getY( 0.5 ) + 0.5,
                getZ() + lookVec.z );
        level().addFreshEntity( fireball );
    }
    
    /** Override to change this ghast's explosion power multiplier. */
    protected int getVariantExplosionPower( int radius ) { return radius; }
    
    /** Called when this entity successfully damages a target to apply on-hit effects. */
    @Override
    public void doEnchantDamageEffects( LivingEntity attacker, Entity target ) {
        if( target instanceof LivingEntity ) onVariantAttack( (LivingEntity) target );
        super.doEnchantDamageEffects( attacker, target );
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    protected void onVariantAttack( LivingEntity target ) { }
    
    /** Override to save data to this entity's NBT data. */
    public void addVariantSaveData( CompoundTag saveTag ) { }
    
    /** Override to load data from this entity's NBT data. */
    public void readVariantSaveData( CompoundTag saveTag ) { }
    
    
    //--------------- Family-Specific Implementations ----------------
    
    /** The parameter for special mob render scale. */
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId( _SpecialGhastEntity.class, EntityDataSerializers.FLOAT );
    
    /** This entity's attack AI. */
    private Goal currentAttackAI;
    
    public _SpecialGhastEntity( EntityType<? extends _SpecialGhastEntity> entityType, Level level ) {
        super( entityType, level );
        moveControl = new SimpleFlyingMovementController( this );
        reassessAttackGoal();
        getSpecialData().initialize();
    }
    
    /** Called from the Entity.class constructor to define data watcher variables. */
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        specialData = new SpecialMobData<>( this, SCALE );
    }
    
    /** Called to set this entity's attack AI based on current equipment. */
    public void reassessAttackGoal() {
        if( level() != null && !level().isClientSide ) {
            if( currentAttackAI != null ) goalSelector.removeGoal( currentAttackAI );
            
            currentAttackAI = getSpecialData().getRangedAttackMaxRange() > 0.0F ?
                    new SpecialGhastFireballAttackGoal( this ) :
                    new SpecialGhastMeleeAttackGoal( this );
            
            goalSelector.addGoal( getVariantAttackPriority(), currentAttackAI );
        }
    }
    
    
    //--------------- ISpecialMob Implementation ----------------
    
    private SpecialMobData<_SpecialGhastEntity> specialData;
    
    /** @return This mob's special data. */
    @Override
    public SpecialMobData<_SpecialGhastEntity> getSpecialData() { return specialData; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends _SpecialGhastEntity> getSpecies() { return SPECIES; }
    
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
    
    @Override
    public void setSpecialPathfindingMalus( BlockPathTypes types, float malus ) {
        this.setPathfindingMalus( types, malus );
    }
    
    /** Called on spawn to set starting equipment. */
    @Override // Seal method to force spawn equipment changes through ISpecialMob
    protected final void populateDefaultEquipmentSlots( RandomSource random, DifficultyInstance difficulty ) { super.populateDefaultEquipmentSlots( random, difficulty ); }
    
    /** Called on spawn to initialize properties based on the world, difficulty, and the group it spawns with. */
    @Override
    public void finalizeSpecialSpawn( ServerLevelAccessor level, DifficultyInstance difficulty, @Nullable MobSpawnType spawnType,
                                      @Nullable SpawnGroupData groupData ) {
        finalizeVariantSpawn( level, difficulty, spawnType, groupData );
        reassessAttackGoal();
    }
    
    
    //--------------- SpecialMobData Hooks ----------------
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        super.aiStep();
        getSpecialData().tick();
    }
    
    /** @return The eye height of this entity when standing. */
    @Override
    protected float getStandingEyeHeight( Pose pose, EntityDimensions size ) {
        return super.getStandingEyeHeight( pose, size ) * getSpecialData().getHeightScaleByAge();
    }
    
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
    public void makeStuckInBlock(BlockState block, Vec3 speedMulti ) {
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
        if( isSensitiveToWater() && source.getDirectEntity() instanceof Snowball ) {
            amount = Math.max( 3.0F, amount );
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
        
        getSpecialData().writeToNBT( saveTag );
        addVariantSaveData( saveTag );
    }
    
    /** Loads data from this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void readAdditionalSaveData( CompoundTag tag ) {
        super.readAdditionalSaveData( tag );
        
        final CompoundTag saveTag = SpecialMobData.getSaveLocation( tag );
        
        getSpecialData().readFromNBT( saveTag );
        readVariantSaveData( saveTag );
        
        reassessAttackGoal();
    }
}