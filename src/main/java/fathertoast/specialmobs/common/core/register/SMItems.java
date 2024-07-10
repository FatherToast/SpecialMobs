package fathertoast.specialmobs.common.core.register;

import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.item.IncorporealFireChargeItem;
import fathertoast.specialmobs.common.item.SlabFireChargeItem;
import fathertoast.specialmobs.common.item.SyringeItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SMItems {
    
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create( ForgeRegistries.ITEMS, SpecialMobs.MOD_ID );
    public static final List<RegistryObject<? extends Item>> SIMPLE_ITEMS = new ArrayList<>();


    public static final RegistryObject<Item> SYRINGE = registerSimpleItem( "syringe", SyringeItem::new );
    public static final RegistryObject<Item> INCORPOREAL_FIREBALL = registerSimpleItem( "incorporeal_fire_charge", IncorporealFireChargeItem::new );
    public static final RegistryObject<Item> SLAB_FIREBALL = registerSimpleItem( "slab_fire_charge", SlabFireChargeItem::new );

    
    /** Registers an entity type's spawn egg item to the deferred register. */
    public static <T extends Mob> RegistryObject<ForgeSpawnEggItem> registerSpawnEgg(
            RegistryObject<EntityType<T>> entityType, int eggBaseColor, int eggSpotsColor ) {
        final String name = entityType.getId().getPath() + "_spawn_egg";
        return REGISTRY.register( name, () ->
                new ForgeSpawnEggItem( entityType, eggBaseColor, eggSpotsColor, new Item.Properties() )
        );
    }

    public static <T extends Item> RegistryObject<T> registerSimpleItem(String name, Supplier<T> itemSupplier) {
        RegistryObject<T> regObject = REGISTRY.register(name, itemSupplier);
        SIMPLE_ITEMS.add(regObject);
        return regObject;
    }
}