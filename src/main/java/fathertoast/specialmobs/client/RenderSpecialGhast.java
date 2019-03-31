package fathertoast.specialmobs.client;

import fathertoast.specialmobs.entity.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderGhast;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
class RenderSpecialGhast extends RenderGhast
{
	RenderSpecialGhast( RenderManager renderManager )
	{
		super( renderManager );
		// Requires some additional tweaks to make glowing eyes animate with the base texture - okay for now
		addLayer( new LayerSpecialMobEyes<>( this ) );
		// Model doesn't support size parameter - overlay texture is applied to the animation instead
		//addLayer( new LayerSpecialMobOverlay<>( this, new ModelGhast( 0.25F ) ) );
	}
	
	// Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	@Override
	protected
	ResourceLocation getEntityTexture( EntityGhast entity )
	{
		SpecialMobData data = ((ISpecialMob) entity).getSpecialData( );
		return entity.isAttacking( ) && data.hasOverlayTexture( ) ? data.getTextureOverlay( ) : data.getTexture( );
	}
	
	// Allows the render to do any OpenGL state modifications necessary before the model is rendered.
	@Override
	protected
	void preRenderCallback( EntityGhast entity, float partialTick )
	{
		super.preRenderCallback( entity, partialTick );
		float scale = ((ISpecialMob) entity).getSpecialData( ).getRenderScale( );
		shadowSize = 0.5F * scale;
		GlStateManager.scale( scale, scale, scale );
	}
}
