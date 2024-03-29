package fathertoast.specialmobs.common.core.register;

import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.item.IncorporealFireChargeItem;
import fathertoast.specialmobs.common.item.SyringeItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SMItems {
    
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create( ForgeRegistries.ITEMS, SpecialMobs.MOD_ID );
    public static final List<RegistryObject<? extends Item>> SIMPLE_ITEMS = new ArrayList<>();


    public static final RegistryObject<Item> SYRINGE = registerSimpleItem("syringe", SyringeItem::new);
    public static final RegistryObject<Item> INCORPOREAL_FIREBALL = registerSimpleItem("incorporeal_fire_charge", IncorporealFireChargeItem::new);

    
    /** Registers an entity type's spawn egg item to the deferred register. */
    public static <T extends Entity> RegistryObject<ForgeSpawnEggItem> registerSpawnEgg(
            RegistryObject<EntityType<T>> entityType, int eggBaseColor, int eggSpotsColor ) {
        final String name = entityType.getId().getPath() + "_spawn_egg";
        return REGISTRY.register( name, () ->
                new ForgeSpawnEggItem( entityType, eggBaseColor, eggSpotsColor, new Item.Properties().tab( ItemGroup.TAB_MISC ) )
        );
    }

    public static <T extends Item> RegistryObject<T> registerSimpleItem(String name, Supplier<T> itemSupplier) {
        RegistryObject<T> regObject = REGISTRY.register(name, itemSupplier);
        SIMPLE_ITEMS.add(regObject);
        return regObject;
    }
}