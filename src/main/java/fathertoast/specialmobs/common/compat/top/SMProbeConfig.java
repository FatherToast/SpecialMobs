package fathertoast.specialmobs.common.compat.top;

import fathertoast.specialmobs.common.entity.ai.INinja;
import mcjty.theoneprobe.api.IProbeConfig;
import mcjty.theoneprobe.api.IProbeConfigProvider;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeHitEntityData;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class SMProbeConfig implements IProbeConfigProvider {

    @Override
    public void getProbeConfig(IProbeConfig probeConfig, PlayerEntity playerEntity, World world, Entity entity, IProbeHitEntityData iProbeHitEntityData) {
        if (entity instanceof INinja) {
            INinja ninja = (INinja) entity;

            if (ninja.getHiddenDragon() != null) {
                probeConfig.showMobHealth(IProbeConfig.ConfigMode.NOT);
            }
        }
    }

    @Override
    public void getProbeConfig(IProbeConfig probeConfig, PlayerEntity playerEntity, World world, BlockState blockState, IProbeHitData iProbeHitData) {

    }
}