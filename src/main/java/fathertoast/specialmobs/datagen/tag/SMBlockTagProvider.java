package fathertoast.specialmobs.datagen.tag;

import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.core.register.SMBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class SMBlockTagProvider extends BlockTagsProvider {
    
    public SMBlockTagProvider( PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper ) {
        super( output, lookupProvider, SpecialMobs.MOD_ID, existingFileHelper );
    }
    
    @Override
    protected void addTags( HolderLookup.Provider provider ) {
        IntrinsicTagAppender<Block> fires = tag( BlockTags.FIRE );
        SMBlocks.SLAB_FIRES.forEach( ( regObj ) -> fires.add( regObj.get() ) );
    }
}
