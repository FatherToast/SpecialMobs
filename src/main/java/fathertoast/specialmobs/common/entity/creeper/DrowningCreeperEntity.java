package fathertoast.specialmobs.common.entity.creeper;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.AttributeHelper;
import fathertoast.specialmobs.common.util.ExplosionHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootEntryItemBuilder;
import fathertoast.specialmobs.datagen.loot.LootPoolBuilder;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.passive.fish.PufferfishEntity;
import net.minecraft.item.Items;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class DrowningCreeperEntity extends _SpecialCreeperEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<DrowningCreeperEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0x2D41F4, BestiaryInfo.BaseWeight.LOW );
        //TODO theme - water
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return AttributeHelper.of( _SpecialCreeperEntity.createAttributes() )
                .addAttribute( Attributes.MAX_HEALTH, 10.0 )
                .build();
    }
    
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
        loot.addUncommonDrop( "uncommon", Items.GOLD_NUGGET, Items.PRISMARINE_SHARD, Items.PRISMARINE_CRYSTALS );
    }
    
    @SpecialMob.Constructor
    public DrowningCreeperEntity( EntityType<? extends _SpecialCreeperEntity> entityType, World world ) {
        super( entityType, world );
        getSpecialData().setImmuneToBurning( true );
        getSpecialData().setCanBreatheInWater( true );
        getSpecialData().setIgnoreWaterPush( true );
        xpReward += 2;
    }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** Override to change this creeper's explosion power multiplier. */
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
        
        final BlockState brainCoral = Blocks.BRAIN_CORAL_BLOCK.defaultBlockState();
        final BlockState hornCoral = Blocks.HORN_CORAL_BLOCK.defaultBlockState();
        final BlockState water = Blocks.WATER.defaultBlockState();
        final BlockState seaPickle = Blocks.SEA_PICKLE.defaultBlockState().setValue( BlockStateProperties.WATERLOGGED, true );
        final BlockState seaGrass = Blocks.SEAGRASS.defaultBlockState();
        final int radius = (int) Math.floor( explosionPower );
        final int rMinusOneSq = (radius - 1) * (radius - 1);
        final BlockPos center = new BlockPos( explosion.getPos() );
        
        // Track how many pufferfish have been spawned
        spawnPufferfish( center.above( 1 ) );
        int pufferCount = 1;
        
        for( int y = -radius; y <= radius; y++ ) {
            for( int x = -radius; x <= radius; x++ ) {
                for( int z = -radius; z <= radius; z++ ) {
                    final int distSq = x * x + y * y + z * z;
                    
                    if( distSq <= radius * radius ) {
                        final BlockPos pos = center.offset( x, y, z );
                        final BlockState stateAtPos = level.getBlockState( pos );
                        
                        if( stateAtPos.getMaterial().isReplaceable() || stateAtPos.is( BlockTags.LEAVES ) ) {
                            if( distSq > rMinusOneSq ) {
                                // "Coral" casing
                                level.setBlock( pos, random.nextFloat() < 0.25F ? brainCoral : hornCoral, References.SET_BLOCK_FLAGS );
                            }
                            else {
                                final float fillChoice = random.nextFloat();
                                
                                if( fillChoice < 0.1F && seaPickle.canSurvive( level, pos ) ) {
                                    level.setBlock( pos, seaPickle, References.SET_BLOCK_FLAGS );
                                }
                                else if( fillChoice < 0.3F && seaGrass.canSurvive( level, pos ) ) {
                                    level.setBlock( pos, seaGrass, References.SET_BLOCK_FLAGS );
                                }
                                else {
                                    // Water fill
                                    level.setBlock( pos, water, References.SET_BLOCK_FLAGS );
                                    
                                    // Prevent greater radiuses from spawning a bazillion pufferfish
                                    if( random.nextFloat() < 0.01F && pufferCount < 10 ) {
                                        spawnPufferfish( pos );
                                        pufferCount++;
                                    }
                                }
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
    
    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "drowning" ),
            GET_TEXTURE_PATH( "drowning_eyes" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
}