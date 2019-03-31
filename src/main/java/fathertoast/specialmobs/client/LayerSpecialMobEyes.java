package fathertoast.specialmobs.client;

import fathertoast.specialmobs.entity.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLiving;

class LayerSpecialMobEyes< T extends EntityLiving > implements LayerRenderer< T >
{
	private final RenderLiving< T > specialMobRenderer;
	
	LayerSpecialMobEyes( RenderLiving< T > renderSpecial )
	{
		specialMobRenderer = renderSpecial;
	}
	
	@Override
	public
	void doRenderLayer( T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale )
	{
		SpecialMobData specialData = ((ISpecialMob) entity).getSpecialData( );
		if( !specialData.hasEyesTexture( ) )
			return;
		
		specialMobRenderer.bindTexture( specialData.getTextureEyes( ) );
		GlStateManager.enableBlend( );
		//GlStateManager.disableAlpha( ); // Removed from vanilla code
		GlStateManager.blendFunc( GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE );
		
		GlStateManager.disableLighting( );
		GlStateManager.depthMask( !entity.isInvisible( ) );
		OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, 61680.0F, 0.0F );
		GlStateManager.enableLighting( );
		
		GlStateManager.color( 1.0F, 1.0F, 1.0F, 1.0F );
		Minecraft.getMinecraft( ).entityRenderer.setupFogColor( true );
		specialMobRenderer.getMainModel( ).render( entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale );
		Minecraft.getMinecraft( ).entityRenderer.setupFogColor( false );
		
		specialMobRenderer.setLightmap( entity );
		GlStateManager.depthMask( true );
		GlStateManager.disableBlend( );
		//GlStateManager.enableAlpha( ); // Removed from vanilla code
	}
	
	@Override
	public
	boolean shouldCombineTextures( ) { return false; }
}
