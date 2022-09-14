package fathertoast.specialmobs.common.entity.creeper;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.block.UnderwaterSilverfishBlock;
import fathertoast.specialmobs.common.config.species.DrowningCreeperSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.config.util.EnvironmentEntry;
import fathertoast.specialmobs.common.config.util.EnvironmentList;
import fathertoast.specialmobs.common.config.util.environment.biome.BiomeCategory;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.entity.ai.AmphibiousMovementController;
import fathertoast.specialmobs.common.entity.ai.IAmphibiousMob;
import fathertoast.specialmobs.common.entity.ai.goal.AmphibiousGoToWaterGoal;
import fathertoast.specialmobs.common.entity.ai.goal.AmphibiousSwimUpGoal;
import fathertoast.specialmobs.common.event.NaturalSpawnManager;
import fathertoast.specialmobs.common.util.ExplosionHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootEntryItemBuilder;
import fathertoast.specialmobs.datagen.loot.LootPoolBuilder;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.passive.fish.PufferfishEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.SwimmerPathNavigator;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

import java.util.Random;

@SpecialMob
public class DrowningCreeperEntity extends _SpecialCreeperEntity implements IAmphibiousMob {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<DrowningCreeperEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x2D41F4 ).weight( BestiaryInfo.DefaultWeight.LOW ).theme( BestiaryInfo.Theme.WATER )
                .uniqueTextureWithEyes()
                .addExperience( 2 ).drownImmune().fluidPushImmune()
                .addToAttribute( Attributes.MAX_HEALTH, 10.0 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( MobFamily.Species<?> species ) {
        SpeciesConfig.NEXT_NATURAL_SPAWN_CHANCE_EXCEPTIONS = new EnvironmentList(
                EnvironmentEntry.builder( 0.06F ).inBiome( Biomes.WARM_OCEAN ).build(),
                EnvironmentEntry.builder( 0.06F ).inBiome( Biomes.DEEP_WARM_OCEAN ).build(),
                EnvironmentEntry.builder( 0.06F ).inBiomeCategory( BiomeCategory.RIVER ).build(),
                EnvironmentEntry.builder( 0.02F ).inBiomeCategory( BiomeCategory.OCEAN ).belowSeaDepths().build(),
                EnvironmentEntry.builder( 0.0F ).inBiomeCategory( BiomeCategory.OCEAN ).build() );
        return new DrowningCreeperSpeciesConfig( species, false, false, false,
                0.25, 2, 4 );
    }
    
    /** @return This entity's species config. */
    @Override
    public DrowningCreeperSpeciesConfig getConfig() { return (DrowningCreeperSpeciesConfig) getSpecies().config; }
    
    @SpecialMob.SpawnPlacementRegistrar
    public static void registerSpeciesSpawnPlacement( MobFamily.Species<? extends DrowningCreeperEntity> species ) {
        NaturalSpawnManager.registerSpawnPlacement( species, EntitySpawnPlacementRegistry.PlacementType.IN_WATER,
                DrowningCreeperEntity::checkSpeciesSpawnRules );
    }
    
    public static boolean checkSpeciesSpawnRules( EntityType<? extends DrowningCreeperEntity> type, IServerWorld world,
                                                  SpawnReason reason, BlockPos pos, Random random ) {
        final Biome.Category biomeCategory = world.getBiome( pos ).getBiomeCategory();
        if( biomeCategory == Biome.Category.OCEAN || biomeCategory == Biome.Category.RIVER ) {
            return NaturalSpawnManager.checkSpawnRulesWater( type, world, reason, pos, random );
        }
        return NaturalSpawnManager.checkSpawnRulesDefault( type, world, reason, pos, random );
    }
    
    /** @return True if this entity's position is currently obstructed. */
    @Override
    public boolean checkSpawnObstruction( IWorldReader world ) { return world.isUnobstructed( this ); }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Drowning Creeper",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addPool( new LootPoolBuilder( "common" )
                .addEntry( new LootEntryItemBuilder( Items.COD ).setCount( 0, 2 ).addLootingBonus( 0, 1 ).smeltIfBurning().toLootEntry() )
                .toLootPool() );
        loot.addPool( new LootPoolBuilder( "semicommon" )
                .addEntry( new LootEntryItemBuilder( Items.SALMON ).setCount( 0, 1 ).addLootingBonus( 0, 1 ).smeltIfBurning().toLootEntry() )
                .toLootPool() );
        loot.addUncommonDrop( "uncommon", Items.GOLD_NUGGET, Items.PRISMARINE_SHARD, Items.PRISMARINE_CRYSTALS );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<DrowningCreeperEntity> getVariantFactory() { return DrowningCreeperEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends DrowningCreeperEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** The maximum number of pufferfish spawned on explosion. */
    private int pufferfish;
    
    public DrowningCreeperEntity( EntityType<? extends _SpecialCreeperEntity> entityType, World world ) {
        super( entityType, world );
        moveControl = new AmphibiousMovementController<>( this );
        waterNavigation = new SwimmerPathNavigator( this, world );
        groundNavigation = new GroundPathNavigator( this, world );
        maxUpStep = 1.0F;
        setPathfindingMalus( PathNodeType.WATER, PathNodeType.WALKABLE.getMalus() );
        
        pufferfish = getConfig().DROWNING.puffPuffs.next( random );
    }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        AIHelper.removeGoals( goalSelector, SwimGoal.class );
        AIHelper.insertGoal( goalSelector, 5, new AmphibiousGoToWaterGoal( this, 1.0 ).alwaysEnabled() );
        AIHelper.insertGoal( goalSelector, 6, new AmphibiousSwimUpGoal<>( this, 1.0 ) );
        AIHelper.replaceWaterAvoidingRandomWalking( this, 0.8 );
    }
    
    /** Override to change this creeper's explosion power multiplier. */
    @Override
    protected float getVariantExplosionPower( float radius ) { return super.getVariantExplosionPower( radius ) + 3.0F; }
    
    /** Override to change this creeper's explosion. */
    @Override
    protected void makeVariantExplosion( float explosionPower ) {
        final Explosion.Mode explosionMode = ExplosionHelper.getMode( this );
        final ExplosionHelper explosion = new ExplosionHelper( this,
                explosionMode == Explosion.Mode.NONE ? explosionPower : 1.0F, explosionMode, false );
        if( !explosion.initializeExplosion() ) return;
        explosion.finalizeExplosion();
        
        if( explosionMode == Explosion.Mode.NONE ) return;
        
        final UnderwaterSilverfishBlock.Type mainType = UnderwaterSilverfishBlock.Type.next( random );
        final UnderwaterSilverfishBlock.Type rareType = UnderwaterSilverfishBlock.Type.next( random );
        final BlockState mainCoral = mainType.hostBlock().defaultBlockState();
        final BlockState rareCoral = rareType.hostBlock().defaultBlockState();
        final BlockState mainInfestedCoral = mainType.block().defaultBlockState();
        final BlockState rareInfestedCoral = rareType.block().defaultBlockState();
        
        final BlockState water = Blocks.WATER.defaultBlockState();
        final BlockState seaPickle = Blocks.SEA_PICKLE.defaultBlockState().setValue( BlockStateProperties.WATERLOGGED, true );
        final BlockState seaGrass = Blocks.SEAGRASS.defaultBlockState();
        final int radius = (int) Math.floor( explosionPower );
        final int radiusSq = radius * radius;
        final int rMinusOneSq = (radius - 1) * (radius - 1);
        final BlockPos center = new BlockPos( explosion.getPos() );
        
        // Track how many pufferfish have been spawned so we don't spawn a bunch of them
        if( pufferfish > 0 ) spawnPufferfish( center.above( 1 ) );
        int pufferCount = 1;
        
        for( int y = -radius; y <= radius; y++ ) {
            for( int x = -radius; x <= radius; x++ ) {
                for( int z = -radius; z <= radius; z++ ) {
                    final int distSq = x * x + y * y + z * z;
                    
                    if( distSq <= radiusSq ) {
                        final BlockPos pos = center.offset( x, y, z );
                        final BlockState stateAtPos = level.getBlockState( pos );
                        
                        if( stateAtPos.getMaterial().isReplaceable() || stateAtPos.is( BlockTags.LEAVES ) ) {
                            if( distSq <= rMinusOneSq ) {
                                // Water fill
                                final float fillChoice = random.nextFloat();
                                
                                if( fillChoice < 0.1F && seaPickle.canSurvive( level, pos ) ) {
                                    MobHelper.placeBlock( this, pos, seaPickle );
                                }
                                else if( fillChoice < 0.3F && seaGrass.canSurvive( level, pos ) ) {
                                    MobHelper.placeBlock( this, pos, seaGrass );
                                }
                                else {
                                    // Water fill
                                    MobHelper.placeBlock( this, pos, water );
                                    
                                    if( random.nextFloat() < 0.0075F && pufferCount < pufferfish ) {
                                        spawnPufferfish( pos );
                                        pufferCount++;
                                    }
                                }
                            }
                            else if( isCoralSafe( rMinusOneSq, x, y, z ) ) {
                                // Coral casing
                                final boolean infested = getConfig().DROWNING.infestedBlockChance.rollChance( random );
                                MobHelper.placeBlock( this, pos, random.nextFloat() < 0.8F ?
                                        infested ? mainInfestedCoral : mainCoral :
                                        infested ? rareInfestedCoral : rareCoral );
                            }
                        }
                    }
                }
            }
        }
    }
    
    /** Helper method to simplify spawning pufferfish. */
    private void spawnPufferfish( BlockPos pos ) {
        if( !(level instanceof IServerWorld) ) return;
        final PufferfishEntity lePuffPuff = EntityType.PUFFERFISH.create( level );
        if( lePuffPuff != null ) {
            lePuffPuff.setPos( pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5 );
            level.addFreshEntity( lePuffPuff );
        }
    }
    
    /** @return Checks the inner three-ish block distances and returns true if at least one is inside the main radius. */
    @SuppressWarnings( "RedundantIfStatement" ) // The symmetry makes it look better
    private boolean isCoralSafe( int rMinusOneSq, int x, int y, int z ) {
        int distSq;
        if( y != 0 ) {
            final int innerY = y < 0 ? y + 1 : y - 1;
            distSq = x * x + innerY * innerY + z * z;
            if( distSq <= rMinusOneSq ) return true;
        }
        if( x != 0 ) {
            final int innerX = x < 0 ? x + 1 : x - 1;
            distSq = innerX * innerX + y * y + z * z;
            if( distSq <= rMinusOneSq ) return true;
        }
        if( z != 0 ) {
            final int innerZ = z < 0 ? z + 1 : z - 1;
            distSq = x * x + y * y + innerZ * innerZ;
            if( distSq <= rMinusOneSq ) return true;
        }
        return false;
    }
    
    // The below two methods are here to effectively override the private Entity#isInRain to always return true (always wet)
    @Override
    public boolean isInWaterOrRain() { return true; }
    
    @Override
    public boolean isInWaterRainOrBubble() { return true; }
    
    /** Override to save data to this entity's NBT data. */
    @Override
    public void addVariantSaveData( CompoundNBT saveTag ) {
        saveTag.putByte( References.TAG_SUMMONS, (byte) pufferfish );
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundNBT saveTag ) {
        if( saveTag.contains( References.TAG_SUMMONS, References.NBT_TYPE_NUMERICAL ) )
            pufferfish = saveTag.getByte( References.TAG_SUMMONS );
        
        setPathfindingMalus( PathNodeType.WATER, PathNodeType.WALKABLE.getMalus() );
    }
    
    
    //--------------- IAmphibiousMob Implementation ----------------
    
    private final SwimmerPathNavigator waterNavigation;
    private final GroundPathNavigator groundNavigation;
    
    private boolean swimmingUp;
    
    /** Called each tick to update this entity's swimming state. */
    @Override
    public void updateSwimming() {
        if( !level.isClientSide ) {
            if( isEffectiveAi() && isUnderWater() && shouldSwim() ) {
                setNavigatorToSwim();
                setSwimming( true );
            }
            else {
                setNavigatorToGround();
                setSwimming( false );
            }
        }
    }
    
    /** Moves this entity in the desired direction. Input magnitude of < 1 scales down movement speed. */
    @Override
    public void travel( Vector3d input ) {
        if( isEffectiveAi() && isUnderWater() && shouldSwim() ) {
            moveRelative( 0.01F, input );
            move( MoverType.SELF, getDeltaMovement() );
            setDeltaMovement( getDeltaMovement().scale( 0.9 ) );
        }
        else super.travel( input );
    }
    
    /** @return Water drag coefficient. */
    @Override
    protected float getWaterSlowDown() { return 0.9F; }
    
    /** @return True if this mob should use its swimming navigator for its current goal. */
    @Override
    public boolean shouldSwim() {
        if( swimmingUp ) return true;
        final LivingEntity target = getTarget();
        return target != null && target.isInWater();
    }
    
    /** Sets whether this mob should swim upward. */
    @Override
    public void setSwimmingUp( boolean value ) { swimmingUp = value; }
    
    /** @return True if this mob should swim upward. */
    @Override
    public boolean isSwimmingUp() { return swimmingUp; }
    
    /** Sets this mob's current navigator to swimming mode. */
    @Override
    public void setNavigatorToSwim() { navigation = waterNavigation; }
    
    /** Sets this mob's current navigator to ground mode. */
    @Override
    public void setNavigatorToGround() { navigation = groundNavigation; }
}