package fathertoast.specialmobs.client;

import fathertoast.specialmobs.entity.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSpider;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
class RenderSpecialSpider extends RenderSpider< EntitySpider >
{
	RenderSpecialSpider( RenderManager renderManager )
	{
		super( renderManager );
		layerRenderers.remove( layerRenderers.size( ) - 1 );
		addLayer( new LayerSpecialMobEyes<>( this ) );
		// Model doesn't support size parameter
		//addLayer( new LayerSpecialMobOverlay<>( this, new ModelSpider( 0.25F ) ) );
	}
	
	// Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	@Override
	protected
	ResourceLocation getEntityTexture( EntitySpider entity )
	{
		return ((ISpecialMob) entity).getSpecialData( ).getTexture( );
	}
	
	// Allows the render to do any OpenGL state modifications necessary before the model is rendered.
	@Override
	protected
	void preRenderCallback( EntitySpider entity, float partialTick )
	{
		super.preRenderCallback( entity, partialTick );
		float scale = ((ISpecialMob) entity).getSpecialData( ).getRenderScale( );
		shadowSize = 0.5F * scale;
		GlStateManager.scale( scale, scale, scale );
	}
}
