package fathertoast.specialmobs.common.entity.slime;

import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.value.EnvironmentEntry;
import fathertoast.crust.api.config.common.value.EnvironmentList;
import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.event.NaturalSpawnManager;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.common.ForgeMod;

@SpecialMob
public class BlueberrySlimeEntity extends _SpecialSlimeEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<BlueberrySlimeEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x766BBC ).theme( BestiaryInfo.Theme.WATER )
                .uniqueTextureBaseOnly()
                .addExperience( 1 ).drownImmune().fluidPushImmune()
                .addToAttribute( Attributes.ATTACK_DAMAGE, 2.0 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( ConfigManager manager, MobFamily.Species<?> species ) {
        SpeciesConfig.NEXT_NATURAL_SPAWN_CHANCE_EXCEPTIONS = new EnvironmentList(
                EnvironmentEntry.builder( manager, 0.0F ).atNoMoonLight().build(),
                EnvironmentEntry.builder( manager, 0.04F ).atMaxMoonLight().build(),
                EnvironmentEntry.builder( manager, 0.01F ).belowHalfMoonLight().build(),
                EnvironmentEntry.builder( manager, 0.02F ).atHalfMoonLight().build(),
                EnvironmentEntry.builder( manager, 0.03F ).aboveHalfMoonLight().build() );
        return new SpeciesConfig( manager, species );
    }
    
    @SpecialMob.SpawnPlacementRegistrar
    public static void registerSpeciesSpawnPlacement( MobFamily.Species<? extends BlueberrySlimeEntity> species ) {
        NaturalSpawnManager.registerSpawnPlacement( species, SpawnPlacements.Type.IN_WATER,
                BlueberrySlimeEntity::checkSpeciesSpawnRules );
    }
    
    public static boolean checkSpeciesSpawnRules(EntityType<? extends BlueberrySlimeEntity> type, ServerLevelAccessor level,
                                                 MobSpawnType spawnType, BlockPos pos, RandomSource random ) {
        final Holder<Biome> biome = level.getBiome( pos );
        if( biome.is( BiomeTags.IS_RIVER ) || biome.is( BiomeTags.IS_OCEAN ) ) {
            return NaturalSpawnManager.checkSpawnRulesWater( type, level, spawnType, pos, random );
        }
        return _SpecialSlimeEntity.checkFamilySpawnRules( type, level, spawnType, pos, random );
    }
    
    /** @return True if this entity's position is currently obstructed. */
    @Override
    public boolean checkSpawnObstruction( LevelReader level ) { return level.isUnobstructed( this ); }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Blueberry Slime",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addRareDrop( "rare", Items.GOLD_NUGGET, Items.PRISMARINE_SHARD, Items.PRISMARINE_CRYSTALS );
        loot.addUncommonDrop( "uncommon", Items.BLUE_DYE );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<BlueberrySlimeEntity> getVariantFactory() { return BlueberrySlimeEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends BlueberrySlimeEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public BlueberrySlimeEntity( EntityType<? extends _SpecialSlimeEntity> entityType, Level level ) {
        super( entityType, level );
        setPathfindingMalus( BlockPathTypes.WATER, BlockPathTypes.WALKABLE.getMalus() );
    }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        AIHelper.removeGoals( goalSelector, 1 ); // SlimeEntity.FloatGoal
    }
    
    /** Called each tick to update this entity. */
    @Override
    public void tick() {
        super.tick();
        
        // Hacky way of attacking submerged targets; drop down on them when directly above
        double floatAccel = 0.06;
        final LivingEntity target = getTarget();
        if( target != null && target.getY( 0.5 ) < getY( 0.5 ) ) {
            //
            final double dX = target.getX() - getX();
            final double dZ = target.getZ() - getZ();
            final float range = (target.getBbWidth() + getBbWidth() + 0.1F) / 2.0F;
            if( dX * dX + dZ * dZ < range * range )
                floatAccel = -0.12;
        }
        if( tickCount > 1 && getFluidTypeHeight( ForgeMod.WATER_TYPE.get() ) > 0.0 ) {
            if( !CollisionContext.of( this ).isAbove( LiquidBlock.STABLE_SHAPE, blockPosition(), true ) ||
                    level().getFluidState( blockPosition().above() ).is( FluidTags.WATER ) ) {
                setDeltaMovement( getDeltaMovement().scale( 0.5 ).add( 0.0, floatAccel, 0.0 ) );
            }
        }
    }
    
    // The below two methods are here to effectively override the private Entity#isInRain to always return true (always wet)
    @Override
    public boolean isInWaterOrRain() { return true; }
    
    @Override
    public boolean isInWaterRainOrBubble() { return true; }
    
    /** @return Water drag coefficient. */
    @Override
    protected float getWaterSlowDown() { return 0.9F; }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundTag saveTag ) {
        setPathfindingMalus( BlockPathTypes.WATER, BlockPathTypes.WALKABLE.getMalus() );
    }
    
    /** @return This slime's particle type for jump effects. */
    @Override
    protected ParticleOptions getParticleType() { return ParticleTypes.SPLASH; }
}