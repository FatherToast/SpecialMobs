package fathertoast.specialmobs.datagen.model;

import fathertoast.specialmobs.common.block.MeltingIceBlock;
import fathertoast.specialmobs.common.block.SlabFireBlock;
import fathertoast.specialmobs.common.block.UnderwaterSilverfishBlock;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.core.register.SMBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.function.Supplier;

public class SMBlockStateAndModelProvider extends BlockStateProvider {
    
    // Render type ids
    private static final ResourceLocation R_TRANSLUCENT = ResourceLocation.withDefaultNamespace( "translucent" );
    private static final ResourceLocation CUTOUT = ResourceLocation.withDefaultNamespace( "cutout" );
    
    /** A string pointing to SM's block model templates folder. */
    private static final String TEMPLATE_FOLDER = BlockModelProvider.BLOCK_FOLDER + "/template";
    
    
    public SMBlockStateAndModelProvider( PackOutput output, ExistingFileHelper existingFileHelper ) {
        super( output, SpecialMobs.MOD_ID, existingFileHelper );
    }
    
    @Override
    protected void registerStatesAndModels() {
        // Melting ice
        final VariantBlockStateBuilder builder = getVariantBuilder( SMBlocks.MELTING_ICE.get() );
        String name = blockName( Blocks.FROSTED_ICE );
        for( int age = 0; age <= 3; age++ ) {
            builder.partialState().with( MeltingIceBlock.AGE, age ).modelForState().modelFile( models().getExistingFile(
                    mcLoc( ModelProvider.BLOCK_FOLDER + "/" + name + "_" + age ) ) ).addModel();
        }
        itemModels().withExistingParent( Objects.requireNonNull( SMBlocks.MELTING_ICE.getId() ).getPath(),
                        mcLoc( ModelProvider.BLOCK_FOLDER + "/" + name + "_0" ) )
                .renderType( R_TRANSLUCENT );
        
        // Infested coral
        for( UnderwaterSilverfishBlock.Type type : UnderwaterSilverfishBlock.Type.values() ) {
            name = blockName( type.hostBlock() );
            
            getVariantBuilder( type.block() ).partialState().modelForState().modelFile( models().getExistingFile(
                    mcLoc( ModelProvider.BLOCK_FOLDER + "/" + name ) ) ).addModel();
            itemModels().withExistingParent( type.blockId(), mcLoc( ModelProvider.BLOCK_FOLDER + "/" + name ) );
        }
        
        // Slab fire
        for( SlabFireBlock.Type type : SlabFireBlock.Type.values() ) {
            slabFire( type );
        }
    }
    
    private void slabFire( SlabFireBlock.Type type ) {
        final Block slabFireBlock = type.block();
        final Block parentBlock = type.parentBlock();
        final String slabFireName = blockName( slabFireBlock );
        final ResourceLocation templateFloor = modLoc( TEMPLATE_FOLDER + "/slab_fire_floor" );
        final ResourceLocation templateSide = modLoc( TEMPLATE_FOLDER + "/slab_fire_side" );
        final ResourceLocation templateSideAlt = modLoc( TEMPLATE_FOLDER + "/slab_fire_side_alt" );
        final ResourceLocation templateUp = modLoc( TEMPLATE_FOLDER + "/slab_fire_up" );
        final ResourceLocation templateUpAlt = modLoc( TEMPLATE_FOLDER + "/slab_fire_up_alt" );
        
        final ModelFile[] floorModels = {
                models().withExistingParent( slabFireName + "_floor0", templateFloor )
                        .texture( TextureSlot.FIRE.getId(), TextureMapping.fire0( parentBlock ).get( TextureSlot.FIRE ) )
                        .renderType( CUTOUT ),
                models().withExistingParent( slabFireName + "_floor1", templateFloor )
                        .texture( TextureSlot.FIRE.getId(), TextureMapping.fire1( parentBlock ).get( TextureSlot.FIRE ) )
                        .renderType( CUTOUT ) };
        final ModelFile[] sideModels = {
                models().withExistingParent( slabFireName + "_side0", templateSide )
                        .texture( TextureSlot.FIRE.getId(), TextureMapping.fire0( parentBlock ).get( TextureSlot.FIRE ) )
                        .renderType( CUTOUT ),
                models().withExistingParent( slabFireName + "_side1", templateSide )
                        .texture( TextureSlot.FIRE.getId(), TextureMapping.fire1( parentBlock ).get( TextureSlot.FIRE ) )
                        .renderType( CUTOUT ),
                models().withExistingParent( slabFireName + "_side_alt0", templateSideAlt )
                        .texture( TextureSlot.FIRE.getId(), TextureMapping.fire0( parentBlock ).get( TextureSlot.FIRE ) )
                        .renderType( CUTOUT ),
                models().withExistingParent( slabFireName + "_side_alt1", templateSideAlt )
                        .texture( TextureSlot.FIRE.getId(), TextureMapping.fire1( parentBlock ).get( TextureSlot.FIRE ) )
                        .renderType( CUTOUT ) };
        final ModelFile[] upModels = {
                models().withExistingParent( slabFireName + "_up0", templateUp )
                        .texture( TextureSlot.FIRE.getId(), TextureMapping.fire0( parentBlock ).get( TextureSlot.FIRE ) )
                        .renderType( CUTOUT ),
                models().withExistingParent( slabFireName + "_up1", templateUp )
                        .texture( TextureSlot.FIRE.getId(), TextureMapping.fire1( parentBlock ).get( TextureSlot.FIRE ) )
                        .renderType( CUTOUT ),
                models().withExistingParent( slabFireName + "_up_alt0", templateUpAlt )
                        .texture( TextureSlot.FIRE.getId(), TextureMapping.fire0( parentBlock ).get( TextureSlot.FIRE ) )
                        .renderType( CUTOUT ),
                models().withExistingParent( slabFireName + "_up_alt1", templateUpAlt )
                        .texture( TextureSlot.FIRE.getId(), TextureMapping.fire1( parentBlock ).get( TextureSlot.FIRE ) )
                        .renderType( CUTOUT ) };
        
        final MultiPartBlockStateBuilder builder = getMultipartBuilder( slabFireBlock );
        
        // Floor models are applied if the fire is "standing" on a block
        builder.part().modelFile( floorModels[0] ).nextModel().modelFile( floorModels[1] ).addModel()
                .condition( BlockStateProperties.NORTH, false ).condition( BlockStateProperties.EAST, false )
                .condition( BlockStateProperties.SOUTH, false ).condition( BlockStateProperties.WEST, false )
                .condition( BlockStateProperties.UP, false ).end();
        
        // Up/ceiling models
        builder.part().modelFile( upModels[0] )
                .nextModel().modelFile( upModels[1] ).nextModel().modelFile( upModels[2] ).nextModel().modelFile( upModels[3] )
                .addModel().condition( BlockStateProperties.UP, true ).end();
        
        // Side models
        builder.part().modelFile( sideModels[0] )
                .nextModel().modelFile( sideModels[1] )
                .nextModel().modelFile( sideModels[2] )
                .nextModel().modelFile( sideModels[3] )
                .addModel().useOr()
                .nestedGroup()
                .condition( BlockStateProperties.NORTH, false ).condition( BlockStateProperties.EAST, false )
                .condition( BlockStateProperties.SOUTH, false ).condition( BlockStateProperties.WEST, false )
                .condition( BlockStateProperties.UP, false )
                .end()
                .nestedGroup()
                .condition( BlockStateProperties.NORTH, true )
                .end();
        builder.part().modelFile( sideModels[0] ).rotationY( 90 )
                .nextModel().modelFile( sideModels[1] ).rotationY( 90 )
                .nextModel().modelFile( sideModels[2] ).rotationY( 90 )
                .nextModel().modelFile( sideModels[3] ).rotationY( 90 )
                .addModel().useOr()
                .nestedGroup()
                .condition( BlockStateProperties.NORTH, false ).condition( BlockStateProperties.EAST, false )
                .condition( BlockStateProperties.SOUTH, false ).condition( BlockStateProperties.WEST, false )
                .condition( BlockStateProperties.UP, false )
                .end()
                .nestedGroup()
                .condition( BlockStateProperties.EAST, true )
                .end();
        builder.part().modelFile( sideModels[0] ).rotationY( 180 )
                .nextModel().modelFile( sideModels[1] ).rotationY( 180 )
                .nextModel().modelFile( sideModels[2] ).rotationY( 180 )
                .nextModel().modelFile( sideModels[3] ).rotationY( 180 )
                .addModel().useOr()
                .nestedGroup()
                .condition( BlockStateProperties.NORTH, false ).condition( BlockStateProperties.EAST, false )
                .condition( BlockStateProperties.SOUTH, false ).condition( BlockStateProperties.WEST, false )
                .condition( BlockStateProperties.UP, false )
                .end()
                .nestedGroup()
                .condition( BlockStateProperties.SOUTH, true )
                .end();
        builder.part().modelFile( sideModels[0] ).rotationY( 270 )
                .nextModel().modelFile( sideModels[1] ).rotationY( 270 )
                .nextModel().modelFile( sideModels[2] ).rotationY( 270 )
                .nextModel().modelFile( sideModels[3] ).rotationY( 270 )
                .addModel().useOr()
                .nestedGroup()
                .condition( BlockStateProperties.NORTH, false ).condition( BlockStateProperties.EAST, false )
                .condition( BlockStateProperties.SOUTH, false ).condition( BlockStateProperties.WEST, false )
                .condition( BlockStateProperties.UP, false )
                .end()
                .nestedGroup()
                .condition( BlockStateProperties.WEST, true )
                .end();
    }
    
    private String blockName( Supplier<Block> block ) {
        return Objects.requireNonNull( ForgeRegistries.BLOCKS.getKey( block.get() ) ).getPath();
    }
    
    private String blockName( Block block ) {
        return Objects.requireNonNull( ForgeRegistries.BLOCKS.getKey( block ) ).getPath();
    }
}