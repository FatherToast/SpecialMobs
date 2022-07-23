package fathertoast.specialmobs.common.entity.creeper;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.entity.ai.FluidPathNavigator;
import fathertoast.specialmobs.common.util.ExplosionHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.FrostWalkerEnchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.Effects;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

@SpecialMob
public class SnowCreeperEntity extends _SpecialCreeperEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<SnowCreeperEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xE8F8F8 ).weight( BestiaryInfo.DefaultWeight.LOW ).theme( BestiaryInfo.Theme.ICE )
                .uniqueTextureBaseOnly()
                .addExperience( 2 ).effectImmune( Effects.MOVEMENT_SLOWDOWN )
                .addToAttribute( Attributes.MAX_HEALTH, 10.0 );
    }
    
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
    public static EntityType.IFactory<SnowCreeperEntity> getVariantFactory() { return SnowCreeperEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends SnowCreeperEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public SnowCreeperEntity( EntityType<? extends _SpecialCreeperEntity> entityType, World world ) {
        super( entityType, world );
        setPathfindingMalus( PathNodeType.WATER, PathNodeType.WALKABLE.getMalus() );
    }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        AIHelper.replaceWaterAvoidingRandomWalking( this, 0.8 );
    }
    
    /** @return A new path navigator for this entity to use. */
    @Override
    protected PathNavigator createNavigation( World world ) {
        return new FluidPathNavigator( this, world, true, false );
    }
    
    /** @return Whether this entity can stand on a particular type of fluid. */
    @Override
    public boolean canStandOnFluid( Fluid fluid ) { return fluid.is( FluidTags.WATER ); }
    
    /** Called each tick to update this entity. */
    @Override
    public void tick() {
        super.tick();
        MobHelper.floatInFluid( this, 0.06, FluidTags.WATER );
    }
    
    /** Called whenever this entity's block position changes. */
    @Override
    protected void onChangedBlock( BlockPos pos ) {
        super.onChangedBlock( pos );
        updateFrostWalker( pos );
    }
    
    /** Override to change this creeper's explosion power multiplier. */
    @Override
    protected float getVariantExplosionPower( float radius ) { return super.getVariantExplosionPower( radius ) + 2.0F; }
    
    /** Override to change this creeper's explosion. */
    @Override
    protected void makeVariantExplosion( float explosionPower ) {
        final Explosion.Mode explosionMode = ExplosionHelper.getMode( this );
        final ExplosionHelper explosion = new ExplosionHelper( this,
                explosionMode == Explosion.Mode.NONE ? explosionPower : 1.0F, explosionMode, false );
        if( !explosion.initializeExplosion() ) return;
        explosion.finalizeExplosion();
        
        if( explosionMode == Explosion.Mode.NONE ) return;
        
        final BlockState ice = Blocks.ICE.defaultBlockState();
        final int radius = (int) Math.floor( explosionPower );
        final int rMinusOneSq = (radius - 1) * (radius - 1);
        final BlockPos center = new BlockPos( explosion.getPos() );
        //TODO make ice arena and then populate with some strays
        
        //        for( int y = -radius; y <= radius; y++ ) {
        //            for( int x = -radius; x <= radius; x++ ) {
        //                for( int z = -radius; z <= radius; z++ ) {
        //                    final int distSq = x * x + y * y + z * z;
        //
        //                    if( distSq <= radius * radius ) {
        //                        final BlockPos pos = center.offset( x, y, z );
        //                        final BlockState stateAtPos = level.getBlockState( pos );
        //
        //                        if( stateAtPos.getMaterial().isReplaceable() || stateAtPos.is( BlockTags.LEAVES ) ) {
        //                            if( distSq > rMinusOneSq ) {
        //                                // "Coral" casing
        //                                level.setBlock( pos, random.nextFloat() < 0.25F ? brainCoral : hornCoral, References.SET_BLOCK_FLAGS );
        //                            }
        //                            else {
        //                                final float fillChoice = random.nextFloat();
        //
        //                                if( fillChoice < 0.1F && seaPickle.canSurvive( level, pos ) ) {
        //                                    level.setBlock( pos, seaPickle, References.SET_BLOCK_FLAGS );
        //                                }
        //                                else if( fillChoice < 0.3F && seaGrass.canSurvive( level, pos ) ) {
        //                                    level.setBlock( pos, seaGrass, References.SET_BLOCK_FLAGS );
        //                                }
        //                                else {
        //                                    // Water fill
        //                                    level.setBlock( pos, water, References.SET_BLOCK_FLAGS );
        //
        //                                    // Prevent greater radiuses from spawning a bazillion pufferfish
        //                                    if( random.nextFloat() < 0.01F && pufferCount < 10 ) {
        //                                        spawnStray( pos );
        //                                        pufferCount++;
        //                                    }
        //                                }
        //                            }
        //                        }
        //                    }
        //                }
        //            }
        //        }
    }
    
    /** Helper method to simplify spawning strays. */
    private void spawnStray( BlockPos pos ) {
        //TODO
        //        if( !(level instanceof IServerWorld) ) return;
        //        final PufferfishEntity lePuffPuff = EntityType.PUFFERFISH.create( level );
        //        if( lePuffPuff != null ) {
        //            lePuffPuff.setPos( pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5 );
        //            level.addFreshEntity( lePuffPuff );
        //        }
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundNBT saveTag ) {
        setPathfindingMalus( PathNodeType.WATER, PathNodeType.WALKABLE.getMalus() );
    }
    
    /** Called to make the frost walker ice platform around this entity, as needed. */
    private void updateFrostWalker( BlockPos pos ) {
        final boolean actualOnGround = onGround;
        onGround = true; // Spoof the frost walker enchant requirement to be on the ground
        FrostWalkerEnchantment.onEntityMoved( this, level, pos, 1 );
        onGround = actualOnGround;
    }
}