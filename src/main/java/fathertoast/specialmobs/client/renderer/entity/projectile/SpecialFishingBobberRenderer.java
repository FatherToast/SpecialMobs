package fathertoast.specialmobs.client.renderer.entity.projectile;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import fathertoast.specialmobs.common.entity.projectile.SpecialFishingBobberEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn( Dist.CLIENT )
public class SpecialFishingBobberRenderer extends EntityRenderer<SpecialFishingBobberEntity> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation( "textures/entity/fishing_hook.png" );
    private static final RenderType RENDER_TYPE = RenderType.entityCutout( TEXTURE_LOCATION );
    
    public SpecialFishingBobberRenderer( EntityRendererManager rendererManager ) { super( rendererManager ); }
    
    @Override
    public void render( SpecialFishingBobberEntity entity, float rotation, float partialTicks,
                        MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight ) {
        final LivingEntity angler = entity.getLivingOwner();
        if( angler == null ) return;
        
        matrixStack.pushPose();
        
        // Render the hook/bobber texture
        matrixStack.pushPose();
        matrixStack.scale( 0.5F, 0.5F, 0.5F );
        matrixStack.mulPose( entityRenderDispatcher.cameraOrientation() );
        matrixStack.mulPose( Vector3f.YP.rotationDegrees( 180.0F ) );
        final MatrixStack.Entry matrixEntry = matrixStack.last();
        drawQuad( buffer.getBuffer( RENDER_TYPE ), matrixEntry.pose(), matrixEntry.normal(), packedLight );
        matrixStack.popPose();
        
        // Render the fishing line connecting the top of the bobber to the tip of angler's fishing rod
        
        // Define position of the fishing rod tip in local space
        final ItemStack heldItem = angler.getMainHandItem();
        // If main hand doesn't have a fishing rod, then we assume it's a mob that can't hold items
        final int handedness = heldItem.getItem() != Items.FISHING_ROD && heldItem.getItem() != Items.STICK ? 0 :
                angler.getMainArm() == HandSide.RIGHT ? 1 : -1;
        final double forwardOffset = 0.5;
        final double rightwardOffset = handedness * 0.35;
        final double upwardOffset = angler.getEyeHeight() - (angler.isCrouching() ? 0.1875F : 0.0F);
        
        // Convert local space offsets to global space
        final float yRot = MathHelper.lerp( partialTicks, angler.yBodyRotO, angler.yBodyRot ) * (float) Math.PI / 180.0F;
        final double forwardX = MathHelper.sin( yRot );
        final double forwardZ = MathHelper.cos( yRot );
        
        final double xRod = MathHelper.lerp( partialTicks, angler.xo, angler.getX() ) - forwardX * forwardOffset - forwardZ * rightwardOffset;
        final double yRod = MathHelper.lerp( partialTicks, angler.yo, angler.getY() ) + upwardOffset;
        final double zRod = MathHelper.lerp( partialTicks, angler.zo, angler.getZ() ) + forwardZ * forwardOffset - forwardX * rightwardOffset;
        
        final double xBobber = MathHelper.lerp( partialTicks, entity.xo, entity.getX() );
        final double yBobber = MathHelper.lerp( partialTicks, entity.yo, entity.getY() ) + 0.25;
        final double zBobber = MathHelper.lerp( partialTicks, entity.zo, entity.getZ() );
        
        drawLine( buffer.getBuffer( RenderType.lines() ), matrixStack.last().pose(), 16,
                xBobber, yBobber, zBobber, xRod, yRod, zRod );
        
        matrixStack.popPose();
        
        super.render( entity, rotation, partialTicks, matrixStack, buffer, packedLight );
    }
    
    /** Creates the vertexes for a quad. */
    private static void drawQuad( IVertexBuilder vertexBuilder, Matrix4f pose, Matrix3f normal, int packedLight ) {
        quadVertex( vertexBuilder, pose, normal, packedLight, 0.0F, 0.0F, 0, 1 );
        quadVertex( vertexBuilder, pose, normal, packedLight, 1.0F, 0.0F, 1, 1 );
        quadVertex( vertexBuilder, pose, normal, packedLight, 1.0F, 1.0F, 1, 0 );
        quadVertex( vertexBuilder, pose, normal, packedLight, 0.0F, 1.0F, 0, 0 );
    }
    
    private static void quadVertex( IVertexBuilder vertexBuilder, Matrix4f pose, Matrix3f normal, int packedLight,
                                    float dX, float dY, int u, int v ) {
        vertexBuilder.vertex( pose, dX - 0.5F, dY - 0.5F, 0.0F )
                .color( 255, 255, 255, 255 ) // RGBA - white is no tint
                .uv( u, v ).overlayCoords( OverlayTexture.NO_OVERLAY ).uv2( packedLight )
                .normal( normal, 0.0F, 1.0F, 0.0F ).endVertex();
    }
    
    /** Creates the vertexes for the fishing line. A line from the bobber to the rod that hangs down a little in the y-axis. */
    private static void drawLine( IVertexBuilder vertexBuilder, Matrix4f pose, int resolution,
                                  double x1, double y1, double z1, double x2, double y2, double z2 ) {
        final float dX = (float) (x2 - x1);
        final float dY = (float) (y2 - y1);
        final float dZ = (float) (z2 - z1);
        
        for( int segment = 0; segment < resolution; segment++ ) {
            // Each line segment is defined by 2 vertexes
            lineVertex( dX, dY, dZ, vertexBuilder, pose, segment, resolution );
            lineVertex( dX, dY, dZ, vertexBuilder, pose, segment + 1, resolution );
        }
    }
    
    private static void lineVertex( float dX, float dY, float dZ, IVertexBuilder vertexBuilder, Matrix4f pose, int segment, int totalSegments ) {
        final float r = (float) segment / totalSegments;
        vertexBuilder.vertex( pose, dX * r, dY * (r * r + r) * 0.5F + 0.25F, dZ * r )
                .color( 0, 0, 0, 255 ) // RGBA - black
                .endVertex();
    }
    
    @Override
    public ResourceLocation getTextureLocation( SpecialFishingBobberEntity entity ) { return TEXTURE_LOCATION; }
}