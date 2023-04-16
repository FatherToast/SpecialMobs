package fathertoast.specialmobs.common.entity.creeper;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.block.MeltingIceBlock;
import fathertoast.specialmobs.common.config.species.SnowCreeperSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.entity.ai.FluidPathNavigator;
import fathertoast.specialmobs.common.entity.skeleton.StraySkeletonEntity;
import fathertoast.specialmobs.common.util.ExplosionHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

@SpecialMob
public class SnowCreeperEntity extends _SpecialCreeperEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<SnowCreeperEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xE8F8F8 ).weight( BestiaryInfo.DefaultWeight.LOW ).theme( BestiaryInfo.Theme.ICE )
                .uniqueTextureWithEyes()
                .addExperience( 2 ).effectImmune( MobEffects.MOVEMENT_SLOWDOWN )
                .addToAttribute( Attributes.MAX_HEALTH, 10.0 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( MobFamily.Species<?> species ) {
        return new SnowCreeperSpeciesConfig( species, false, false, false,
                0.33 );
    }
    
    /** @return This entity's species config. */
    @Override
    public SnowCreeperSpeciesConfig getConfig() { return (SnowCreeperSpeciesConfig) getSpecies().config; }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Snow Creeper",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addClusterDrop( "common", Items.SNOWBALL );
        loot.addUncommonDrop( "uncommon", Blocks.PACKED_ICE );
        loot.addRareDrop( "rare", Blocks.BLUE_ICE );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<SnowCreeperEntity> getVariantFactory() { return SnowCreeperEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends SnowCreeperEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public SnowCreeperEntity( EntityType<? extends _SpecialCreeperEntity> entityType, Level level ) {
        super( entityType, level );
        setPathfindingMalus( BlockPathTypes.WATER, BlockPathTypes.WALKABLE.getMalus() );
    }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        AIHelper.replaceWaterAvoidingRandomWalking( this, 0.8 );
    }
    
    /** @return A new path navigator for this entity to use. */
    @Override
    protected PathNavigation createNavigation(Level level ) {
        return new FluidPathNavigator( this, level, true, false );
    }

    /** @return Whether this entity can stand on a particular type of fluid. */
    @Override
    public boolean canStandOnFluid( FluidState fluid ) { return fluid.is( FluidTags.WATER ); }
    
    /** Called each tick to update this entity. */
    @Override
    public void tick() {
        super.tick();
        MobHelper.floatInFluid( this, 0.06, FluidTags.WATER );
        MobHelper.hopOnFluid( this );
    }
    
    /** Called whenever this entity's block position changes. */
    @Override
    protected void onChangedBlock( BlockPos pos ) {
        super.onChangedBlock( pos );
        MobHelper.updateFrostWalker( this, pos );
    }
    
    /** Override to change this creeper's explosion power multiplier. */
    @Override
    protected float getVariantExplosionPower( float radius ) { return super.getVariantExplosionPower( radius ) + 3.0F; }
    
    /** Override to change this creeper's explosion. */
    @Override
    protected void makeVariantExplosion( float explosionPower ) {
        final Explosion.BlockInteraction explosionMode = ExplosionHelper.getMode( this );
        final ExplosionHelper explosion = new ExplosionHelper( this,
                explosionMode == Explosion.BlockInteraction.NONE ? explosionPower : 2.0F, false, false );
        if( !explosion.initializeExplosion() ) return;
        explosion.finalizeExplosion();
        
        final int radius = (int) Math.floor( explosionPower );
        final BlockPos center = new BlockPos( explosion.getPos() );
        
        if( explosionMode != Explosion.BlockInteraction.NONE ) {
            final boolean snowGlobe = isUnderWater() || random.nextDouble() < getConfig().SNOW.snowGlobeChance.get();
            
            final int radiusSq = radius * radius;
            final int rMinusOneSq = (radius - 1) * (radius - 1);
            
            for( int y = -radius; y <= radius; y++ ) {
                for( int x = -radius; x <= radius; x++ ) {
                    for( int z = -radius; z <= radius; z++ ) {
                        final int distSq = x * x + y * y + z * z;
                        
                        if( distSq <= radiusSq ) {
                            final BlockPos pos = center.offset( x, y, z );
                            
                            // Freeze top layer of water sources and frosted ice within affected volume
                            final BlockState block = level.getBlockState( pos );
                            if( block.is( Blocks.FROSTED_ICE ) || block.getBlock() == Blocks.WATER && block.getValue( LiquidBlock.LEVEL ) == 0 ) {
                                final BlockState blockAbove = level.getBlockState( pos.above() );
                                if( !blockAbove.getMaterial().blocksMotion() && !blockAbove.getFluidState().is( FluidTags.WATER ) &&
                                        MobHelper.placeBlock( this, pos, MeltingIceBlock.getState( level, pos ) ) ) {
                                    MeltingIceBlock.scheduleFirstTick( level, pos, random );
                                }
                            }
                            
                            if( distSq > rMinusOneSq ) {
                                if( snowGlobe ) {
                                    // Create spherical shell of ice
                                    if( level.getBlockState( pos ).getMaterial().isReplaceable() &&
                                            MobHelper.placeBlock( this, pos, MeltingIceBlock.getState( level, pos ) ) ) {
                                        MeltingIceBlock.scheduleFirstTick( level, pos, random );
                                    }
                                }
                                else if( y == 0 ) {
                                    // Create ice wall
                                    placePillar( pos, radius );
                                }
                            }
                        }
                    }
                }
            }
        }
        
        final int strays = radius / 2;
        for( int count = 0; count < strays; count++ ) {
            for( int attempt = 0; attempt < 8; attempt++ ) {
                if( trySpawnStray( center, radius ) ) break;
            }
        }
    }
    
    /** Try to place an ice pillar at the location. */
    private void placePillar( BlockPos pos, int radius ) {
        final BlockPos.MutableBlockPos currentPos = pos.mutable();
        if( shouldReplace( currentPos ) ) findGroundBelow( currentPos, radius );
        else if( findGroundAbove( currentPos, radius ) ) return;
        
        final int maxY = Math.min( currentPos.getY() + 4, level.getMaxBuildHeight() - 2 );
        int height = -2; // This is minimum pillar height
        if( pos.getY() > currentPos.getY() ) height -= (pos.getY() - currentPos.getY()) / 2;
        
        while( currentPos.getY() < maxY && shouldReplace( currentPos ) ) {
            if( MobHelper.placeBlock( this, currentPos, MeltingIceBlock.getState( level, currentPos ) ) ) {
                MeltingIceBlock.scheduleFirstTick( level, currentPos, random );
            }
            currentPos.move( 0, 1, 0 );
            
            if( ++height >= 0 && random.nextBoolean() ) break;
        }
    }
    
    /** Attempts to find the ground. Resets the position if none can be found. */
    private void findGroundBelow( BlockPos.MutableBlockPos currentPos, int radius ) {
        final int yI = currentPos.getY();
        final int minY = Math.max( yI - radius, 0 );
        
        while( currentPos.getY() > minY ) {
            currentPos.move( 0, -1, 0 );
            if( !shouldReplace( currentPos ) ) {
                // Move back up one to ensure the current pos is replaceable
                currentPos.move( 0, 1, 0 );
                return;
            }
        }
        // Initial y was replaceable, so we can default to this
        currentPos.setY( yI );
    }
    
    /** @return Attempts to find the ground. Returns true if the pillar should be canceled. */
    private boolean findGroundAbove( BlockPos.MutableBlockPos currentPos, int radius ) {
        final int yI = currentPos.getY();
        final int maxY = Math.min( yI + radius, level.getMaxBuildHeight() - 2 );
        
        while( currentPos.getY() < maxY ) {
            currentPos.move( 0, 1, 0 );
            // Found a replaceable pos
            if( shouldReplace( currentPos ) ) return false;
        }
        // Initial y was not replaceable, so we must cancel the entire operation
        return true;
    }
    
    /** @return True if a generating pillar should replace the block at a particular position. */
    private boolean shouldReplace( BlockPos pos ) {
        final BlockState stateAtPos = level.getBlockState( pos );
        return (stateAtPos.getMaterial().isReplaceable() || stateAtPos.is( BlockTags.LEAVES )) &&
                !stateAtPos.getFluidState().is( FluidTags.WATER );
    }
    
    /** @return Helper method to simplify spawning strays. Returns true if it spawns one. */
    private boolean trySpawnStray( BlockPos center, int radius ) {
        if( !(level instanceof ServerLevelAccessor) ) return false;
        final StraySkeletonEntity stray = StraySkeletonEntity.SPECIES.entityType.get().create( level );
        if( stray == null ) return false;
        
        // Pick a random position within the ice prison, then cancel if we can't spawn at that position
        final float angle = random.nextFloat() * 2.0F * (float) Math.PI;
        final float distance = random.nextFloat() * (radius - 1);
        final BlockPos.MutableBlockPos currentPos = center.mutable().move(
                Mth.floor( Mth.cos( angle ) * distance ),
                0,
                Mth.floor( Mth.sin( angle ) * distance )
        );
        if( shouldReplace( currentPos ) ) findGroundBelow( currentPos, radius );
        else if( findGroundAbove( currentPos, radius ) ) {
            stray.discard();
            return false; // No floor found
        }
        stray.moveTo( currentPos, angle * 180.0F / (float) Math.PI + 180.0F, 0.0F );
        while( !level.noCollision( stray.getBoundingBox() ) ) {
            if( currentPos.getY() > center.getY() + radius ) {
                stray.discard();
                return false; // Too high
            }
            currentPos.move( 0, 1, 0 );
            stray.moveTo( currentPos, stray.getYRot(), stray.getXRot() );
        }
        
        stray.setTarget( getTarget() );
        stray.finalizeSpawn( (ServerLevelAccessor) level, level.getCurrentDifficultyAt( blockPosition() ),
                MobSpawnType.MOB_SUMMONED, null, null );
        level.addFreshEntity( stray );
        stray.spawnAnim();
        return true;
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundTag saveTag ) {
        setPathfindingMalus( BlockPathTypes.WATER, BlockPathTypes.WALKABLE.getMalus() );
    }
}