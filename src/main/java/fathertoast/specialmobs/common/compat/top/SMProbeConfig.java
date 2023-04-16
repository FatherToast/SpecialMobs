package fathertoast.specialmobs.common.compat.top;

import fathertoast.specialmobs.common.entity.ai.INinja;
import mcjty.theoneprobe.api.IProbeConfig;
import mcjty.theoneprobe.api.IProbeConfigProvider;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeHitEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SMProbeConfig implements IProbeConfigProvider {

    @Override
    public void getProbeConfig(IProbeConfig probeConfig, Player player, Level level, Entity entity, IProbeHitEntityData iProbeHitEntityData) {
        if (entity instanceof INinja ninja) {
            if (ninja.getHiddenDragon() != null) {
                probeConfig.showMobHealth(IProbeConfig.ConfigMode.NOT);
            }
        }
    }

    @Override
    public void getProbeConfig(IProbeConfig probeConfig, Player playerEntity, Level level, BlockState blockState, IProbeHitData iProbeHitData) {

    }
}