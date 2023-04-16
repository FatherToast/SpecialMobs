package fathertoast.specialmobs.common.compat.top;

import fathertoast.specialmobs.common.entity.ai.INinja;
import mcjty.theoneprobe.api.*;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class NinjaEntityDisplayOverride implements IEntityDisplayOverride {

    @Override
    public boolean overrideStandardInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, Player playerEntity, Level level, Entity entity, IProbeHitEntityData iProbeHitEntityData) {
        if (entity instanceof INinja ninja) {
            if (ninja.getHiddenDragon() != null) {
                Block block = ninja.getHiddenDragon().getBlock();

                iProbeInfo.horizontal().item(new ItemStack(block.asItem()));
                iProbeInfo.horizontal().text(CompoundText.create().text(Component.literal(translateBlockName(block))), iProbeInfo.defaultTextStyle());

                return true;
            }
        }
        return false;
    }

    /** Returns the localized name of the block */
    private String translateBlockName(Block block) {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
        return I18n.get("block." + Objects.requireNonNull(id).getNamespace() + "." + id.getPath());
    }
}