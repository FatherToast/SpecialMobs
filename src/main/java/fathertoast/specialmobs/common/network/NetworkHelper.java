package fathertoast.specialmobs.common.network;

import fathertoast.specialmobs.common.entity.misc.MobBoat;
import fathertoast.specialmobs.common.network.message.S2CUpdateBoatInputs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class NetworkHelper {

    public static void sendUpdateBoatInputs( MobBoat boat, boolean forward, boolean backward, boolean left, boolean right ) {
        if ( boat.level() instanceof ServerLevel serverLevel ) {
            for ( ServerPlayer player : serverLevel.players() ) {
                PacketHandler.sendToClient( new S2CUpdateBoatInputs( boat.getId(), forward, backward, left, right ), player );
            }
        }
    }
}