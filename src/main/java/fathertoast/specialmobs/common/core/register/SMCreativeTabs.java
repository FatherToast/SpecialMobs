package fathertoast.specialmobs.common.core.register;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.RegistryObject;

public class SMCreativeTabs {

	public static void buildCreativeContents(final BuildCreativeModeTabContentsEvent event) {
		if( event.getTabKey() == CreativeModeTabs.SEARCH ) {
			for( RegistryObject<Item> item : SMItems.REGISTRY.getEntries() ) {
				event.accept(item.get());
			}
		}
	}
}
