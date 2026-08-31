package fathertoast.specialmobs.common.core.register;

import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class SMCreativeTabs {
    
    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create( Registries.CREATIVE_MODE_TAB, SpecialMobs.MOD_ID );
    
    
    public static final RegObj EGGS_TAB = register( "all", () -> CreativeModeTab.builder()
            .icon( () -> new ItemStack( Items.CREEPER_SPAWN_EGG ) )
            .title( Component.translatable( "itemGroup.magical_relics.spawn_eggs" ) )
            .build() );
    
    
    private static RegObj register( String name, Supplier<CreativeModeTab> supplier ) {
        RegistryObject<CreativeModeTab> regObj = REGISTRY.register( name, supplier );
        return new RegObj( regObj, ResourceKey.create( Registries.CREATIVE_MODE_TAB, SpecialMobs.rl( name ) ) );
    }
    
    
    public static void buildCreativeContents( final BuildCreativeModeTabContentsEvent event ) {
        if( event.getTabKey() == CreativeModeTabs.SEARCH ) {
            for( RegistryObject<Item> item : SMItems.REGISTRY.getEntries() ) {
                event.accept( item.get() );
            }
        }
        else if( event.getTabKey() == EGGS_TAB.key() ) {
            for( RegistryObject<Item> item : SMItems.REGISTRY.getEntries() ) {
                if( item.get() instanceof ForgeSpawnEggItem )
                    event.accept( item.get() );
            }
        }
    }
    
    
    public record RegObj( RegistryObject<CreativeModeTab> regObj, ResourceKey<CreativeModeTab> key ) {
        @SuppressWarnings( "unused" )
        public CreativeModeTab getTab() { return regObj.get(); }
    }
}