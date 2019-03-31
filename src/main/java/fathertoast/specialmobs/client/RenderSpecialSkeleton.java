package fathertoast.specialmobs.client;

import fathertoast.specialmobs.ai.*;
import fathertoast.specialmobs.entity.*;
import net.minecraft.client.model.ModelSkeleton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSkeleton;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
class RenderSpecialSkeleton extends RenderSkeleton
{
	RenderSpecialSkeleton( RenderManager renderManager )
	{
		super( renderManager );
		addLayer( new LayerSpecialMobEyes<>( this ) );
		addLayer( new LayerSpecialMobOverlay<>( this, new ModelSkeleton( 0.25F, true ) ) );
	}
	
	// Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	@Override
	protected
	ResourceLocation getEntityTexture( AbstractSkeleton entity )
	{
		return ((ISpecialMob) entity).getSpecialData( ).getTexture( );
	}
	
	// Allows the render to do any OpenGL state modifications necessary before the model is rendered.
	@Override
	protected
	void preRenderCallback( AbstractSkeleton entity, float partialTick )
	{
		super.preRenderCallback( entity, partialTick );
		float scale = ((ISpecialMob) entity).getSpecialData( ).getRenderScale( );
		shadowSize = 0.5F * scale;
		GlStateManager.scale( scale, scale, scale );
	}
	
	@Override
	public
	void doRender( AbstractSkeleton entity, double x, double y, double z, float entityYaw, float partialTicks )
	{
		if( entity instanceof INinja && ClientProxy.tryRenderingHiddenNinja( this, renderOutlines, getTeamColor( entity ), (EntityLiving & INinja) entity, x, y, z, entityYaw, partialTicks ) ) {
			return;
		}
		super.doRender( entity, x, y, z, entityYaw, partialTicks );
	}
}
