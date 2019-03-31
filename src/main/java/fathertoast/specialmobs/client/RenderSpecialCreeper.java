package fathertoast.specialmobs.client;

import fathertoast.specialmobs.entity.*;
import net.minecraft.client.model.ModelCreeper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderCreeper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
class RenderSpecialCreeper extends RenderCreeper
{
	RenderSpecialCreeper( RenderManager renderManager )
	{
		super( renderManager );
		addLayer( new LayerSpecialMobEyes<>( this ) );
		addLayer( new LayerSpecialMobOverlay<>( this, new ModelCreeper( 0.25F ) ) );
	}
	
	// Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	@Override
	protected
	ResourceLocation getEntityTexture( EntityCreeper entity )
	{
		return ((ISpecialMob) entity).getSpecialData( ).getTexture( );
	}
	
	// Allows the render to do any OpenGL state modifications necessary before the model is rendered.
	@Override
	protected
	void preRenderCallback( EntityCreeper entity, float partialTick )
	{
		super.preRenderCallback( entity, partialTick );
		float scale = ((ISpecialMob) entity).getSpecialData( ).getRenderScale( );
		shadowSize = 0.5F * scale;
		GlStateManager.scale( scale, scale, scale );
	}
}
