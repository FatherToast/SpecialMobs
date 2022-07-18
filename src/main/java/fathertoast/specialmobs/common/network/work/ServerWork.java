package fathertoast.specialmobs.common.network.work;

import fathertoast.specialmobs.common.entity.projectile.IncorporealFireballEntity;
import fathertoast.specialmobs.common.network.message.C2SSpawnIncorporealFireball;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

public class ServerWork {

    public static void handleSpawnIncorporealFireball(C2SSpawnIncorporealFireball message) {
        MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);

        if (server == null)
            return;

        ServerPlayerEntity player = server.getPlayerList().getPlayer(message.playerUUID);

        if (player == null)
            return;

        ServerWorld world = (ServerWorld) player.level;
        Entity entity = world.getEntity(message.targetEntityID);

        if (!(entity instanceof LivingEntity))
            return;

        LivingEntity livingEntity = (LivingEntity) entity;
        IncorporealFireballEntity fireballEntity = new IncorporealFireballEntity(world, player, livingEntity, player.getX(), player.getEyeY(), player.getZ());
        world.addFreshEntity(fireballEntity);
    }
 }
