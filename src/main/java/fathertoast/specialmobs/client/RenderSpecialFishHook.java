package fathertoast.specialmobs.client;

import fathertoast.specialmobs.entity.projectile.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
class RenderSpecialFishHook extends Render< EntitySpecialFishHook >
{
	private static final ResourceLocation SPRITESHEET = new ResourceLocation( "textures/particle/particles.png" );
	
	// The u,v position of the sprite to use; with 0,0 being the top-left of the spritesheet.
	private static final int INDEX_U = 1;
	private static final int INDEX_V = 2;
	
	// How many sprites are in each row/column of the spritesheet.
	private static final int SPRITESHEET_RESOLUTION = 16;
	// The width/height of the sprite measured in 1x1 sprites, to accomodate sprites that take up multiple indexes.
	private static final int SPRITE_SIZE            = 1;
	
	// Sprite vertex positions on the spritesheet.
	private static final float U_MIN = (float) INDEX_U / (float) SPRITESHEET_RESOLUTION;
	private static final float U_MAX = U_MIN + (float) SPRITE_SIZE / (float) SPRITESHEET_RESOLUTION;
	private static final float V_MIN = (float) INDEX_V / (float) SPRITESHEET_RESOLUTION;
	private static final float V_MAX = V_MIN + (float) SPRITE_SIZE / (float) SPRITESHEET_RESOLUTION;
	
	RenderSpecialFishHook( RenderManager renderManager )
	{
		super( renderManager );
	}
	
	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	@Override
	protected
	ResourceLocation getEntityTexture( EntitySpecialFishHook entity )
	{
		return SPRITESHEET;
	}
	
	/**
	 * Renders the desired {@code T} type Entity.
	 */
	@Override
	public
	void doRender( EntitySpecialFishHook hook, double x, double y, double z, float entityYaw, float partialTicks )
	{
		EntityLiving angler = hook.getAngler( );
		if( angler != null && !renderOutlines ) {
			// Draw the hook
			GlStateManager.pushMatrix( );
			GlStateManager.translate( (float) x, (float) y, (float) z );
			GlStateManager.enableRescaleNormal( );
			GlStateManager.scale( 0.5F, 0.5F, 0.5F );
			bindEntityTexture( hook );
			
			Tessellator   tessellator = Tessellator.getInstance( );
			BufferBuilder buffer      = tessellator.getBuffer( );
			
			GlStateManager.rotate( 180.0F - renderManager.playerViewY, 0.0F, 1.0F, 0.0F );
			GlStateManager.rotate( (renderManager.options.thirdPersonView == 2 ? -1 : 1) * -renderManager.playerViewX, 1.0F, 0.0F, 0.0F );
			
			if( renderOutlines ) {
				GlStateManager.enableColorMaterial( );
				GlStateManager.enableOutlineMode( getTeamColor( hook ) );
			}
			
			final float center = 0.5F;
			buffer.begin( 7, DefaultVertexFormats.POSITION_TEX_NORMAL );
			buffer.pos( -center, -center, 0.0 ).tex( U_MIN, V_MAX ).normal( 0.0F, 1.0F, 0.0F ).endVertex( );
			buffer.pos( center, -center, 0.0 ).tex( U_MAX, V_MAX ).normal( 0.0F, 1.0F, 0.0F ).endVertex( );
			buffer.pos( center, center, 0.0 ).tex( U_MAX, V_MIN ).normal( 0.0F, 1.0F, 0.0F ).endVertex( );
			buffer.pos( -center, center, 0.0 ).tex( U_MIN, V_MIN ).normal( 0.0F, 1.0F, 0.0F ).endVertex( );
			tessellator.draw( );
			
			if( renderOutlines ) {
				GlStateManager.disableOutlineMode( );
				GlStateManager.disableColorMaterial( );
			}
			
			GlStateManager.disableRescaleNormal( );
			GlStateManager.popMatrix( );
			
			// Draw the fishing line
			final float  anglerYaw = (angler.prevRenderYawOffset + (angler.renderYawOffset - angler.prevRenderYawOffset) * partialTicks) * (float) Math.PI / 180.0F;
			final double sinYaw    = MathHelper.sin( anglerYaw );
			final double cosYaw    = MathHelper.cos( anglerYaw );
			
			final boolean armsRaised = angler instanceof EntityZombie && ((EntityZombie) angler).isArmsRaised( );
			
			final double rightOffset   = 0.30 * (angler.getPrimaryHand( ) == EnumHandSide.RIGHT ? 1 : -1);
			final double forwardOffset = 0.65 + (armsRaised ? -0.7 : 0.0);
			final double upwardOffset  = 0.40 + angler.getEyeHeight( ) + (angler.isSneaking( ) ? -0.1875 : 0.0) + (armsRaised ? 0.2 : 0.0);
			
			final double dX = angler.prevPosX + (angler.posX - angler.prevPosX) * partialTicks - cosYaw * rightOffset - sinYaw * forwardOffset -
			                  (hook.prevPosX + (hook.posX - hook.prevPosX) * partialTicks);
			
			final double dY = angler.prevPosY + (angler.posY - angler.prevPosY) * partialTicks + upwardOffset -
			                  (hook.prevPosY + (hook.posY - hook.prevPosY) * partialTicks + 0.25);
			
			final double dZ = angler.prevPosZ + (angler.posZ - angler.prevPosZ) * partialTicks - sinYaw * rightOffset + cosYaw * forwardOffset -
			                  hook.prevPosZ + (hook.posZ - hook.prevPosZ) * partialTicks;
			
			GlStateManager.disableTexture2D( );
			GlStateManager.disableLighting( );
			
			buffer.begin( 3, DefaultVertexFormats.POSITION_COLOR );
			final int fishingLineVerts = 16;
			for( int v = 0; v <= fishingLineVerts; v++ ) {
				float vN = (float) v / (float) fishingLineVerts;
				buffer.pos( x + dX * vN, y + dY * (vN * vN + vN) * 0.5 + 0.25, z + dZ * vN ).color( 0, 0, 0, 255 ).endVertex( );
			}
			tessellator.draw( );
			
			GlStateManager.enableLighting( );
			GlStateManager.enableTexture2D( );
			
			super.doRender( hook, x, y, z, entityYaw, partialTicks );
		}
	}
}
