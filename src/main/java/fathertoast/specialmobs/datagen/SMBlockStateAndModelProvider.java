package fathertoast.specialmobs.datagen;

import fathertoast.specialmobs.common.block.MeltingIceBlock;
import fathertoast.specialmobs.common.block.UnderwaterSilverfishBlock;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.core.register.SMBlocks;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Objects;

public class SMBlockStateAndModelProvider extends BlockStateProvider {
    
    public SMBlockStateAndModelProvider( DataGenerator gen, ExistingFileHelper existingFileHelper ) {
        super( gen, SpecialMobs.MOD_ID, existingFileHelper );
    }
    
    @Override
    protected void registerStatesAndModels() {
        String name;
        
        // Melting ice
        final VariantBlockStateBuilder builder = getVariantBuilder( SMBlocks.MELTING_ICE.get() );
        name = Objects.requireNonNull( Blocks.FROSTED_ICE.getRegistryName() ).getPath();
        for( int age = 0; age <= 3; age++ ) {
            builder.partialState().with( MeltingIceBlock.AGE, age ).modelForState().modelFile( models().getExistingFile(
                    mcLoc( ModelProvider.BLOCK_FOLDER + "/" + name + "_" + age ) ) ).addModel();
        }
        itemModels().withExistingParent( SMBlocks.MELTING_ICE.getId().getPath(),
                mcLoc( ModelProvider.BLOCK_FOLDER + "/" + name + "_0" ) );
        
        // Infested coral
        for( UnderwaterSilverfishBlock.Type type : UnderwaterSilverfishBlock.Type.values() ) {
            name = Objects.requireNonNull( type.hostBlock().getRegistryName() ).getPath();
            
            getVariantBuilder( type.block() ).partialState().modelForState().modelFile( models().getExistingFile(
                    mcLoc( ModelProvider.BLOCK_FOLDER + "/" + name ) ) ).addModel();
            itemModels().withExistingParent( type.blockId(), mcLoc( ModelProvider.BLOCK_FOLDER + "/" + name ) );
        }
    }
}