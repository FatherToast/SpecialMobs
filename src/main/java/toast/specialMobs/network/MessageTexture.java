package toast.specialMobs.network;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.ISpecialMob;
import toast.specialMobs.entity.SpecialMobData;

public class MessageTexture implements IMessage {
    
    public int entityId;
    public String[] texturePaths;
    
    public MessageTexture() { }
    
    public MessageTexture( Entity entity ) {
        entityId = entity.getEntityId();
        ResourceLocation[] textures = ((ISpecialMob) entity).getSpecialData().getTextures();
        texturePaths = new String[textures.length];
        
        for( int i = textures.length; i-- > 0; ) {
            texturePaths[i] = textures[i].toString();
        }
    }
    
    /**
     * @see cpw.mods.fml.common.network.simpleimpl.IMessage#fromBytes(io.netty.buffer.ByteBuf)
     */
    @Override
    public void fromBytes( ByteBuf buf ) {
        entityId = buf.readInt();
        
        texturePaths = new String[buf.readInt()];
        StringBuilder path;
        
        for( int i = 0; i < texturePaths.length; i++ ) {
            path = new StringBuilder();
            
            for( short l = buf.readShort(); l-- > 0; ) {
                path.append( buf.readChar() );
            }
            texturePaths[i] = path.toString();
        }
    }
    
    /**
     * @see cpw.mods.fml.common.network.simpleimpl.IMessage#toBytes(io.netty.buffer.ByteBuf)
     */
    @Override
    public void toBytes( ByteBuf buf ) {
        buf.writeInt( entityId );
        
        buf.writeInt( texturePaths.length );
        char[] path;
        
        for( String texturePath : texturePaths ) {
            path = texturePath.toCharArray();
            buf.writeShort( path.length );
            for( char c : path ) {
                buf.writeChar( c );
            }
        }
    }
    
    public static class Handler implements IMessageHandler<MessageTexture, IMessage> {
        
        /**
         * @see cpw.mods.fml.common.network.simpleimpl.IMessageHandler#onMessage(cpw.mods.fml.common.network.simpleimpl.IMessage, cpw.mods.fml.common.network.simpleimpl.MessageContext)
         */
        @Override
        public IMessage onMessage( MessageTexture message, MessageContext ctx ) {
            try {
                World world = FMLClientHandler.instance().getWorldClient();
                ISpecialMob mob = (ISpecialMob) world.getEntityByID( message.entityId );
                if( mob != null ) {
                    SpecialMobData data = mob.getSpecialData();
                    data.loadTextures( message.texturePaths );
                }
            }
            catch( Exception ex ) {
                _SpecialMobs.console( "[ERROR] Failed to fetch mob texture from server!" );
                // noinspection all
                ex.printStackTrace();
            }
            return null;
        }
        
    }
}
