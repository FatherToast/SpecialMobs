package fathertoast.specialmobs.common.entity.witch;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.SpecialMobData;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.event.NaturalSpawnManager;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@SpecialMob
public class _SpecialWitchEntity extends Witch implements ISpecialMob<_SpecialWitchEntity> {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<_SpecialWitchEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x51A03E )
                .vanillaTextureBaseOnly( "textures/entity/witch.png" )
                .experience( 5 )
                .throwAttack( 1.0, 1.0, 60, 10.0 );
    }
    
    @SpecialMob.AttributeSupplier
    public static AttributeSupplier.Builder createAttributes() { return Witch.createAttributes(); }
    
    @SpecialMob.SpawnPlacementRegistrar
    public static void registerSpawnPlacement( MobFamily.Species<? extends _SpecialWitchEntity> species ) {
        NaturalSpawnManager.registerSpawnPlacement( species );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        //noinspection SpellCheckingInspection
        return References.translations( langKey, "Witch",
                "Bruja", "Bruxa", "Sorcière", "Strega", "Hexe", "Wizard" );
    }
    
    @SpecialMob.LootTableProvider
    public static void addBaseLoot( LootTableBuilder loot ) {
        loot.addLootTable("main", EntityType.WITCH.getDefaultLootTable());
    }


    @SpecialMob.EntityTagProvider
    public static List<TagKey<EntityType<?>>> getEntityTags() {
        return Collections.singletonList(EntityTypeTags.RAIDERS);
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<_SpecialWitchEntity> getFactory() { return _SpecialWitchEntity::new; }
    
    
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
    public void finalizeVariantSpawn( ServerLevelAccessor level, DifficultyInstance difficulty, @Nullable MobSpawnType spawnType,
                                     @Nullable SpawnGroupData groupData ) { }
    
    /** Called when this entity successfully damages a target to apply on-hit effects. */
    @Override
    public void doEnchantDamageEffects( LivingEntity attacker, Entity target ) {
        if( target instanceof LivingEntity ) onVariantAttack( (LivingEntity) target );
        super.doEnchantDamageEffects( attacker, target );
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @SuppressWarnings( "unused" ) // Not normally used for witches
    protected void onVariantAttack( LivingEntity target ) { }
    
    /** Called to attack the target with a ranged attack. */
    @Override
    public void performRangedAttack( LivingEntity target, float damageMulti ) {
        if( isDrinkingPotion() ) return;
        
        final Vec3 vTarget = target.getDeltaMovement();
        final double dX = target.getX() + vTarget.x - getX();
        final double dY = target.getEyeY() - 1.1 - getY();
        final double dZ = target.getZ() + vTarget.z - getZ();
        final float dH = Mth.sqrt( (float) (dX * dX + dZ * dZ) );
        
        final ItemStack potion = pickThrownPotion( target, damageMulti, dH );
        if( potion.isEmpty() ) return;
        
        final ThrownPotion thrownPotion = new ThrownPotion( level, this );
        thrownPotion.setItem( potion );
        thrownPotion.setXRot(thrownPotion.getXRot() + 20.0F);
        thrownPotion.shoot( dX, dY + (double) (dH * 0.2F), dZ, 0.75F, 8.0F * getSpecialData().getRangedAttackSpread() );
        if( !isSilent() ) {
            level.playSound( null, getX(), getY(), getZ(), SoundEvents.WITCH_THROW, getSoundSource(),
                    1.0F, 0.8F + random.nextFloat() * 0.4F );
        }
        level.addFreshEntity( thrownPotion );
    }
    
    /** @return A throwable potion item depending on the situation. */
    protected ItemStack pickThrownPotion( LivingEntity target, float damageMulti, float distance ) {
        final ItemStack potion;
        
        // Healing an ally
        if( target instanceof Raider ) {
            if( target.getMobType() == MobType.UNDEAD ) {
                potion = makeSplashPotion( Potions.HARMING );
            }
            else if( target.getHealth() <= 4.0F ) {
                potion = makeSplashPotion( Potions.HEALING );
            }
            else {
                potion = makeSplashPotion( Potions.REGENERATION );
            }
            setTarget( null );
            
            // Let the variant change the choice or cancel potion throwing
            return pickVariantSupportPotion( potion, (Raider) target, distance );
        }
        
        // Attack potions
        if( distance >= 8.0F && !target.hasEffect( MobEffects.MOVEMENT_SLOWDOWN ) ) {
            potion = makeSplashPotion( Potions.SLOWNESS );
        }
        else if( target.getHealth() >= 8.0F && !target.hasEffect( MobEffects.POISON ) ) {
            potion = makeSplashPotion( Potions.POISON );
        }
        else if( distance <= 3.0F && !target.hasEffect( MobEffects.WEAKNESS ) && random.nextFloat() < 0.25F ) {
            potion = makeSplashPotion( Potions.WEAKNESS );
        }
        else if( target.getMobType() == MobType.UNDEAD ) {
            potion = makeSplashPotion( Potions.HEALING );
        }
        else {
            potion = makeSplashPotion( Potions.HARMING );
        }
        // Let the variant change the choice or cancel potion throwing
        return pickVariantThrownPotion( potion, target, damageMulti, distance );
    }
    
    /** Override to modify potion support. Return an empty item stack to cancel the potion throw. */
    @SuppressWarnings( "unused" )
    protected ItemStack pickVariantSupportPotion( ItemStack originalPotion, Raider target, float distance ) {
        return originalPotion;
    }
    
    /** Override to modify potion attacks. Return an empty item stack to cancel the potion throw. */
    protected ItemStack pickVariantThrownPotion( ItemStack originalPotion, LivingEntity target, float damageMulti, float distance ) {
        return originalPotion;
    }
    
    /** Called each tick while this witch is capable of using a potion on itself. */
    protected void tryUsingPotion() {
        if( random.nextFloat() < 0.15F && isEyeInFluid( FluidTags.WATER ) && !hasEffect( MobEffects.WATER_BREATHING ) ) {
            usePotion( makePotion( Potions.WATER_BREATHING ) );
        }
        else if( random.nextFloat() < 0.15F && (isOnFire() || getLastDamageSource() != null && getLastDamageSource().isFire()) &&
                !hasEffect( MobEffects.FIRE_RESISTANCE ) ) {
            usePotion( makePotion( Potions.FIRE_RESISTANCE ) );
        }
        else if( random.nextFloat() < 0.05F && getHealth() < getMaxHealth() ) {
            usePotion( makePotion( getMobType() == MobType.UNDEAD ? Potions.HARMING : Potions.HEALING ) );
        }
        else if( random.nextFloat() < 0.5F && getTarget() != null && !hasEffect( MobEffects.MOVEMENT_SPEED ) &&
                getTarget().distanceToSqr( this ) > 121.0 ) {
            usePotion( MobFamily.WITCH.config.WITCHES.useSplashSwiftness.get() ? makeSplashPotion( Potions.SWIFTNESS ) :
                    makePotion( Potions.SWIFTNESS ) );
        }
        else {
            tryVariantUsingPotion();
        }
    }
    
    /** Override to add additional potions this witch can drink if none of the base potions are chosen. */
    protected void tryVariantUsingPotion() { }
    
    /** Override to save data to this entity's NBT data. */
    public void addVariantSaveData( CompoundTag saveTag ) { }
    
    /** Override to load data from this entity's NBT data. */
    public void readVariantSaveData( CompoundTag saveTag ) { }
    
    
    //--------------- Family-Specific Implementations ----------------
    
    /** The speed penalty to apply while drinking. */
    private static final AttributeModifier DRINKING_SPEED_PENALTY = new AttributeModifier( UUID.fromString( "5CD17E52-A79A-43D3-A529-90FDE04B181E" ),
            "Drinking speed penalty", -0.25, AttributeModifier.Operation.ADDITION );
    
    /** The parameter for special mob render scale. */
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId( _SpecialWitchEntity.class, EntityDataSerializers.FLOAT );
    
    /** Used to prevent vanilla code from handling potion-drinking. */
    private boolean fakeDrinkingPotion;
    
    /** Ticks until this witch finishes drinking. */
    protected int potionDrinkTimer;
    /** Ticks until this witch can use another potion on itself. */
    protected int potionUseCooldownTimer;
    
    /** While the witch is drinking a potion, it stores its 'actual' held item here. */
    public ItemStack sheathedItem = ItemStack.EMPTY;
    
    public _SpecialWitchEntity( EntityType<? extends _SpecialWitchEntity> entityType, Level level ) {
        super( entityType, level );
        usingTime = Integer.MAX_VALUE; // Effectively disable vanilla witch potion drinking logic in combo with "fake drinking"
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
            AIHelper.removeGoals( goalSelector, RangedAttackGoal.class );
            goalSelector.addGoal( 2, new RangedAttackGoal( this, getSpecialData().getRangedWalkSpeed(),
                    getSpecialData().getRangedAttackCooldown(), getSpecialData().getRangedAttackMaxRange() ) );
        }
    }
    
    /** Called each AI tick to update potion-drinking behavior. */
    public void drinkPotionUpdate() {
        potionUseCooldownTimer--;
        
        if( isDrinkingPotion() ) {
            // Complete potion drinking
            if( potionDrinkTimer-- <= 0 ) {
                final ItemStack drinkingItem = getMainHandItem();
                usePotion( ItemStack.EMPTY );
                
                if( drinkingItem.getItem() == Items.POTION ) {
                    final List<MobEffectInstance> effects = PotionUtils.getMobEffects( drinkingItem );
                    for( MobEffectInstance effect : effects ) {
                        addEffect( new MobEffectInstance( effect ) );
                    }
                }
            }
        }
        else if( potionUseCooldownTimer <= 0 ) {
            tryUsingPotion();
        }
    }
    
    /** Have this witch use the potion item on itself, or stop drinking if the given 'potion' is an empty stack. */
    public void usePotion( ItemStack potion ) {
        // Cancel any current drinking before using a new potion so we don't accidentally delete the sheathed item
        if( isDrinkingPotion() && !potion.isEmpty() ) usePotion( ItemStack.EMPTY );
        
        if( potion.isEmpty() ) {
            // Cancel drinking the current potion and re-equip the sheathed item
            if( isDrinkingPotion() ) {
                setUsingItem( false );
                potionDrinkTimer = 0;
                
                final AttributeInstance attribute = getAttribute( Attributes.MOVEMENT_SPEED );
                if( attribute != null ) attribute.removeModifier( DRINKING_SPEED_PENALTY );
                
                setItemSlot( EquipmentSlot.MAINHAND, sheathedItem );
                sheathedItem = ItemStack.EMPTY;
            }
        }
        else if( potion.getItem() == Items.POTION ) {
            // It is a normal potion, start drinking and sheathe the held item
            sheathedItem = getMainHandItem();
            
            setItemSlot( EquipmentSlot.MAINHAND, potion );
            setUsingItem( true );
            potionDrinkTimer = getMainHandItem().getUseDuration();
            
            if( !isSilent() ) {
                level.playSound( null, getX(), getY(), getZ(), SoundEvents.WITCH_DRINK, getSoundSource(),
                        1.0F, 0.8F + random.nextFloat() * 0.4F );
            }
            
            final AttributeInstance attribute = getAttribute( Attributes.MOVEMENT_SPEED );
            if( attribute != null ) {
                attribute.removeModifier( DRINKING_SPEED_PENALTY );
                attribute.addTransientModifier( DRINKING_SPEED_PENALTY );
            }
        }
        else if( potion.getItem() == Items.SPLASH_POTION || potion.getItem() == Items.LINGERING_POTION ) {
            // It is a splash or lingering potion, throw it straight down to apply to self
            potionUseCooldownTimer = 40;
            
            final ThrownPotion thrownPotion = new ThrownPotion( level, this );
            thrownPotion.setItem( potion );
            thrownPotion.setXRot(thrownPotion.getXRot() + 20.0F);
            thrownPotion.shoot( 0.0, -1.0, 0.0, 0.2F, 0.0F );
            if( !isSilent() ) {
                level.playSound( null, getX(), getY(), getZ(), SoundEvents.WITCH_THROW, getSoundSource(),
                        1.0F, 0.8F + random.nextFloat() * 0.4F );
            }
            level.addFreshEntity( thrownPotion );
        }
        else {
            SpecialMobs.LOG.warn( "Witch {} attempted to use '{}' as a potion! Gross!", getClass().getSimpleName(), potion );
        }
    }
    
    /** @return A new regular potion with standard effects. */
    public ItemStack makePotion( Potion type ) { return newPotion( Items.POTION, type ); }
    
    /** @return A new regular potion with custom effects. */
    @SuppressWarnings( "unused" )
    public ItemStack makePotion( Collection<MobEffectInstance> effects ) { return newPotion( Items.POTION, effects ); }
    
    /** @return A new splash potion with standard effects. */
    public ItemStack makeSplashPotion( Potion type ) { return newPotion( Items.SPLASH_POTION, type ); }
    
    /** @return A new splash potion on self with custom effects. */
    public ItemStack makeSplashPotion( Collection<MobEffectInstance> effects ) { return newPotion( Items.SPLASH_POTION, effects ); }
    
    /** @return A new lingering splash potion with standard effects. */
    public ItemStack makeLingeringPotion( Potion type ) { return newPotion( Items.LINGERING_POTION, type ); }
    
    /** @return A new lingering splash potion with custom effects. */
    @SuppressWarnings( "unused" )
    public ItemStack makeLingeringPotion( Collection<MobEffectInstance> effects ) { return newPotion( Items.LINGERING_POTION, effects ); }
    
    /** @return A new potion with standard effects. */
    private ItemStack newPotion( ItemLike item, Potion type ) {
        return PotionUtils.setPotion( new ItemStack( item ), type );
    }
    
    /** @return A new potion with custom effects. */
    private ItemStack newPotion( ItemLike item, Collection<MobEffectInstance> effects ) {
        return PotionUtils.setCustomEffects( new ItemStack( item ), effects );
    }
    
    
    //--------------- ISpecialMob Implementation ----------------
    
    private SpecialMobData<_SpecialWitchEntity> specialData;
    
    /** @return This mob's special data. */
    @Override
    public SpecialMobData<_SpecialWitchEntity> getSpecialData() { return specialData; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends _SpecialWitchEntity> getSpecies() { return SPECIES; }
    
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
    public void finalizeSpecialSpawn( ServerLevelAccessor level, DifficultyInstance difficulty, @Nullable MobSpawnType spawnReason,
                                      @Nullable SpawnGroupData groupData ) {
        finalizeVariantSpawn( level, difficulty, spawnReason, groupData );
    }
    
    
    //--------------- SpecialMobData Hooks ----------------
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        if( !level.isClientSide() && isAlive() ) {
            drinkPotionUpdate();
            fakeDrinkingPotion = true;
        }
        super.aiStep();
        getSpecialData().tick();
    }
    
    /** @return True if this witch is currently drinking a potion. */
    @Override
    public boolean isDrinkingPotion() {
        // Effectively disable vanilla witch potion drinking logic in combo with "infinite using time"
        if( fakeDrinkingPotion ) {
            fakeDrinkingPotion = false;
            return true;
        }
        return super.isDrinkingPotion();
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
        
        final CompoundTag itemTag = new CompoundTag();
        if( !sheathedItem.isEmpty() ) sheathedItem.save( itemTag );
        saveTag.put( References.TAG_SHEATHED_ITEM, itemTag );
        saveTag.putShort( References.TAG_POTION_USE_TIME, (short) potionDrinkTimer );
        
        getSpecialData().writeToNBT( saveTag );
        addVariantSaveData( saveTag );
    }
    
    /** Loads data from this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void readAdditionalSaveData( CompoundTag tag ) {
        super.readAdditionalSaveData( tag );
        
        final CompoundTag saveTag = SpecialMobData.getSaveLocation( tag );
        
        if( saveTag.contains( References.TAG_SHEATHED_ITEM, References.NBT_TYPE_COMPOUND ) )
            sheathedItem = ItemStack.of( saveTag.getCompound( References.TAG_SHEATHED_ITEM ) );
        if( saveTag.contains( References.TAG_POTION_USE_TIME, References.NBT_TYPE_NUMERICAL ) )
            potionDrinkTimer = saveTag.getShort( References.TAG_POTION_USE_TIME );
        
        getSpecialData().readFromNBT( saveTag );
        readVariantSaveData( saveTag );
        
        recalculateAttackGoal();
    }
}