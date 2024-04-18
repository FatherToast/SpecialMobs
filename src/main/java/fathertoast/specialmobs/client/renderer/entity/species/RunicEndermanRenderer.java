package fathertoast.specialmobs.client.renderer.entity.species;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import fathertoast.specialmobs.client.renderer.entity.family.SpecialEndermanRenderer;
import fathertoast.specialmobs.common.entity.enderman.RunicEndermanEntity;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class RunicEndermanRenderer extends SpecialEndermanRenderer {
    public static final ResourceLocation BEAM_TEXTURE_LOCATION = new ResourceLocation( "textures/entity/end_crystal/end_crystal_beam.png" );
    private static final RenderType BEAM = RenderType.entitySmoothCutout( BEAM_TEXTURE_LOCATION );
    
    public RunicEndermanRenderer( EntityRendererProvider.Context context ) { super( context ); }
    
    @Override
    public void render( EnderMan entity, float rotation, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight ) {
        // Beam attack
        final RunicEndermanEntity.BeamState beamState = ((RunicEndermanEntity) entity).getBeamState();
        if( beamState != RunicEndermanEntity.BeamState.OFF ) {
            poseStack.pushPose();
            
            Vec3 beamVec = entity.getViewVector( partialTicks ).scale( RunicEndermanEntity.BEAM_MAX_RANGE );
            
            final Vec3 beamStartPos = entity.getEyePosition( partialTicks );
            final Vec3 beamEndPos = beamStartPos.add( beamVec );
            
            final HitResult blockRayTrace = entity.level().clip(
                    new ClipContext( beamStartPos, beamEndPos,
                            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity ) );
            if( blockRayTrace.getType() != HitResult.Type.MISS ) {
                beamVec = blockRayTrace.getLocation().subtract( beamStartPos );
            }
            
            renderBeamAttack( beamState, entity.getEyeHeight(), (float) beamVec.x, (float) beamVec.y, (float) beamVec.z,
                    partialTicks, entity.tickCount, poseStack, buffer, packedLight );

            poseStack.popPose();
        }
        
        super.render( entity, rotation, partialTicks, poseStack, buffer, packedLight );
    }
    
    @Override
    protected boolean isBodyVisible( EnderMan entity ) {
        // This is called right after EntityModel#setupAnim, so we add on our own animation here as a hacky way to avoid a lot of extra stuff
        final RunicEndermanEntity.BeamState beamState = ((RunicEndermanEntity) entity).getBeamState();
        if( beamState != RunicEndermanEntity.BeamState.OFF ) {
            final float zRot = beamState == RunicEndermanEntity.BeamState.DAMAGING ? 0.25F : 0.6F;
            final EndermanModel<EnderMan> model = getModel();
            model.leftArm.xRot = -0.6F;
            model.rightArm.xRot = -0.6F;
            model.leftArm.zRot = -zRot;
            model.rightArm.zRot = zRot;
        }
        
        return super.isBodyVisible( entity );
    }
    
    /**
     * Copy-paste Ender Crystal render code.
     * {@link EnderDragonRenderer#renderCrystalBeams(float, float, float, float, int, PoseStack, MultiBufferSource, int)}
     */
    private void renderBeamAttack( RunicEndermanEntity.BeamState beamState, float offsetY, float dX, float dY, float dZ,
                                   float partialTicks, int tickCount, PoseStack poseStack, MultiBufferSource buffer, int packedLight ) {
        poseStack.pushPose();
        
        final float dH = Mth.sqrt( dX * dX + dZ * dZ );
        final float length = Mth.sqrt( dX * dX + dY * dY + dZ * dZ );
        
        poseStack.translate( 0.0, offsetY, 0.0 );
        poseStack.mulPose( Axis.YP.rotation( (float) -Math.atan2( dZ, dX ) - (float) Math.PI / 2.0F ) );
        poseStack.mulPose( Axis.XP.rotation( (float) -Math.atan2( dH, dY ) - (float) Math.PI / 2.0F ) );
        
        final VertexConsumer vertexConsumer = buffer.getBuffer( BEAM );
        final PoseStack.Pose matrixEntry = poseStack.last();
        final Matrix4f pose = matrixEntry.pose();
        final Matrix3f normal = matrixEntry.normal();
        
        final int c1, c2;
        final float endScale;
        final float v1;
        if( beamState == RunicEndermanEntity.BeamState.DAMAGING ) {
            // Same properties as end crystal beam; black at beam start point transitioning to magenta at end point,
            //  beam is wider at end point (conical), and animation scrolls the texture towards the end point
            c1 = 0;
            c2 = 255;
            endScale = 1.0F;
            v1 = (tickCount + partialTicks) * -0.01F;
        }
        else {
            // Make the whole beam gray, uniform width, and animation reversed
            c1 = 100;
            c2 = 100;
            endScale = 0.2F;
            v1 = (tickCount + partialTicks) * 0.006F;
        }
        final float v2 = length / 32.0F + v1;
        
        float u1 = 0.0F;
        float x1 = 0.0F;
        float y1 = 0.75F;
        
        final int resolution = 8;
        for( int n = 1; n <= resolution; n++ ) {
            final float u2 = (float) n / resolution;
            final float angle = u2 * (float) Math.PI * 2.0F;
            final float x2 = Mth.sin( angle ) * 0.75F;
            final float y2 = Mth.cos( angle ) * 0.75F;

            vertexConsumer.vertex( pose, x1 * 0.2F, y1 * 0.2F, 0.0F )
                    .color( c1, c1, c1, 255 )
                    .uv( u1, v1 ).overlayCoords( OverlayTexture.NO_OVERLAY ).uv2( packedLight )
                    .normal( normal, 0.0F, -1.0F, 0.0F ).endVertex();
            vertexConsumer.vertex( pose, x1 * endScale, y1 * endScale, length )
                    .color( c2, 100, c2, 255 )
                    .uv( u1, v2 ).overlayCoords( OverlayTexture.NO_OVERLAY ).uv2( packedLight )
                    .normal( normal, 0.0F, -1.0F, 0.0F ).endVertex();
            vertexConsumer.vertex( pose, x2 * endScale, y2 * endScale, length )
                    .color( c2, 100, c2, 255 )
                    .uv( u2, v2 ).overlayCoords( OverlayTexture.NO_OVERLAY ).uv2( packedLight )
                    .normal( normal, 0.0F, -1.0F, 0.0F ).endVertex();
            vertexConsumer.vertex( pose, x2 * 0.2F, y2 * 0.2F, 0.0F )
                    .color( c1, c1, c1, 255 )
                    .uv( u2, v1 ).overlayCoords( OverlayTexture.NO_OVERLAY ).uv2( packedLight )
                    .normal( normal, 0.0F, -1.0F, 0.0F ).endVertex();
            
            x1 = x2;
            y1 = y2;
            u1 = u2;
        }
        
        poseStack.popPose();
    }
}