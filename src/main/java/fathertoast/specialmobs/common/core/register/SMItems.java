package fathertoast.specialmobs.common.core.register;

import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class SMItems {
    
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create( ForgeRegistries.ITEMS, SpecialMobs.MOD_ID );
    
    /** Registers an entity type's spawn egg item to the deferred register. */
    public static <T extends Entity> RegistryObject<ForgeSpawnEggItem> registerSpawnEgg(
            RegistryObject<EntityType<T>> entityType, int eggBaseColor, int eggSpotsColor ) {
        final String name = entityType.getId().getPath() + "_spawn_egg";
        return REGISTRY.register( name, () ->
                new ForgeSpawnEggItem( entityType, eggBaseColor, eggSpotsColor, new Item.Properties().tab( ItemGroup.TAB_MISC ) )
        );
    }
}