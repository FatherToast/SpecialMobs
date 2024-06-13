package fathertoast.specialmobs.common.core.register;

import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.core.register.util.CreativeTabRegObj;
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

	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SpecialMobs.MOD_ID);


	public static final CreativeTabRegObj EGGS_TAB = register("all", () -> CreativeModeTab.builder()
			.icon(() -> new ItemStack(Items.CREEPER_SPAWN_EGG))
			.title(Component.translatable("itemGroup.magical_relics.spawn_eggs"))
			.build());


	private static CreativeTabRegObj register(String name, Supplier<CreativeModeTab> supplier) {
		RegistryObject<CreativeModeTab> regObj = REGISTRY.register(name, supplier);
		return new CreativeTabRegObj(regObj, ResourceKey.create(Registries.CREATIVE_MODE_TAB, SpecialMobs.resourceLoc(name)));
	}


	public static void buildCreativeContents(final BuildCreativeModeTabContentsEvent event) {
		if( event.getTabKey() == CreativeModeTabs.SEARCH ) {
			for( RegistryObject<Item> item : SMItems.REGISTRY.getEntries() ) {
				event.accept(item.get());
			}
		}
		else if ( event.getTabKey() == EGGS_TAB.getKey() ) {
			for( RegistryObject<Item> item : SMItems.REGISTRY.getEntries() ) {
				if ( item.get() instanceof ForgeSpawnEggItem )
					event.accept(item.get());
			}
		}
	}
}
