package fathertoast.specialmobs.common.network.work;

import fathertoast.specialmobs.common.entity.misc.MobBoat;
import fathertoast.specialmobs.common.network.message.S2CUpdateBoatInputs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public class ClientWork {
    
    public static void handleUpdateBoatInputs( S2CUpdateBoatInputs message ) {
        ClientLevel level = Minecraft.getInstance().level;
        
        if( level != null && level.getEntity( message.entityId() ) instanceof MobBoat boat ) {
            boat.setInput( message.left(), message.right(), message.forward(), message.backward() );
            boat.setPaddleState( message.right() && !message.left() || message.forward(), message.left() && !message.right() || message.forward() );
        }
    }
}