package fathertoast.specialmobs.client;

import fathertoast.specialmobs.entity.*;
import net.minecraft.client.model.ModelWitch;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderWitch;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
class RenderSpecialWitch extends RenderWitch
{
	RenderSpecialWitch( RenderManager renderManager )
	{
		super( renderManager );
		addLayer( new LayerSpecialMobEyes<>( this ) );
		// Note: Overlay scaling only applies to base villager model
		addLayer( new LayerSpecialMobOverlay<>( this, new ModelWitch( 0.25F ) ) );
	}
	
	// Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	@Override
	protected
	ResourceLocation getEntityTexture( EntityWitch entity )
	{
		return ((ISpecialMob) entity).getSpecialData( ).getTexture( );
	}
	
	// Allows the render to do any OpenGL state modifications necessary before the model is rendered.
	@Override
	protected
	void preRenderCallback( EntityWitch entity, float partialTick )
	{
		super.preRenderCallback( entity, partialTick );
		float scale = ((ISpecialMob) entity).getSpecialData( ).getRenderScale( );
		shadowSize = 0.5F * scale;
		GlStateManager.scale( scale, scale, scale );
	}
}
