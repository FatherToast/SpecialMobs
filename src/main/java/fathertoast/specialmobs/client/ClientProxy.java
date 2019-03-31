package fathertoast.specialmobs.client;

import fathertoast.specialmobs.*;
import fathertoast.specialmobs.ai.*;
import fathertoast.specialmobs.entity.blaze.*;
import fathertoast.specialmobs.entity.cavespider.*;
import fathertoast.specialmobs.entity.creeper.*;
import fathertoast.specialmobs.entity.enderman.*;
import fathertoast.specialmobs.entity.ghast.*;
import fathertoast.specialmobs.entity.lavaslime.*;
import fathertoast.specialmobs.entity.pigzombie.*;
import fathertoast.specialmobs.entity.projectile.*;
import fathertoast.specialmobs.entity.silverfish.*;
import fathertoast.specialmobs.entity.skeleton.*;
import fathertoast.specialmobs.entity.slime.*;
import fathertoast.specialmobs.entity.spider.*;
import fathertoast.specialmobs.entity.witch.*;
import fathertoast.specialmobs.entity.witherskeleton.*;
import fathertoast.specialmobs.entity.zombie.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public
class ClientProxy extends SidedModProxy
{
	@Override
	public
	void registerRenderers( )
	{
		RenderingRegistry.registerEntityRenderingHandler( Entity_SpecialBlaze.class, RenderSpecialBlaze::new );
		RenderingRegistry.registerEntityRenderingHandler( Entity_SpecialCaveSpider.class, RenderSpecialSpider::new );
		RenderingRegistry.registerEntityRenderingHandler( Entity_SpecialCreeper.class, RenderSpecialCreeper::new );
		RenderingRegistry.registerEntityRenderingHandler( Entity_SpecialEnderman.class, RenderSpecialEnderman::new );
		RenderingRegistry.registerEntityRenderingHandler( Entity_SpecialGhast.class, RenderSpecialGhast::new );
		RenderingRegistry.registerEntityRenderingHandler( Entity_SpecialLavaSlime.class, RenderSpecialLavaSlime::new );
		RenderingRegistry.registerEntityRenderingHandler( Entity_SpecialPigZombie.class, RenderSpecialZombie::new );
		RenderingRegistry.registerEntityRenderingHandler( Entity_SpecialSilverfish.class, RenderSpecialSilverfish::new );
		RenderingRegistry.registerEntityRenderingHandler( Entity_SpecialSkeleton.class, RenderSpecialSkeleton::new );
		RenderingRegistry.registerEntityRenderingHandler( Entity_SpecialSlime.class, RenderSpecialSlime::new );
		RenderingRegistry.registerEntityRenderingHandler( Entity_SpecialSpider.class, RenderSpecialSpider::new );
		RenderingRegistry.registerEntityRenderingHandler( Entity_SpecialWitch.class, RenderSpecialWitch::new );
		RenderingRegistry.registerEntityRenderingHandler( Entity_SpecialWitherSkeleton.class, RenderSpecialSkeleton::new );
		RenderingRegistry.registerEntityRenderingHandler( Entity_SpecialZombie.class, RenderSpecialZombie::new );
		
		RenderingRegistry.registerEntityRenderingHandler( EntitySpecialFishHook.class, RenderSpecialFishHook::new );
	}
	
	// Renders the entity as a block if it is a hiding ninja. Returns false if this doesn't render anything (the ninja is not hiding).
	public static
	< T extends EntityLiving & INinja >
	boolean tryRenderingHiddenNinja( Render renderer, boolean renderOutlines, int teamColor, T entity, double x, double y, double z, float entityYaw, float partialTicks )
	{
		IBlockState block = entity.getDisguiseBlock( );
		if( block != null && block.getRenderType( ) == EnumBlockRenderType.MODEL ) {
			renderer.bindTexture( TextureMap.LOCATION_BLOCKS_TEXTURE );
			GlStateManager.pushMatrix( );
			GlStateManager.disableLighting( );
			
			Tessellator   tessellator   = Tessellator.getInstance( );
			BufferBuilder bufferBuilder = tessellator.getBuffer( );
			
			if( renderOutlines ) {
				GlStateManager.enableColorMaterial( );
				GlStateManager.enableOutlineMode( teamColor );
			}
			
			bufferBuilder.begin( 7, DefaultVertexFormats.BLOCK );
			
			BlockPos pos = new BlockPos( entity );
			GlStateManager.translate( (float) (x - pos.getX( ) - 0.5), (float) (y - pos.getY( )), (float) (z - pos.getZ( ) - 0.5) );
			
			BlockRendererDispatcher blockRenderManager = Minecraft.getMinecraft( ).getBlockRendererDispatcher( );
			blockRenderManager.getBlockModelRenderer( ).renderModel(
				entity.world, blockRenderManager.getModelForState( block ),
				block, pos, bufferBuilder, false, MathHelper.getPositionRandom( pos )
			);
			
			tessellator.draw( );
			
			if( renderOutlines ) {
				GlStateManager.disableOutlineMode( );
				GlStateManager.disableColorMaterial( );
			}
			
			GlStateManager.enableLighting( );
			GlStateManager.popMatrix( );
			return true;
		}
		return false;
	}
}
