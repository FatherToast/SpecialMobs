package fathertoast.specialmobs.client;

import fathertoast.specialmobs.entity.*;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
class RenderSpecialZombie extends RenderZombie
{
	RenderSpecialZombie( RenderManager renderManager )
	{
		super( renderManager );
		addLayer( new LayerSpecialMobEyes<>( this ) );
		addLayer( new LayerSpecialMobOverlay<>( this, new ModelZombie( 0.25F, false ) ) );
	}
	
	// Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	@Override
	protected
	ResourceLocation getEntityTexture( EntityZombie entity )
	{
		return ((ISpecialMob) entity).getSpecialData( ).getTexture( );
	}
	
	// Allows the render to do any OpenGL state modifications necessary before the model is rendered.
	@Override
	protected
	void preRenderCallback( EntityZombie entity, float partialTick )
	{
		super.preRenderCallback( entity, partialTick );
		float scale = ((ISpecialMob) entity).getSpecialData( ).getRenderScale( );
		shadowSize = 0.5F * scale;
		GlStateManager.scale( scale, scale, scale );
	}
}
