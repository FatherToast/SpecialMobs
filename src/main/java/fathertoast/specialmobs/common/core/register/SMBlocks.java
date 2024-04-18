package fathertoast.specialmobs.common.core.register;

import fathertoast.specialmobs.common.block.MeltingIceBlock;
import fathertoast.specialmobs.common.block.UnderwaterSilverfishBlock;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
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
    
    public static final RegistryObject<Block> MELTING_ICE = registerTechnicalBlock( "melting_ice", MeltingIceBlock::new );
    
    public static final List<RegistryObject<Block>> INFESTED_CORAL;
    
    static {
        final ArrayList<RegistryObject<Block>> infestedCoral = new ArrayList<>();
        for( UnderwaterSilverfishBlock.Type type : UnderwaterSilverfishBlock.Type.values() ) {
            infestedCoral.add( registerBlock( type.blockId(), type::blockSupplier, CreativeModeTabs.COLORED_BLOCKS ) );
        }
        infestedCoral.trimToSize();
        INFESTED_CORAL = Collections.unmodifiableList( infestedCoral );
    }
    
    /** Registers a block and a simple BlockItem for it. */
    private static <T extends Block> RegistryObject<T> registerBlock( String name, Supplier<T> blockSupplier, ResourceKey<CreativeModeTab> creativeTab ) {
        final RegistryObject<T> blockRegObject = REGISTRY.register( name, blockSupplier );
        SMItems.REGISTRY.register( name, () -> new BlockItem( blockRegObject.get(), new Item.Properties() ) );
        return blockRegObject;
    }
    
    /** Registers a technical block (not visible in the creative menu) and a simple BlockItem for it. */
    private static <T extends Block> RegistryObject<T> registerTechnicalBlock( String name, Supplier<T> blockSupplier ) {
        final RegistryObject<T> blockRegObject = REGISTRY.register( name, blockSupplier );
        SMItems.REGISTRY.register( name, () -> new BlockItem( blockRegObject.get(), new Item.Properties() ) );
        return blockRegObject;
    }
}