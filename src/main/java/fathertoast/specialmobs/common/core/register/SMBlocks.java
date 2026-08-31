package fathertoast.specialmobs.common.core.register;

import fathertoast.specialmobs.common.block.MeltingIceBlock;
import fathertoast.specialmobs.common.block.SlabFireBlock;
import fathertoast.specialmobs.common.block.UnderwaterSilverfishBlock;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class SMBlocks {
    /** The deferred register for this mod's blocks. */
    public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create( ForgeRegistries.BLOCKS, SpecialMobs.MOD_ID );
    
    public static final RegistryObject<Block> MELTING_ICE = registerBlockNoItem( "melting_ice", MeltingIceBlock::new );
    
    public static final List<RegistryObject<Block>> INFESTED_CORAL;
    public static final List<RegistryObject<Block>> SLAB_FIRES;
    
    static {
        final ArrayList<RegistryObject<Block>> infestedCoral = new ArrayList<>();
        for( UnderwaterSilverfishBlock.Type type : UnderwaterSilverfishBlock.Type.values() ) {
            infestedCoral.add( registerBlock( type.blockId(), type::blockSupplier ) );
        }
        infestedCoral.trimToSize();
        INFESTED_CORAL = Collections.unmodifiableList( infestedCoral );
        
        final ArrayList<RegistryObject<Block>> slabFires = new ArrayList<>();
        for( SlabFireBlock.Type type : SlabFireBlock.Type.values() ) {
            slabFires.add( registerBlock( type.blockId(), type::blockSupplier, CreativeModeTabs.COLORED_BLOCKS ) );
        }
        slabFires.trimToSize();
        SLAB_FIRES = Collections.unmodifiableList( slabFires );
    }
    
    /** Registers a block and a simple item for it. */
    private static <T extends Block> RegistryObject<T> registerBlock( String name, Supplier<T> blockSupplier ) {
        final RegistryObject<T> blockRegObject = registerBlockNoItem( name, blockSupplier );
        SMItems.REGISTRY.register( name, () -> new BlockItem( blockRegObject.get(), new Item.Properties() ) );
        return blockRegObject;
    }
    
    /** Registers a block with no item. */
    private static <T extends Block> RegistryObject<T> registerBlockNoItem( String name, Supplier<T> blockSupplier ) {
        return REGISTRY.register( name, blockSupplier );
    }
}