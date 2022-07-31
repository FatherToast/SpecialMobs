package fathertoast.specialmobs.common.network;

import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.network.message.C2SSpawnIncorporealFireball;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "0";
    
    /** The network channel our mod will be using when sending messages. */
    public static final SimpleChannel CHANNEL = createChannel();
    
    private int messageIndex;
    
    private static SimpleChannel createChannel() {
        return NetworkRegistry.ChannelBuilder
                .named( new ResourceLocation( SpecialMobs.MOD_ID, "channel" ) )
                .serverAcceptedVersions( PROTOCOL_VERSION::equals )
                .clientAcceptedVersions( PROTOCOL_VERSION::equals )
                .networkProtocolVersion( () -> PROTOCOL_VERSION )
                .simpleChannel();
    }
    
    public final void registerMessages() {
        registerMessage( C2SSpawnIncorporealFireball.class, C2SSpawnIncorporealFireball::encode, C2SSpawnIncorporealFireball::decode, C2SSpawnIncorporealFireball::handle );
    }
    
    public <MSG> void registerMessage( Class<MSG> messageType, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler ) {
        CHANNEL.registerMessage( this.messageIndex++, messageType, encoder, decoder, handler, Optional.empty() );
    }
    
    /**
     * Sends the specified message to the client.
     *
     * @param message The message to send to the client.
     * @param player  The player client that should receive this message.
     * @param <MSG>   Packet type.
     */
    public static <MSG> void sendToClient( MSG message, ServerPlayerEntity player ) {
        CHANNEL.sendTo( message, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT );
    }
}