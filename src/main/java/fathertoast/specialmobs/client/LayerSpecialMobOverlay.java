package fathertoast.specialmobs.client;

import fathertoast.specialmobs.entity.*;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLiving;

class LayerSpecialMobOverlay< T extends EntityLiving > implements LayerRenderer< T >
{
	private final RenderLiving< T > specialMobRenderer;
	private final ModelBase         layerModel;
	
	LayerSpecialMobOverlay( RenderLiving< T > renderSpecial, ModelBase model )
	{
		specialMobRenderer = renderSpecial;
		layerModel = model;
	}
	
	@Override
	public
	void doRenderLayer( T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale )
	{
		SpecialMobData specialData = ((ISpecialMob) entity).getSpecialData( );
		if( !specialData.hasOverlayTexture( ) )
			return;
		
		layerModel.setModelAttributes( specialMobRenderer.getMainModel( ) );
		layerModel.setLivingAnimations( entity, limbSwing, limbSwingAmount, partialTicks );
		GlStateManager.color( 1.0F, 1.0F, 1.0F, 1.0F );
		specialMobRenderer.bindTexture( specialData.getTextureOverlay( ) );
		layerModel.render( entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale );
	}
	
	@Override
	public
	boolean shouldCombineTextures( ) { return true; }
}
