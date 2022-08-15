package fathertoast.specialmobs.common.entity.drowned;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.DrownedSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.SpecialMobData;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.entity.ai.goal.SpecialDrownedAttackGoal;
import fathertoast.specialmobs.common.entity.ai.goal.SpecialHurtByTargetGoal;
import fathertoast.specialmobs.common.entity.ai.goal.SpecialTridentAttackGoal;
import fathertoast.specialmobs.common.event.NaturalSpawnManager;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.monster.DrownedEntity;
import net.minecraft.entity.monster.ZombifiedPiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SnowballEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

@SpecialMob
public class _SpecialDrownedEntity extends DrownedEntity implements ISpecialMob<_SpecialDrownedEntity> {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<_SpecialDrownedEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x799C65 )
                .vanillaTextureWithOverlay( "textures/entity/zombie/drowned.png", "textures/entity/zombie/drowned_outer_layer.png" )
                .experience( 5 ).undead()
                .throwAttack( 1.0, 1.0, 40, 10.0 );
    }
    
    protected static final double DEFAULT_TRIDENT_CHANCE = 0.0625;
    protected static final double DEFAULT_SHIELD_CHANCE = 0.0625;
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( MobFamily.Species<?> species ) {
        return new DrownedSpeciesConfig( species, DEFAULT_TRIDENT_CHANCE, DEFAULT_SHIELD_CHANCE );
    }
    
    /** @return This entity's species config. */
    public DrownedSpeciesConfig getConfig() { return (DrownedSpeciesConfig) getSpecies().config; }
    
    @SpecialMob.AttributeSupplier
    public static AttributeModifierMap.MutableAttribute createAttributes() { return DrownedEntity.createAttributes(); }
    
    @SpecialMob.SpawnPlacementRegistrar
    public static void registerSpawnPlacement( MobFamily.Species<? extends _SpecialDrownedEntity> species ) {
        NaturalSpawnManager.registerSpawnPlacement( species, EntitySpawnPlacementRegistry.PlacementType.IN_WATER,
                _SpecialDrownedEntity::checkFamilySpawnRules );
    }
    
    public static boolean checkFamilySpawnRules( EntityType<? extends DrownedEntity> type, IServerWorld world,
                                                 SpawnReason reason, BlockPos pos, Random random ) {
        //noinspection unchecked
        return DrownedEntity.checkDrownedSpawnRules( (EntityType<DrownedEntity>) type, world, reason, pos, random ) &&
                NaturalSpawnManager.checkSpawnRulesConfigured( type, world, reason, pos, random );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Drowned",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void addBaseLoot( LootTableBuilder loot ) {
        loot.addLootTable( "main", EntityType.DROWNED.getDefaultLootTable() );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<_SpecialDrownedEntity> getFactory() { return _SpecialDrownedEntity::new; }
    
    
    //--------------- Variant-Specific Breakouts ----------------
    
    /** Called in the MobEntity.class constructor to initialize AI goals. */
    @Override
    protected void registerGoals() {
        super.registerGoals();
        
        // We don't really want to remove the melee attack goal, so just re-add it afterward
        AIHelper.removeGoals( goalSelector, 2 ); // DrownedEntity.TridentAttackGoal & DrownedEntity.AttackGoal
        goalSelector.addGoal( 2, new SpecialDrownedAttackGoal( this, 1.0, false ) );
        
        AIHelper.replaceHurtByTarget( this, new SpecialHurtByTargetGoal( this, DrownedEntity.class )
                .setAlertOthers( ZombifiedPiglinEntity.class ) );
        
        registerVariantGoals();
    }
    
    /** Override to change this entity's AI goals. */
    protected void registerVariantGoals() { }
    
    /** Override to change starting equipment or stats. */
    public void finalizeVariantSpawn( IServerWorld world, DifficultyInstance difficulty, @Nullable SpawnReason spawnReason,
                                      @Nullable ILivingEntityData groupData ) { }
    
    /** Called when this entity successfully damages a target to apply on-hit effects. */
    @Override
    public void doEnchantDamageEffects( LivingEntity attacker, Entity target ) {
        if( target instanceof LivingEntity ) onVariantAttack( (LivingEntity) target );
        super.doEnchantDamageEffects( attacker, target );
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    protected void onVariantAttack( LivingEntity target ) { }
    
    /** Override to save data to this entity's NBT data. */
    public void addVariantSaveData( CompoundNBT saveTag ) { }
    
    /** Override to load data from this entity's NBT data. */
    public void readVariantSaveData( CompoundNBT saveTag ) { }
    
    
    //--------------- Family-Specific Implementations ----------------
    
    /** The parameter for special mob render scale. */
    private static final DataParameter<Float> SCALE = EntityDataManager.defineId( _SpecialDrownedEntity.class, DataSerializers.FLOAT );
    
    private boolean needsToBeDeeper;
    
    public _SpecialDrownedEntity( EntityType<? extends _SpecialDrownedEntity> entityType, World world ) {
        super( entityType, world );
        recalculateAttackGoal();
        
        getSpecialData().initialize();
    }
    
    /** Called from the Entity.class constructor to define data watcher variables. */
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        specialData = new SpecialMobData<>( this, SCALE );
    }
    
    /** Called to update this entity's attack AI based on NBT data. */
    public void recalculateAttackGoal() {
        if( level != null && !level.isClientSide ) {
            AIHelper.removeGoals( goalSelector, SpecialTridentAttackGoal.class );
            if( getSpecialData().getRangedAttackMaxRange() > 0.0F ) {
                goalSelector.addGoal( 2, new SpecialTridentAttackGoal( this, getSpecialData().getRangedWalkSpeed(),
                        getSpecialData().getRangedAttackCooldown(), getSpecialData().getRangedAttackMaxRange() ) );
            }
        }
    }
    
    /** Called to attack the target with a ranged attack. */
    @Override
    public void performRangedAttack( LivingEntity target, float damageMulti ) {
        final TridentEntity trident = new TridentEntity( level, this, new ItemStack( Items.TRIDENT ) );
        
        final double dX = target.getX() - getX();
        final double dY = target.getY( 0.3333 ) - trident.getY();
        final double dZ = target.getZ() - getZ();
        final double dH = MathHelper.sqrt( dX * dX + dZ * dZ );
        trident.shoot( dX, dY + dH * 0.2, dZ, 1.6F,
                getSpecialData().getRangedAttackSpread() * (14 - level.getDifficulty().getId() * 4) );
        
        playSound( SoundEvents.DROWNED_SHOOT, 1.0F, 1.0F / (getRandom().nextFloat() * 0.4F + 0.8F) );
        level.addFreshEntity( trident );
    }
    
    /** Called each tick to update this entity's swimming state. */
    @Override
    public void updateSwimming() {
        if( !level.isClientSide && isEffectiveAi() ) needsToBeDeeper = true;
        super.updateSwimming();
    }
    
    /** Moves this entity in the desired direction. Input magnitude of < 1 scales down movement speed. */
    @Override
    public void travel( Vector3d input ) {
        if( isEffectiveAi() ) needsToBeDeeper = true;
        super.travel( input );
    }
    
    /** @return Water drag coefficient. */
    @Override
    protected float getWaterSlowDown() { return 0.9F; } // Improve mobility in shallow water
    
    /** @return True if this entity is in water. */
    @Override
    public boolean isInWater() {
        // Hacky way to fix vanilla drowned AI breaking in shallow water
        if( needsToBeDeeper ) {
            needsToBeDeeper = false;
            return isUnderWater();
        }
        return super.isInWater();
    }
    
    
    //--------------- ISpecialMob Implementation ----------------
    
    private SpecialMobData<_SpecialDrownedEntity> specialData;
    
    /** @return This mob's special data. */
    @Override
    public SpecialMobData<_SpecialDrownedEntity> getSpecialData() { return specialData; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends _SpecialDrownedEntity> getSpecies() { return SPECIES; }
    
    /** @return The experience that should be dropped by this entity. */
    @Override
    public final int getExperience() { return xpReward; }
    
    /** Sets the experience that should be dropped by this entity. */
    @Override
    public final void setExperience( int xp ) { xpReward = xp; }

    @Override
    public void setSpecialPathfindingMalus(PathNodeType nodeType, float malus) {
        this.setPathfindingMalus(nodeType, malus);
    }

    /** Converts this entity to one of another type. */
    @Nullable
    @Override
    public <T extends MobEntity> T convertTo( EntityType<T> entityType, boolean keepEquipment ) {
        final T replacement = super.convertTo( entityType, keepEquipment );
        if( replacement instanceof ISpecialMob && level instanceof IServerWorld ) {
            MobHelper.finalizeSpawn( replacement, (IServerWorld) level, level.getCurrentDifficultyAt( blockPosition() ),
                    SpawnReason.CONVERSION, null );
        }
        return replacement;
    }
    
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
        // Completely re-roll held item
        final ItemStack heldItem = getItemBySlot( EquipmentSlotType.MAINHAND );
        if( heldItem.isEmpty() || heldItem.getItem() == Items.TRIDENT || heldItem.getItem() == Items.FISHING_ROD ) {
            final double heldItemChoice = random.nextDouble();
            if( getSpecialData().getRangedAttackMaxRange() > 0.0F && heldItemChoice < getConfig().DROWNED.tridentEquipChance.get() ) {
                setItemSlot( EquipmentSlotType.MAINHAND, new ItemStack( Items.TRIDENT ) );
            }
            else if( heldItemChoice >= 0.9625 ) { // Vanilla's 3.75% chance; not configurable because not important
                setItemSlot( EquipmentSlotType.MAINHAND, new ItemStack( Items.FISHING_ROD ) );
            }
            else {
                setItemSlot( EquipmentSlotType.MAINHAND, ItemStack.EMPTY );
            }
        }
        
        if( getConfig().DROWNED.shieldEquipChance.rollChance( random ) ) {
            setItemSlot( EquipmentSlotType.OFFHAND, new ItemStack( Items.SHIELD ) );
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
    
    /** @return The eye height of this entity when standing. */
    @Override
    protected float getStandingEyeHeight( Pose pose, EntitySize size ) {
        return super.getStandingEyeHeight( pose, size ) * getSpecialData().getHeightScale(); // Age handled in super
    }
    
    /** @return Whether this entity is immune to fire damage. */
    @Override
    public boolean fireImmune() { return getSpecialData().isImmuneToFire(); }
    
    /** Sets this entity on fire for a specific duration. */
    @Override
    public void setRemainingFireTicks( int ticks ) {
        if( !getSpecialData().isImmuneToBurning() ) super.setRemainingFireTicks( ticks );
    }
    
    /** @return True if this zombie burns in sunlight. */
    @Override
    protected boolean isSunSensitive() { return !getSpecialData().isImmuneToFire() && !getSpecialData().isImmuneToBurning(); }
    
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
        final Entity entity = source.getDirectEntity();
        if( isSensitiveToWater() && entity instanceof SnowballEntity ) {
            amount = Math.max( 3.0F, amount );
        }
        
        // Shield blocking logic
        if( amount > 0.0F && MobHelper.tryBlockAttack( this, source, true ) ) return false;
        return super.hurt( source, amount );
    }
    
    /** @return True if the effect can be applied to this entity. */
    @Override
    public boolean canBeAffected( EffectInstance effect ) { return getSpecialData().isPotionApplicable( effect ); }
    
    /** Saves data to this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void addAdditionalSaveData( CompoundNBT tag ) {
        super.addAdditionalSaveData( tag );
        
        final CompoundNBT saveTag = SpecialMobData.getSaveLocation( tag );
        
        getSpecialData().writeToNBT( saveTag );
        addVariantSaveData( saveTag );
    }
    
    /** Loads data from this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void readAdditionalSaveData( CompoundNBT tag ) {
        super.readAdditionalSaveData( tag );
        
        final CompoundNBT saveTag = SpecialMobData.getSaveLocation( tag );
        
        getSpecialData().readFromNBT( saveTag );
        readVariantSaveData( saveTag );
        
        recalculateAttackGoal();
    }
}