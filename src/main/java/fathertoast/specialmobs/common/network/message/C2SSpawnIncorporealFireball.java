package fathertoast.specialmobs.common.network.message;

public class C2SSpawnIncorporealFireball {
    //    public final UUID playerUUID;
    //    public final int targetEntityID;
    //
    //    public C2SSpawnIncorporealFireball(UUID playerUUID, int targetEntityId) {
    //        this.playerUUID = playerUUID;
    //        this.targetEntityID = targetEntityId;
    //    }
    //
    //    public static void handle(C2SSpawnIncorporealFireball message, Supplier<NetworkEvent.Context> contextSupplier) {
    //        NetworkEvent.Context context = contextSupplier.get();
    //
    //        if (context.getDirection().getReceptionSide().isServer()) {
    //            context.enqueueWork(() -> ServerWork.handleSpawnIncorporealFireball(message));
    //        }
    //        context.setPacketHandled(true);
    //    }
    //
    //    public static C2SSpawnIncorporealFireball decode(PacketBuffer buffer) {
    //        return new C2SSpawnIncorporealFireball(buffer.readUUID(), buffer.readInt());
    //    }
    //
    //    public static void encode(C2SSpawnIncorporealFireball message, PacketBuffer buffer) {
    //        buffer.writeUUID(message.playerUUID);
    //        buffer.writeInt(message.targetEntityID);
    //    }
}