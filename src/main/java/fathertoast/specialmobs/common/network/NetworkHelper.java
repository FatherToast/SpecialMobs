package fathertoast.specialmobs.common.network;

import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.network.message.S2CUpdateNinjaModelData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

public class NetworkHelper {

    public static void updateNinjaModelData(World world, int entityId, BlockPos pos) {
        if (world.isClientSide) {
            SpecialMobs.LOG.warn("Attempted to send ninja block model data packet from client side. What?");
            return;
        }
        List<ServerPlayerEntity> recipients = ((ServerWorld)world).players();
        recipients.forEach(player -> PacketHandler.sendToClient(new S2CUpdateNinjaModelData(entityId, pos), player));
    }
}
