package fathertoast.specialmobs.common.entity.skeleton;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.entity.SpecialMobData;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class _SpecialSkeletonEntity extends AbstractSkeletonEntity implements ISpecialMob<_SpecialSkeletonEntity> {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0x494949 );
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return SkeletonEntity.createAttributes();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Skeleton",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void addBaseLoot( LootTableBuilder loot ) {
        loot.addLootTable( "main", EntityType.SKELETON.getDefaultLootTable() );
    }
    
    @SpecialMob.Constructor
    public _SpecialSkeletonEntity( EntityType<? extends _SpecialSkeletonEntity> entityType, World world ) {
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
    
    /** Override to change this entity's attack goal priority. */
    protected int getVariantAttackPriority() { return 4; }
    
    // TODO variant shooting
    
    /** Override to save data to this entity's NBT data. */
    public void addVariantSaveData( CompoundNBT saveTag ) { }
    
    /** Override to load data from this entity's  NBT data. */
    public void readVariantSaveData( CompoundNBT saveTag ) { }
    
    
    //--------------- Family-Specific Implementations ----------------
    
    /** The parameter for special mob render scale. */
    private static final DataParameter<Float> SCALE = EntityDataManager.defineId( _SpecialSkeletonEntity.class, DataSerializers.FLOAT );
    
    /** This entity's attack AI. */
    private Goal currentAttackAI;
    
    /** Called from the Entity.class constructor to define data watcher variables. */
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        specialData = new SpecialMobData<>( this, SCALE, 1.0F );
        entityData.define( IS_BABY, false );
    }
    
    @Override
    protected void populateDefaultEquipmentSlots( DifficultyInstance difficulty ) {
        super.populateDefaultEquipmentSlots( difficulty );
        setItemSlot( EquipmentSlotType.MAINHAND, new ItemStack( Items.BOW ) );
    }
    
    @Nullable
    public ILivingEntityData finalizeSpawn( IServerWorld world, DifficultyInstance difficulty, SpawnReason spawnReason,
                                            @Nullable ILivingEntityData groupData, @Nullable CompoundNBT eggTag ) {
        groupData = super.finalizeSpawn( world, difficulty, spawnReason, groupData, eggTag );
        setBaby( random.nextDouble() < 0.05 ); //TODO config
        return groupData;
    }
    
    /** Called to change */
    @Override
    public void reassessWeaponGoal() {
        if( level != null && !level.isClientSide ) {
            if( currentAttackAI != null ) goalSelector.removeGoal( currentAttackAI );
            
            final SpecialMobData<_SpecialSkeletonEntity> data = getSpecialData();
            final ItemStack weapon = getItemInHand( ProjectileHelper.getWeaponHoldingHand(
                    this, item -> item instanceof BowItem ) );
            if( weapon.getItem() == Items.BOW ) {
                //                currentAttackAI = new EntityAIAttackRangedBow<>( //TODO
                //                        this, data.rangedWalkSpeed,
                //                        data.rangedAttackCooldown, data.rangedAttackMaxRange
                //                );
            }
            else {
                //currentAttackAI = new EntityAISpecialAttackMelee<>( this, 1.2, false ); //TODO
            }
            goalSelector.addGoal( getVariantAttackPriority(), currentAttackAI );
        }
    }
    
    
    //--------------- Vanilla Skeleton Implementations ----------------
    
    /** @return The sound this entity makes idly. */
    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.SKELETON_AMBIENT; }
    
    /** @return The sound this entity makes when damaged. */
    @Override
    protected SoundEvent getHurtSound( DamageSource source ) { return SoundEvents.SKELETON_HURT; }
    
    /** @return The sound this entity makes when killed. */
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.SKELETON_DEATH; }
    
    /** @return The sound this entity makes while walking. */
    @Override
    protected SoundEvent getStepSound() { return SoundEvents.SKELETON_STEP; }
    
    /** Called when this entity dies to add drops regardless of loot table. */
    @Override
    protected void dropCustomDeathLoot( DamageSource source, int looting, boolean killedByPlayer ) {
        super.dropCustomDeathLoot( source, looting, killedByPlayer );
        
        // Skull dropping; note that this enables strays to drop skulls unlike vanilla ones
        final Entity entity = source.getEntity();
        if( entity instanceof CreeperEntity ) {
            final CreeperEntity creeper = (CreeperEntity) entity;
            if( creeper.canDropMobsSkull() ) {
                creeper.increaseDroppedSkulls();
                spawnAtLocation( Items.SKELETON_SKULL );
            }
        }
    }
    
    
    //--------------- Baby-able Implementations ----------------
    
    /** The parameter for baby status. */
    private static final DataParameter<Boolean> IS_BABY = EntityDataManager.defineId( _SpecialSkeletonEntity.class, DataSerializers.BOOLEAN );
    /** The speed boost to apply when in baby state. */
    private static final AttributeModifier BABY_SPEED_BOOST = new AttributeModifier( UUID.fromString( "B9766B59-9566-4402-BC1F-2EE2A276D836" ),
            "Baby speed boost", 0.5, AttributeModifier.Operation.MULTIPLY_BASE );
    
    /** Sets this entity as a baby. */
    @Override
    public void setBaby( boolean value ) {
        getEntityData().set( IS_BABY, value );
        if( level != null && !level.isClientSide ) {
            ModifiableAttributeInstance attributeInstance = getAttribute( Attributes.MOVEMENT_SPEED );
            //noinspection ConstantConditions
            attributeInstance.removeModifier( BABY_SPEED_BOOST );
            if( value ) {
                attributeInstance.addTransientModifier( BABY_SPEED_BOOST );
            }
        }
    }
    
    /** @return True if this entity is a baby. */
    @Override
    public boolean isBaby() {
        return this.getEntityData().get( IS_BABY );
    }
    
    /** Called when a data watcher parameter is changed. */
    @Override
    public void onSyncedDataUpdated( DataParameter<?> parameter ) {
        if( IS_BABY.equals( parameter ) ) {
            refreshDimensions();
        }
        super.onSyncedDataUpdated( parameter );
    }
    
    /** @return The amount of experience to drop from this entity. */
    @Override
    protected int getExperienceReward( PlayerEntity player ) {
        if( isBaby() ) {
            xpReward = (int) ((float) xpReward * 2.5F);
        }
        return super.getExperienceReward( player );
    }
    
    //TODO make sure this works for differing base-scale variants
    @Override
    public double getMyRidingOffset() { return super.getMyRidingOffset() + (isBaby() ? 0.45 : 0.0); }
    
    
    //--------------- ISpecialMob Implementation ----------------
    
    private SpecialMobData<_SpecialSkeletonEntity> specialData;
    
    /** @return This mob's special data. */
    @Override
    public SpecialMobData<_SpecialSkeletonEntity> getSpecialData() { return specialData; }
    
    /** @return The experience that should be dropped by this entity. */
    @Override
    public final int getExperience() { return xpReward; }
    
    /** Sets the experience that should be dropped by this entity. */
    @Override
    public final void setExperience( int xp ) { xpReward = xp; }
    
    static ResourceLocation GET_TEXTURE_PATH( String type ) {
        return SpecialMobs.resourceLoc( SpecialMobs.TEXTURE_PATH + "skeleton/" + type + ".png" );
    }
    
    private static final ResourceLocation[] TEXTURES = { new ResourceLocation( "textures/entity/skeleton/skeleton.png" ) };
    
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
    
    /** @return The eye height of this entity when standing. */
    @Override
    protected float getStandingEyeHeight( Pose pose, EntitySize size ) {
        return super.getStandingEyeHeight( pose, size ) * getSpecialData().getBaseScale() * (isBaby() ? 0.53448F : 1.0F);
    }
    
    /** @return Whether this entity is immune to fire damage. */
    @Override
    public boolean fireImmune() { return specialData.isImmuneToFire(); }
    
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
        if( specialData.canBeStuckIn( block ) ) super.makeStuckInBlock( block, speedMulti );
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
    
    /** @return True if the effect can be applied to this entity. */
    @Override
    public boolean canBeAffected( EffectInstance effect ) { return getSpecialData().isPotionApplicable( effect ); }
    
    /** Saves data to this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void addAdditionalSaveData( CompoundNBT tag ) {
        super.addAdditionalSaveData( tag );
        
        final CompoundNBT saveTag = SpecialMobData.getSaveLocation( tag );
        
        saveTag.putBoolean( References.TAG_IS_BABY, isBaby() );
        
        getSpecialData().writeToNBT( saveTag );
        addVariantSaveData( saveTag );
    }
    
    /** Loads data from this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void readAdditionalSaveData( CompoundNBT tag ) {
        super.readAdditionalSaveData( tag );
        
        final CompoundNBT saveTag = SpecialMobData.getSaveLocation( tag );
        
        if( saveTag.contains( References.TAG_IS_BABY, References.NBT_TYPE_NUMERICAL ) )
            setBaby( saveTag.getBoolean( References.TAG_IS_BABY ) );
        
        getSpecialData().readFromNBT( saveTag );
        readVariantSaveData( saveTag );
        
        reassessWeaponGoal();
    }
}