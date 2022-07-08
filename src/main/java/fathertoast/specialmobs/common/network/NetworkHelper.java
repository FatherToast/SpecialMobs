package fathertoast.specialmobs.common.network;

import fathertoast.specialmobs.common.network.message.C2SSpawnIncorporealFireball;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import javax.annotation.Nonnull;

public class NetworkHelper {

    public static void spawnIncorporealFireball(@Nonnull PlayerEntity player, @Nonnull LivingEntity livingEntity) {
        PacketHandler.CHANNEL.sendToServer(new C2SSpawnIncorporealFireball(player.getUUID(), livingEntity.getId()));
    }
}
