package toast.specialMobs.network;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.world.World;

public class MessageExplosion implements IMessage {
    
    // This explosion's type.
    public Type type;
    // The explosion radius.
    public float size;
    // The explosion coords.
    public double posX, posY, posZ;
    
    public MessageExplosion() { }
    
    public MessageExplosion( Entity exploder, float size, Type type ) {
        this( exploder.posX, exploder.posY, exploder.posZ, size, type );
    }
    
    public MessageExplosion( double posX, double posY, double posZ, float size, Type type ) {
        this.type = type;
        this.size = size;
        this.posX = (float) posX;
        this.posY = (float) posY;
        this.posZ = (float) posZ;
    }
    
    /**
     * @see cpw.mods.fml.common.network.simpleimpl.IMessage#fromBytes(io.netty.buffer.ByteBuf)
     */
    @Override
    public void fromBytes( ByteBuf buf ) {
        try {
            type = Type.getType( buf.readByte() );
            size = buf.readFloat();
            posX = buf.readDouble();
            posY = buf.readDouble();
            posZ = buf.readDouble();
        }
        catch( Exception ex ) {
            // noinspection all
            ex.printStackTrace();
        }
    }
    
    /**
     * @see cpw.mods.fml.common.network.simpleimpl.IMessage#toBytes(io.netty.buffer.ByteBuf)
     */
    @Override
    public void toBytes( ByteBuf buf ) {
        try {
            buf.writeByte( type.getId() );
            buf.writeFloat( size );
            buf.writeDouble( posX );
            buf.writeDouble( posY );
            buf.writeDouble( posZ );
        }
        catch( Exception ex ) {
            // noinspection all
            ex.printStackTrace();
        }
    }
    
    public static class Handler implements IMessageHandler<MessageExplosion, IMessage> {
        
        /**
         * @see cpw.mods.fml.common.network.simpleimpl.IMessageHandler#onMessage(cpw.mods.fml.common.network.simpleimpl.IMessage, cpw.mods.fml.common.network.simpleimpl.MessageContext)
         */
        @Override
        public IMessage onMessage( MessageExplosion message, MessageContext ctx ) {
            World world = FMLClientHandler.instance().getWorldClient();
            
            switch( message.type ) {
                case LIGHTNING: {
                    if( message.size < 0.0F ) {
                        message.size = 0.0F;
                    }
                    for( float x = -message.size; x <= message.size; x++ ) {
                        for( float z = -message.size; z <= message.size; z++ ) {
                            world.spawnEntityInWorld( new EntityLightningBolt( world, message.posX + x, message.posY, message.posZ + z ) );
                        }
                    }
                    break;
                }
                default: break;
            }
            // No reply message
            return null;
        }
    }
    
    public enum Type {
        SAFE( 0, "safe" ),
        LIGHTNING( 1, "lightning" );
        
        private static final Type[] allTypes = new Type[Type.values().length];
        
        private final byte id;
        private final String name;
        
        Type( int id, String name ) {
            this.id = (byte) id;
            this.name = name;
        }
        
        /** @return This type's id. */
        public byte getId() {
            return id;
        }
        
        /** @return This type's name. */
        public String getName() {
            return name;
        }
        
        /** @return The explosion type with the given id. */
        public static Type getType( byte id ) {
            return Type.allTypes[id % Type.allTypes.length];
        }
        
        static {
            // Assign all enum types to an ordered array.
            Type[] types = Type.values();
            
            for( Type type : types ) {
                Type.allTypes[type.getId()] = type;
            }
        }
    }
}
