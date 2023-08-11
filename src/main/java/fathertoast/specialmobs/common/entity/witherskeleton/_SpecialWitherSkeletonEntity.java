package fathertoast.specialmobs.common.entity.witherskeleton;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.SkeletonSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.SpecialMobData;
import fathertoast.specialmobs.common.event.NaturalSpawnManager;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

@SpecialMob
public class _SpecialWitherSkeletonEntity extends WitherSkeleton implements ISpecialMob<_SpecialWitherSkeletonEntity> {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<_SpecialWitherSkeletonEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x474D4D )
                .vanillaTextureBaseOnly( "textures/entity/skeleton/wither_skeleton.png" )
                .familySize( 1.2F )
                .experience( 5 ).undead()
                .bowAttack( 2.0, 1.0, 1.0, 20, 15.0 );
    }
    
    protected static final double DEFAULT_BOW_CHANCE = 0.05;
    protected static final double DEFAULT_SHIELD_CHANCE = 0.05;
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( MobFamily.Species<?> species ) {
        return new SkeletonSpeciesConfig( species, DEFAULT_BOW_CHANCE, DEFAULT_SHIELD_CHANCE );
    }
    
    /** @return This entity's species config. */
    public SkeletonSpeciesConfig getConfig() { return (SkeletonSpeciesConfig) getSpecies().config; }
    
    @SpecialMob.AttributeSupplier
    public static AttributeSupplier.Builder createAttributes() { return WitherSkeleton.createAttributes(); }
    
    @SpecialMob.SpawnPlacementRegistrar
    public static void registerSpawnPlacement( MobFamily.Species<? extends _SpecialWitherSkeletonEntity> species ) {
        NaturalSpawnManager.registerSpawnPlacement( species );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        //noinspection SpellCheckingInspection
        return References.translations( langKey, "Wither Skeleton",
                "Esqueleto Wither", "Esqueleto wither", "Wither squelette",
                "Scheletro wither", "Witherskelett", "Corrupted Bag o' Bones" );
    }
    
    @SpecialMob.LootTableProvider
    public static void addBaseLoot( LootTableBuilder loot ) {
        loot.addLootTable( "main", EntityType.WITHER_SKELETON.getDefaultLootTable() );
    }

    @SpecialMob.EntityTagProvider
    public static List<TagKey<EntityType<?>>> getEntityTags() {
        return Collections.singletonList(EntityTypeTags.SKELETONS);
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<_SpecialWitherSkeletonEntity> getFactory() { return _SpecialWitherSkeletonEntity::new; }
    
    
    //--------------- Variant-Specific Breakouts ----------------
    
    /** Called in the MobEntity.class constructor to initialize AI goals. */
    @Override
    protected void registerGoals() {
        super.registerGoals();
        registerVariantGoals();
    }
    
    /** Override to change this entity's AI goals. */
    protected void registerVariantGoals() { }
    
    /** Override to change this entity's attack goal priority. */
    protected int getVariantAttackPriority() { return 4; }
    
    /** Override to change starting equipment or stats. */
    public void finalizeVariantSpawn( ServerLevelAccessor level, DifficultyInstance difficulty, @Nullable MobSpawnType spawnReason,
                                     @Nullable SpawnGroupData groupData ) { }
    
    /** Called to attack the target with a ranged attack. */
    @Override
    public void performRangedAttack( LivingEntity target, float damageMulti ) {
        final ItemStack arrowItem = getProjectile( getItemInHand( ProjectileUtil.getWeaponHoldingHand(
                this, item -> item instanceof BowItem ) ) );
        AbstractArrow arrow = getArrow( arrowItem, damageMulti );
        if( getMainHandItem().getItem() instanceof BowItem )
            arrow = ((BowItem) getMainHandItem().getItem()).customArrow( arrow );
        
        final double dX = target.getX() - getX();
        final double dY = target.getY( 0.3333 ) - arrow.getY();
        final double dZ = target.getZ() - getZ();
        final double dH = Mth.sqrt( (float) (dX * dX + dZ * dZ) );
        arrow.shoot( dX, dY + dH * 0.2, dZ, 1.6F,
                getSpecialData().getRangedAttackSpread() * (14 - 4 * level.getDifficulty().getId()) );
        
        playSound( SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F) );
        level.addFreshEntity( arrow );
    }
    
    /** @return The arrow for this skeleton to shoot. */
    @Override
    protected AbstractArrow getArrow( ItemStack arrowItem, float damageMulti ) {
        return getVariantArrow( super.getArrow( arrowItem,
                damageMulti * getSpecialData().getRangedAttackDamage() / 2.0F ), arrowItem, damageMulti );
    }
    
    /** Override to modify this entity's ranged attack projectile. */
    protected AbstractArrow getVariantArrow( AbstractArrow arrow, ItemStack arrowItem, float damageMulti ) {
        return arrow;
    }
    
    /** Called when this entity successfully damages a target to apply on-hit effects. */
    @Override
    public void doEnchantDamageEffects( LivingEntity attacker, Entity target ) {
        if( target instanceof LivingEntity ) onVariantAttack( (LivingEntity) target );
        super.doEnchantDamageEffects( attacker, target );
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    protected void onVariantAttack( LivingEntity target ) { }
    
    /** Override to save data to this entity's NBT data. */
    public void addVariantSaveData( @SuppressWarnings( "unused" ) CompoundTag saveTag ) { }
    
    /** Override to load data from this entity's NBT data. */
    public void readVariantSaveData( @SuppressWarnings( "unused" ) CompoundTag saveTag ) { }
    
    
    //--------------- Family-Specific Implementations ----------------
    
    /** The parameter for special mob render scale. */
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId( _SpecialWitherSkeletonEntity.class, EntityDataSerializers.FLOAT );
    
    /** This entity's attack AI. */
    private Goal currentAttackAI;
    
    public _SpecialWitherSkeletonEntity( EntityType<? extends _SpecialWitherSkeletonEntity> entityType, Level level ) {
        super( entityType, level );
        getSpecialData().initialize();
    }
    
    /** Called from the Entity.class constructor to define data watcher variables. */
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        specialData = new SpecialMobData<>( this, SCALE );
        entityData.define( IS_BABY, false );
    }
    
    /** Called to set this entity's attack AI based on current equipment. */
    @Override
    public void reassessWeaponGoal() {
        if( level != null && !level.isClientSide ) {
            if( currentAttackAI != null ) goalSelector.removeGoal( currentAttackAI );
            
            final SpecialMobData<_SpecialWitherSkeletonEntity> data = getSpecialData();
            final ItemStack weapon = getItemInHand( ProjectileUtil.getWeaponHoldingHand(
                    this, item -> item instanceof BowItem ) );
            if( data.getRangedAttackMaxRange() > 0.0F && weapon.getItem() == Items.BOW ) {
                currentAttackAI = new RangedBowAttackGoal<>( this, data.getRangedWalkSpeed(),
                        data.getRangedAttackCooldown(), data.getRangedAttackMaxRange() );
            }
            else {
                currentAttackAI = new MeleeAttackGoal( this, 1.2, false );
            }
            goalSelector.addGoal( getVariantAttackPriority(), currentAttackAI );
        }
    }
    
    
    //--------------- Baby-able Implementations ----------------
    
    /** The parameter for baby status. */
    private static final EntityDataAccessor<Boolean> IS_BABY = SynchedEntityData.defineId( _SpecialWitherSkeletonEntity.class, EntityDataSerializers.BOOLEAN );
    
    /** Sets this entity as a baby. */
    @Override
    public void setBaby( boolean value ) {
        getEntityData().set( IS_BABY, value );
        if( level != null && !level.isClientSide ) {
            final AttributeInstance attributeInstance = getAttribute( Attributes.MOVEMENT_SPEED );
            //noinspection ConstantConditions
            attributeInstance.removeModifier( References.BABY_SPEED_BOOST );
            if( value ) {
                attributeInstance.addTransientModifier( References.BABY_SPEED_BOOST );
            }
        }
    }
    
    /** @return True if this entity is a baby. */
    @Override
    public boolean isBaby() { return getEntityData().get( IS_BABY ); }
    
    /** Called when a data watcher parameter is changed. */
    @Override
    public void onSyncedDataUpdated( EntityDataAccessor<?> parameter ) {
        if( IS_BABY.equals( parameter ) ) {
            refreshDimensions();
        }
        super.onSyncedDataUpdated( parameter );
    }
    
    /** @return The amount of experience to drop from this entity. */
    @Override
    public int getExperienceReward( ) {
        if( isBaby() ) {
            xpReward = (int) ((float) xpReward * 2.5F);
        }
        return super.getExperienceReward( );
    }
    
    //TODO make sure this works for differing base-scale variants
    @Override
    public double getMyRidingOffset() { return super.getMyRidingOffset() + (isBaby() ? 0.45 : 0.0); }
    
    
    //--------------- ISpecialMob Implementation ----------------
    
    private SpecialMobData<_SpecialWitherSkeletonEntity> specialData;
    
    /** @return This mob's special data. */
    @Override
    public SpecialMobData<_SpecialWitherSkeletonEntity> getSpecialData() { return specialData; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends _SpecialWitherSkeletonEntity> getSpecies() { return SPECIES; }
    
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
        if( replacement instanceof ISpecialMob && level instanceof ServerLevelAccessor serverLevel ) {
            MobHelper.finalizeSpawn( replacement, serverLevel, level.getCurrentDifficultyAt( blockPosition() ),
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
    public void setSpecialPathfindingMalus( BlockPathTypes type, float malus ) {
        this.setPathfindingMalus( type, malus );
    }
    
    /** Called on spawn to set starting equipment. */
    @Override // Seal method to force spawn equipment changes through ISpecialMob
    protected final void populateDefaultEquipmentSlots( RandomSource random, DifficultyInstance difficulty ) { super.populateDefaultEquipmentSlots( random, difficulty ); }
    
    /** Called on spawn to initialize properties based on the world, difficulty, and the group it spawns with. */
    @Override
    public void finalizeSpecialSpawn( ServerLevelAccessor level, DifficultyInstance difficulty, @Nullable MobSpawnType spawnType,
                                      @Nullable SpawnGroupData groupData ) {
        setBaby( MobFamily.WITHER_SKELETON.config.SKELETONS.babyChance.rollChance( random ) );
        
        if( getSpecialData().getRangedAttackMaxRange() > 0.0F && getConfig().SKELETONS.bowEquipChance.rollChance( random ) ) {
            setItemSlot( EquipmentSlot.MAINHAND, new ItemStack( Items.BOW ) );
        }
        else if( getConfig().SKELETONS.shieldEquipChance.rollChance( random ) ) {
            setItemSlot( EquipmentSlot.OFFHAND, new ItemStack( Items.SHIELD ) );
        }
        
        finalizeVariantSpawn( level, difficulty, spawnType, groupData );
        reassessWeaponGoal();
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
        final Entity entity = source.getDirectEntity();
        if( isSensitiveToWater() && entity instanceof Snowball ) {
            amount = Math.max( 3.0F, amount );
        }
        
        // Shield blocking logic
        if( amount > 0.0F && MobHelper.tryBlockAttack( this, source, true ) ) return false;
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
        
        saveTag.putBoolean( References.TAG_IS_BABY, isBaby() );
        
        getSpecialData().writeToNBT( saveTag );
        addVariantSaveData( saveTag );
    }
    
    /** Loads data from this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void readAdditionalSaveData( CompoundTag tag ) {
        super.readAdditionalSaveData( tag );
        
        final CompoundTag saveTag = SpecialMobData.getSaveLocation( tag );
        
        if( saveTag.contains( References.TAG_IS_BABY, References.NBT_TYPE_NUMERICAL ) )
            setBaby( saveTag.getBoolean( References.TAG_IS_BABY ) );
        
        getSpecialData().readFromNBT( saveTag );
        readVariantSaveData( saveTag );
        
        reassessWeaponGoal();
    }
}