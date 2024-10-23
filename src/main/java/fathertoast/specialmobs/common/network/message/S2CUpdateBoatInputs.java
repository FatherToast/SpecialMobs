package fathertoast.specialmobs.common.network.message;

import fathertoast.specialmobs.common.network.work.ClientWork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CUpdateBoatInputs {

    public final int entityId;
    public final boolean forward;
    public final boolean backward;
    public final boolean left;
    public final boolean right;

    public S2CUpdateBoatInputs( int entityId, boolean forward, boolean backward, boolean left, boolean right ) {
        this.entityId = entityId;
        this.forward = forward;
        this.backward = backward;
        this.left = left;
        this.right = right;
    }

    public static void handle( S2CUpdateBoatInputs message, Supplier<NetworkEvent.Context> contextSupplier ) {
        NetworkEvent.Context context = contextSupplier.get();

        if ( context.getDirection().getReceptionSide().isClient() ) {
            context.enqueueWork( () -> ClientWork.handleUpdateBoatInputs( message ) );
        }
        context.setPacketHandled( true );
    }

    public static S2CUpdateBoatInputs decode( FriendlyByteBuf buffer ) {
        return new S2CUpdateBoatInputs( buffer.readInt(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean() );
    }

    public static void encode( S2CUpdateBoatInputs message, FriendlyByteBuf buffer ) {
        buffer.writeInt( message.entityId );
        buffer.writeBoolean( message.forward );
        buffer.writeBoolean( message.backward );
        buffer.writeBoolean( message.left );
        buffer.writeBoolean( message.right );
    }
}
