package fathertoast.specialmobs.common.network.work;

import fathertoast.specialmobs.common.entity.misc.MobBoat;
import fathertoast.specialmobs.common.network.message.S2CUpdateBoatInputs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public class ClientWork {

    public static void handleUpdateBoatInputs( S2CUpdateBoatInputs message ) {
        ClientLevel level = Minecraft.getInstance().level;

        if ( level.getEntity( message.entityId ) instanceof MobBoat boat ) {
            boat.setInput( message.left, message.right, message.forward, message.backward );
        }
    }
}
