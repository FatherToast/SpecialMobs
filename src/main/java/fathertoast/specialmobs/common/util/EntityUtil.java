package fathertoast.specialmobs.common.util;

import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;

import javax.annotation.Nullable;

public class EntityUtil {

    @Nullable
    public static Entity getClientMouseOver(PlayerEntity player) {
        if (!player.level.isClientSide) {
            SpecialMobs.LOG.error("Tried to fetch player \"mouse-over\" entity from server side. This can't be right?");
            return null;
        }
        RayTraceResult result = Minecraft.getInstance().hitResult;

        if (result instanceof EntityRayTraceResult) {
            return ((EntityRayTraceResult)result).getEntity();
        }
        return null;
    }
}
