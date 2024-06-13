package fathertoast.specialmobs.common.core.register.util;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.RegistryObject;

public class CreativeTabRegObj {

    private final RegistryObject<CreativeModeTab> regObj;
    private final ResourceKey<CreativeModeTab> key;


    public CreativeTabRegObj(RegistryObject<CreativeModeTab> regObj, ResourceKey<CreativeModeTab> key) {
        this.regObj = regObj;
        this.key = key;
    }

    public RegistryObject<CreativeModeTab> getRegObj() {
        return regObj;
    }

    public CreativeModeTab getTab() {
        return regObj.get();
    }

    public ResourceKey<CreativeModeTab> getKey() {
        return key;
    }
}