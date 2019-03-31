package fathertoast.specialmobs;

import fathertoast.specialmobs.entity.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public
class MessageTexture implements IMessage
{
	private static final int TEXTURE_COUNT = 3;
	
	private int      entityId;
	private String[] texturePaths;
	
	// Used by the client reciever
	@SuppressWarnings( "unused" )
	public
	MessageTexture( ) { }
	
	public
	MessageTexture( Entity entity )
	{
		entityId = entity.getEntityId( );
		SpecialMobData data = ((ISpecialMob) entity).getSpecialData( );
		texturePaths = new String[] {
			data.getTexture( ).toString( ),
			data.getTextureEyes( ) == null ? "" : data.getTextureEyes( ).toString( ),
			data.getTextureOverlay( ) == null ? "" : data.getTextureOverlay( ).toString( )
		};
	}
	
	/*
	 * @see cpw.mods.fml.common.network.simpleimpl.IMessage#fromBytes(io.netty.buffer.ByteBuf)
	 */
	@Override
	public
	void fromBytes( ByteBuf buf )
	{
		entityId = buf.readInt( );
		
		texturePaths = new String[ TEXTURE_COUNT ];
		StringBuffer path;
		short        len;
		for( int i = 0; i < TEXTURE_COUNT; i++ ) {
			path = new StringBuffer( );
			len = buf.readShort( );
			for( short c = 0; c < len; c++ ) {
				path.append( buf.readChar( ) );
			}
			texturePaths[ i ] = path.toString( );
		}
	}
	
	/*
	 * @see cpw.mods.fml.common.network.simpleimpl.IMessage#toBytes(io.netty.buffer.ByteBuf)
	 */
	@Override
	public
	void toBytes( ByteBuf buf )
	{
		buf.writeInt( entityId );
		
		char[] path;
		for( int i = 0; i < TEXTURE_COUNT; i++ ) {
			path = texturePaths[ i ].toCharArray( );
			buf.writeShort( path.length );
			for( char c : path ) {
				buf.writeChar( c );
			}
		}
	}
	
	public static
	class Handler implements IMessageHandler< MessageTexture, IMessage >
	{
		/*
		 * @see cpw.mods.fml.common.network.simpleimpl.IMessageHandler#onMessage(cpw.mods.fml.common.network.simpleimpl.IMessage, cpw.mods.fml.common.network.simpleimpl.MessageContext)
		 */
		@Override
		public
		IMessage onMessage( MessageTexture message, MessageContext ctx )
		{
			try {
				World       world = FMLClientHandler.instance( ).getWorldClient( );
				ISpecialMob mob   = (ISpecialMob) world.getEntityByID( message.entityId );
				if( mob != null ) {
					SpecialMobData data = mob.getSpecialData( );
					data.loadTextures( message.texturePaths );
				}
			}
			catch( Exception ex ) {
				SpecialMobsMod.log( ).error( "Failed to fetch mob texture from server for Entity:{}[{}]", message.entityId, message.texturePaths, ex );
			}
			return null;
		}
	}
}
