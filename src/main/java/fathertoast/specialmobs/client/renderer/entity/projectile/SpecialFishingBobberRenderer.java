package fathertoast.specialmobs.client.renderer.entity.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import fathertoast.specialmobs.common.entity.projectile.SpecialFishingBobberEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class SpecialFishingBobberRenderer extends EntityRenderer<SpecialFishingBobberEntity> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation( "textures/entity/fishing_hook.png" );
    private static final RenderType RENDER_TYPE = RenderType.entityCutout( TEXTURE_LOCATION );
    
    public SpecialFishingBobberRenderer( EntityRendererProvider.Context context ) { super( context ); }
    
    @Override
    public void render(SpecialFishingBobberEntity entity, float rotation, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight ) {
        final LivingEntity angler = entity.getLivingOwner();
        if( angler == null ) return;

        poseStack.pushPose();
        
        // Render the hook/bobber texture
        poseStack.pushPose();
        poseStack.scale( 0.5F, 0.5F, 0.5F );
        poseStack.mulPose( entityRenderDispatcher.cameraOrientation() );
        poseStack.mulPose( Axis.YP.rotationDegrees( 180.0F ) );
        final PoseStack.Pose matrixEntry = poseStack.last();
        drawQuad( buffer.getBuffer( RENDER_TYPE ), matrixEntry.pose(), matrixEntry.normal(), packedLight );
        poseStack.popPose();
        
        // Render the fishing line connecting the top of the bobber to the tip of angler's fishing rod
        
        // Define position of the fishing rod tip in local space
        final ItemStack heldItem = angler.getMainHandItem();
        // If main hand doesn't have a fishing rod, then we assume it's a mob that can't hold items
        final int handedness = heldItem.getItem() != Items.FISHING_ROD && heldItem.getItem() != Items.STICK ? 0 :
                angler.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        final double forwardOffset = 0.5;
        final double rightwardOffset = handedness * 0.35;
        final double upwardOffset = angler.getEyeHeight() - (angler.isCrouching() ? 0.1875F : 0.0F);
        
        // Convert local space offsets to global space
        final float yRot = Mth.lerp( partialTicks, angler.yBodyRotO, angler.yBodyRot ) * (float) Math.PI / 180.0F;
        final double forwardX = Mth.sin( yRot );
        final double forwardZ = Mth.cos( yRot );
        
        final double xRod = Mth.lerp( partialTicks, angler.xo, angler.getX() ) - forwardX * forwardOffset - forwardZ * rightwardOffset;
        final double yRod = Mth.lerp( partialTicks, angler.yo, angler.getY() ) + upwardOffset;
        final double zRod = Mth.lerp( partialTicks, angler.zo, angler.getZ() ) + forwardZ * forwardOffset - forwardX * rightwardOffset;
        
        final double xBobber = Mth.lerp( partialTicks, entity.xo, entity.getX() );
        final double yBobber = Mth.lerp( partialTicks, entity.yo, entity.getY() ) + 0.25;
        final double zBobber = Mth.lerp( partialTicks, entity.zo, entity.getZ() );

        drawLine( buffer.getBuffer( RenderType.lineStrip() ), poseStack.last(), 16,
                xBobber, yBobber, zBobber, xRod, yRod, zRod );
        
        poseStack.popPose();
        
        super.render( entity, rotation, partialTicks, poseStack, buffer, packedLight );
    }
    
    /** Creates the vertexes for a quad. */
    private static void drawQuad(VertexConsumer vertexConsumer, Matrix4f pose, Matrix3f normal, int packedLight ) {
        quadVertex( vertexConsumer, pose, normal, packedLight, 0.0F, 0.0F, 0, 1 );
        quadVertex( vertexConsumer, pose, normal, packedLight, 1.0F, 0.0F, 1, 1 );
        quadVertex( vertexConsumer, pose, normal, packedLight, 1.0F, 1.0F, 1, 0 );
        quadVertex( vertexConsumer, pose, normal, packedLight, 0.0F, 1.0F, 0, 0 );
    }
    
    private static void quadVertex( VertexConsumer vertexConsumer, Matrix4f pose, Matrix3f normal, int packedLight,
                                    float dX, float dY, int u, int v ) {
        vertexConsumer.vertex( pose, dX - 0.5F, dY - 0.5F, 0.0F )
                .color( 255, 255, 255, 255 ) // RGBA - white is no tint
                .uv( u, v ).overlayCoords( OverlayTexture.NO_OVERLAY ).uv2( packedLight )
                .normal( normal, 0.0F, 1.0F, 0.0F ).endVertex();
    }
    
    /** Creates the vertexes for the fishing line. A line from the bobber to the rod that hangs down a little in the y-axis. */
    private static void drawLine( VertexConsumer vertexConsumer, PoseStack.Pose pose, int resolution,
                                  double x1, double y1, double z1, double x2, double y2, double z2 ) {
        final float dX = (float) (x2 - x1);
        final float dY = (float) (y2 - y1);
        final float dZ = (float) (z2 - z1);
        
        for( int segment = 0; segment < resolution; segment++ ) {
            // Each line segment is defined by 2 vertexes
            lineVertex( dX, dY, dZ, vertexConsumer, pose, segment, resolution );
        }
    }

    private static void lineVertex(float x, float y, float z, VertexConsumer vertexConsumer, PoseStack.Pose pose, float segment, float totalSegments) {
        final float r = segment / totalSegments;
        final float k = segment + 1 / totalSegments;

        float vertX = x * r;
        float vertY = y * (r * r + r) * 0.5F + 0.25F;
        float vertZ = z * r;

        float normalX = x * k - vertX;
        float normalY = y * (k * k + k) * 0.5F + 0.25F - vertY;
        float normalZ = z * k - vertZ;

        float squared = Mth.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);

        normalX /= squared;
        normalY /= squared;
        normalZ /= squared;

        vertexConsumer
                .vertex(pose.pose(), vertX, vertY, vertZ)
                .color(0, 0, 0, 255)
                .normal(pose.normal(), normalX, normalY, normalZ)
                .endVertex();
    }
    
    @Override
    public ResourceLocation getTextureLocation( SpecialFishingBobberEntity entity ) { return TEXTURE_LOCATION; }
}