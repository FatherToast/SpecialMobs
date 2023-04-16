package fathertoast.specialmobs.datagen;

import fathertoast.specialmobs.common.block.MeltingIceBlock;
import fathertoast.specialmobs.common.block.UnderwaterSilverfishBlock;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.core.register.SMBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.function.Supplier;

public class SMBlockStateAndModelProvider extends BlockStateProvider {

    // Render type ids
    private static final ResourceLocation R_TRANSLUCENT = new ResourceLocation("translucent");



    public SMBlockStateAndModelProvider( DataGenerator gen, ExistingFileHelper existingFileHelper ) {
        super( gen, SpecialMobs.MOD_ID, existingFileHelper );
    }
    
    @Override
    protected void registerStatesAndModels() {
        // Melting ice
        final VariantBlockStateBuilder builder = getVariantBuilder( SMBlocks.MELTING_ICE.get() );
        String name = blockName(Blocks.FROSTED_ICE);
        for( int age = 0; age <= 3; age++ ) {
            builder.partialState().with( MeltingIceBlock.AGE, age ).modelForState().modelFile( models().getExistingFile(
                    mcLoc( ModelProvider.BLOCK_FOLDER + "/" + name + "_" + age ) ) ).addModel();
        }
        itemModels().withExistingParent( SMBlocks.MELTING_ICE.getId().getPath(),
                mcLoc( ModelProvider.BLOCK_FOLDER + "/" + name + "_0" ) )
                .renderType(R_TRANSLUCENT);
        
        // Infested coral
        for( UnderwaterSilverfishBlock.Type type : UnderwaterSilverfishBlock.Type.values() ) {
            name = blockName(type.hostBlock());
            
            getVariantBuilder( type.block() ).partialState().modelForState().modelFile( models().getExistingFile(
                    mcLoc( ModelProvider.BLOCK_FOLDER + "/" + name ) ) ).addModel();
            itemModels().withExistingParent( type.blockId(), mcLoc( ModelProvider.BLOCK_FOLDER + "/" + name ) );
        }
    }

    private String blockName(Supplier<Block> block) {
        return Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block.get())).getPath();
    }

    private String blockName(Block block) {
        return Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).getPath();
    }
}