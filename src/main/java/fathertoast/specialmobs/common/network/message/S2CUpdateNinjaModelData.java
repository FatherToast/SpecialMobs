package fathertoast.specialmobs.common.network.message;

import fathertoast.specialmobs.common.network.work.ClientWork;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CUpdateNinjaModelData {

    public final int entityId;
    public final BlockPos pos;

    public S2CUpdateNinjaModelData(int entityId, BlockPos pos) {
        this.entityId = entityId;
        this.pos = pos;
    }

    public static void handle(S2CUpdateNinjaModelData message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> ClientWork.updateNinjaModelData(message));
        }
        context.setPacketHandled(true);
    }

    public static S2CUpdateNinjaModelData decode(PacketBuffer buffer) {
        return new S2CUpdateNinjaModelData(buffer.readInt(), buffer.readBlockPos());
    }

    public static void encode(S2CUpdateNinjaModelData message, PacketBuffer buffer) {
        buffer.writeInt(message.entityId);
        buffer.writeBlockPos(message.pos);
    }
}
