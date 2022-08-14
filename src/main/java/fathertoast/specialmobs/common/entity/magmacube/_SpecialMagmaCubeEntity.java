package fathertoast.specialmobs.common.entity.magmacube;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.SpecialMobData;
import fathertoast.specialmobs.common.event.NaturalSpawnManager;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.monster.MagmaCubeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SnowballEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

@SpecialMob
public class _SpecialMagmaCubeEntity extends MagmaCubeEntity implements ISpecialMob<_SpecialMagmaCubeEntity> {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<_SpecialMagmaCubeEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xFCFC00 )
                .vanillaTextureBaseOnly( "textures/entity/slime/magmacube.png" )
                .experience( 0 );
    }
    
    @SpecialMob.AttributeSupplier
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MagmaCubeEntity.createAttributes(); // Slimes define their attributes elsewhere based on size
    }
    
    @SpecialMob.SpawnPlacementRegistrar
    public static void registerSpawnPlacement( MobFamily.Species<? extends _SpecialMagmaCubeEntity> species ) {
        NaturalSpawnManager.registerSpawnPlacement( species, _SpecialMagmaCubeEntity::checkFamilySpawnRules );
    }
    
    public static boolean checkFamilySpawnRules( EntityType<? extends MagmaCubeEntity> type, IServerWorld world,
                                                 SpawnReason reason, BlockPos pos, Random random ) {
        //noinspection unchecked
        return MagmaCubeEntity.checkMagmaCubeSpawnRules( (EntityType<MagmaCubeEntity>) type, world, reason, pos, random ) &&
                NaturalSpawnManager.checkSpawnRulesConfigured( type, world, reason, pos, random );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Magma Cube",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void addBaseLoot( LootTableBuilder loot ) {
        loot.addLootTable( "main", EntityType.MAGMA_CUBE.getDefaultLootTable() );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<_SpecialMagmaCubeEntity> getFactory() { return _SpecialMagmaCubeEntity::new; }
    
    
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
    private static final DataParameter<Float> SCALE = EntityDataManager.defineId( _SpecialMagmaCubeEntity.class, DataSerializers.FLOAT );
    
    /** Used to reset slimes' attributes to their freshly spawned state so attribute adjustments may be reapplied on size change. */
    private static ListNBT magmaCubeAttributeSnapshot;
    
    private static ListNBT getAttributeSnapshot() {
        if( magmaCubeAttributeSnapshot == null )
            magmaCubeAttributeSnapshot = new AttributeModifierManager( createAttributes().build() ).save();
        return magmaCubeAttributeSnapshot;
    }
    
    private int slimeExperienceValue = 0;
    
    public _SpecialMagmaCubeEntity( EntityType<? extends _SpecialMagmaCubeEntity> entityType, World world ) {
        super( entityType, world );
        getSpecialData().initialize();
    }
    
    /** Called from the Entity.class constructor to define data watcher variables. */
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        specialData = new SpecialMobData<>( this, SCALE );
    }
    
    /** Sets this slime's size, optionally resetting its health to max. */
    @Override
    protected void setSize( int size, boolean resetHealth ) {
        // We must reset all attributes and reapply changes since slimes set attribute base values on size change
        getAttributes().load( getAttributeSnapshot() );
        super.setSize( size, resetHealth );
        getSpecies().config.GENERAL.attributeChanges.apply( this );
        
        if( resetHealth ) setHealth( getMaxHealth() );
        setExperience( getExperience() ); // Update for new size
    }
    
    
    //--------------- ISpecialMob Implementation ----------------
    
    private SpecialMobData<_SpecialMagmaCubeEntity> specialData;
    
    /** @return This mob's special data. */
    @Override
    public SpecialMobData<_SpecialMagmaCubeEntity> getSpecialData() { return specialData; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends _SpecialMagmaCubeEntity> getSpecies() { return SPECIES; }
    
    /** @return The experience that should be dropped by this entity. */
    @Override
    public final int getExperience() { return slimeExperienceValue; } // Slime base xp
    
    /** Sets the experience that should be dropped by this entity. */
    @Override
    public final void setExperience( int xp ) {
        slimeExperienceValue = xp;
        xpReward = getSize() + xp;
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
        finalizeVariantSpawn( world, difficulty, spawnReason, groupData );
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
    }
}