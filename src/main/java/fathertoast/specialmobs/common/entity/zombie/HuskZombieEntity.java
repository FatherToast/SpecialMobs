package fathertoast.specialmobs.common.entity.zombie;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.HuskZombieSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.event.NaturalSpawnManager;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import java.util.function.Predicate;

@SpecialMob
public class HuskZombieEntity extends _SpecialZombieEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<HuskZombieEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xE6CC94 ).weight( BestiaryInfo.DefaultWeight.LOW ).theme( BestiaryInfo.Theme.DESERT )
                .vanillaTextureBaseOnly( "textures/entity/zombie/husk.png" )
                .size( 1.0625F, 0.6F, 1.95F )
                .addExperience( 1 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( MobFamily.Species<?> species ) {
        return new HuskZombieSpeciesConfig( species, DEFAULT_BOW_CHANCE, DEFAULT_SHIELD_CHANCE );
    }
    
    /** @return This entity's species config. */
    @Override
    public HuskZombieSpeciesConfig getConfig() { return (HuskZombieSpeciesConfig) getSpecies().config; }
    
    @SpecialMob.AttributeSupplier
    public static AttributeSupplier.Builder createAttributes() { return Husk.createAttributes(); }
    
    @SpecialMob.SpawnPlacementRegistrar
    public static void registerSpeciesSpawnPlacement( MobFamily.Species<? extends HuskZombieEntity> species ) {
        NaturalSpawnManager.registerSpawnPlacement( species, HuskZombieEntity::checkSpeciesSpawnRules );
    }
    
    /**
     * We cannot call the actual husk method because our husk variant does not extend the vanilla husk.
     *
     * @see net.minecraft.world.entity.monster.Husk#checkHuskSpawnRules(EntityType, ServerLevelAccessor, MobSpawnType, BlockPos, RandomSource) 
     */
    public static boolean checkSpeciesSpawnRules( EntityType<? extends HuskZombieEntity> type, ServerLevelAccessor world,
                                                  MobSpawnType spawnType, BlockPos pos, RandomSource random ) {
        return NaturalSpawnManager.checkSpawnRulesDefault( type, world, spawnType, pos, random ) &&
                (spawnType == MobSpawnType.SPAWNER || world.canSeeSky( pos ));
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Husk",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        loot.addLootTable( "main", EntityType.HUSK.getDefaultLootTable() );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<HuskZombieEntity> getVariantFactory() { return HuskZombieEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends HuskZombieEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public HuskZombieEntity( EntityType<? extends _SpecialZombieEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        MobHelper.applyEffect( target, MobEffects.HUNGER );
    }
    
    /** Override to modify this entity's ranged attack projectile. */
    @Override
    protected AbstractArrow getVariantArrow( AbstractArrow arrow, ItemStack arrowItem, float damageMulti ) {
        return MobHelper.tipArrow( arrow, MobEffects.HUNGER );
    }
    
    /** Returns true if the species is not a husk and not damaged by water. */
    private static final Predicate<MobFamily.Species<?>> HUSK_CONVERSION_SELECTOR =
            ( species ) -> species != SPECIES && !species.config.GENERAL.isDamagedByWater.get();
    
    /** Performs this zombie's drowning conversion. */
    @Override
    protected void doUnderWaterConversion() {
        convertToZombieType( getVariantConversionType() );
        References.LevelEvent.HUSK_CONVERTED_TO_ZOMBIE.play( this );
    }
    
    /** Override to change the entity this converts to when drowned. */
    @Override
    protected EntityType<? extends Zombie> getVariantConversionType() {
        // Select a random non-husk, non-water-sensitive zombie; defaults to a normal zombie
        return getConfig().HUSK.convertVariantChance.rollChance( random, level, blockPosition() ) ?
                MobFamily.ZOMBIE.nextVariant( level, blockPosition(), HUSK_CONVERSION_SELECTOR, _SpecialZombieEntity.SPECIES ).entityType.get() :
                _SpecialZombieEntity.SPECIES.entityType.get();
    }
    
    
    //--------------- Husk Implementations ----------------
    
    /** @return True if this zombie burns in sunlight. */
    @Override
    protected boolean isSunSensitive() { return false; }
    
    /** @return The sound this entity makes idly. */
    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.HUSK_AMBIENT; }
    
    /** @return The sound this entity makes when damaged. */
    @Override
    protected SoundEvent getHurtSound( DamageSource source ) { return SoundEvents.HUSK_HURT; }
    
    /** @return The sound this entity makes when killed. */
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.HUSK_DEATH; }
    
    /** @return The sound this entity makes while walking. */
    @Override
    protected SoundEvent getStepSound() { return SoundEvents.HUSK_STEP; }
}