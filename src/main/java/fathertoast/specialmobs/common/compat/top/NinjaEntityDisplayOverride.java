package fathertoast.specialmobs.common.compat.top;

import fathertoast.specialmobs.common.entity.ai.INinja;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.util.Objects;

public class NinjaEntityDisplayOverride implements IEntityDisplayOverride {



    @Override
    public boolean overrideStandardInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, PlayerEntity playerEntity, World world, Entity entity, IProbeHitEntityData iProbeHitEntityData) {
        if (entity instanceof INinja) {
            INinja ninja = (INinja) entity;

            if (ninja.getDisguiseBlock() != null) {
                Block block = ninja.getDisguiseBlock().getBlock();

                iProbeInfo.horizontal().item(new ItemStack(block.asItem()));
                iProbeInfo.horizontal().text(CompoundText.create().text(new StringTextComponent(translateBlockName(block))), iProbeInfo.defaultTextStyle());

                return true;
            }
        }
        return false;
    }

    /** Returns the localized name of a block */
    private String translateBlockName(Block block) {
        ResourceLocation resourceLocation = block.getRegistryName();
        return I18n.get("block." + Objects.requireNonNull(resourceLocation).getNamespace() + "." + resourceLocation.getPath());
    }
}
