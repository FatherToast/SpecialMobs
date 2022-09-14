package fathertoast.specialmobs.client.renderer.entity.species;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import fathertoast.specialmobs.client.renderer.entity.family.SpecialEndermanRenderer;
import fathertoast.specialmobs.common.entity.enderman.RunicEndermanEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.EndermanModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class RunicEndermanRenderer extends SpecialEndermanRenderer {
    public static final ResourceLocation BEAM_TEXTURE_LOCATION = new ResourceLocation( "textures/entity/end_crystal/end_crystal_beam.png" );
    private static final RenderType BEAM = RenderType.entitySmoothCutout( BEAM_TEXTURE_LOCATION );
    
    public RunicEndermanRenderer( EntityRendererManager rendererManager ) { super( rendererManager ); }
    
    @Override
    public void render( EndermanEntity entity, float rotation, float partialTicks,
                        MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight ) {
        // Beam attack
        final RunicEndermanEntity.BeamState beamState = ((RunicEndermanEntity) entity).getBeamState();
        if( beamState != RunicEndermanEntity.BeamState.OFF ) {
            matrixStack.pushPose();
            
            Vector3d beamVec = entity.getViewVector( partialTicks ).scale( RunicEndermanEntity.BEAM_MAX_RANGE );
            
            final Vector3d beamStartPos = entity.getEyePosition( partialTicks );
            final Vector3d beamEndPos = beamStartPos.add( beamVec );
            
            final RayTraceResult blockRayTrace = entity.level.clip(
                    new RayTraceContext( beamStartPos, beamEndPos,
                            RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity ) );
            if( blockRayTrace.getType() != RayTraceResult.Type.MISS ) {
                beamVec = blockRayTrace.getLocation().subtract( beamStartPos );
            }
            
            renderBeamAttack( beamState, entity.getEyeHeight(), (float) beamVec.x, (float) beamVec.y, (float) beamVec.z,
                    partialTicks, entity.tickCount, matrixStack, buffer, packedLight );
            
            matrixStack.popPose();
        }
        
        super.render( entity, rotation, partialTicks, matrixStack, buffer, packedLight );
    }
    
    @Override
    protected boolean isBodyVisible( EndermanEntity entity ) {
        // This is called right after EntityModel#setupAnim, so we add on our own animation here as a hacky way to avoid a lot of extra stuff
        final RunicEndermanEntity.BeamState beamState = ((RunicEndermanEntity) entity).getBeamState();
        if( beamState != RunicEndermanEntity.BeamState.OFF ) {
            final float zRot = beamState == RunicEndermanEntity.BeamState.DAMAGING ? 0.25F : 0.6F;
            final EndermanModel<EndermanEntity> model = getModel();
            model.leftArm.xRot = -0.6F;
            model.rightArm.xRot = -0.6F;
            model.leftArm.zRot = -zRot;
            model.rightArm.zRot = zRot;
        }
        
        return super.isBodyVisible( entity );
    }
    
    /**
     * Copy-paste Ender Crystal render code.
     * {@link EnderDragonRenderer#renderCrystalBeams(float, float, float, float, int, MatrixStack, IRenderTypeBuffer, int)}
     */
    private void renderBeamAttack( RunicEndermanEntity.BeamState beamState, float offsetY, float dX, float dY, float dZ,
                                   float partialTicks, int tickCount, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight ) {
        matrixStack.pushPose();
        
        final float dH = MathHelper.sqrt( dX * dX + dZ * dZ );
        final float length = MathHelper.sqrt( dX * dX + dY * dY + dZ * dZ );
        
        matrixStack.translate( 0.0, offsetY, 0.0 );
        matrixStack.mulPose( Vector3f.YP.rotation( (float) -Math.atan2( dZ, dX ) - (float) Math.PI / 2.0F ) );
        matrixStack.mulPose( Vector3f.XP.rotation( (float) -Math.atan2( dH, dY ) - (float) Math.PI / 2.0F ) );
        
        final IVertexBuilder vertexBuilder = buffer.getBuffer( BEAM );
        final MatrixStack.Entry matrixEntry = matrixStack.last();
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
            final float x2 = MathHelper.sin( angle ) * 0.75F;
            final float y2 = MathHelper.cos( angle ) * 0.75F;
            
            vertexBuilder.vertex( pose, x1 * 0.2F, y1 * 0.2F, 0.0F )
                    .color( c1, c1, c1, 255 )
                    .uv( u1, v1 ).overlayCoords( OverlayTexture.NO_OVERLAY ).uv2( packedLight )
                    .normal( normal, 0.0F, -1.0F, 0.0F ).endVertex();
            vertexBuilder.vertex( pose, x1 * endScale, y1 * endScale, length )
                    .color( c2, 100, c2, 255 )
                    .uv( u1, v2 ).overlayCoords( OverlayTexture.NO_OVERLAY ).uv2( packedLight )
                    .normal( normal, 0.0F, -1.0F, 0.0F ).endVertex();
            vertexBuilder.vertex( pose, x2 * endScale, y2 * endScale, length )
                    .color( c2, 100, c2, 255 )
                    .uv( u2, v2 ).overlayCoords( OverlayTexture.NO_OVERLAY ).uv2( packedLight )
                    .normal( normal, 0.0F, -1.0F, 0.0F ).endVertex();
            vertexBuilder.vertex( pose, x2 * 0.2F, y2 * 0.2F, 0.0F )
                    .color( c1, c1, c1, 255 )
                    .uv( u2, v1 ).overlayCoords( OverlayTexture.NO_OVERLAY ).uv2( packedLight )
                    .normal( normal, 0.0F, -1.0F, 0.0F ).endVertex();
            
            x1 = x2;
            y1 = y2;
            u1 = u2;
        }
        
        matrixStack.popPose();
    }
}