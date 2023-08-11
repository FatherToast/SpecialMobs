package fathertoast.specialmobs.client.renderer.entity.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import fathertoast.specialmobs.common.entity.projectile.BugSpitEntity;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.client.model.LlamaSpitModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class BugSpitRenderer extends EntityRenderer<BugSpitEntity> {
    private static final ResourceLocation TEXTURE_LOCATION = References.getEntityBaseTexture( "projectile", "bug_spit" );
    
    private final LlamaSpitModel<BugSpitEntity> model;
    
    public BugSpitRenderer( EntityRendererProvider.Context context ) {
        super( context );
        model  = new LlamaSpitModel<>( context.bakeLayer( ModelLayers.LLAMA_SPIT ));
    }
    
    @Override
    public void render(BugSpitEntity entity, float rotation, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight ) {
        poseStack.pushPose();
        poseStack.translate( 0.0, 0.15, 0.0 );
        poseStack.mulPose( Vector3f.YP.rotationDegrees( Mth.lerp( partialTicks, entity.yRotO, entity.getYRot() ) - 90.0F ) );
        poseStack.mulPose( Vector3f.ZP.rotationDegrees( Mth.lerp( partialTicks, entity.xRotO, entity.getXRot() ) ) );
        
        model.setupAnim( entity, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F );
        final int color = entity.getColor();
        final VertexConsumer vertexConsumer = buffer.getBuffer( model.renderType( TEXTURE_LOCATION ) );
        model.renderToBuffer( poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY,
                References.getRed( color ), References.getGreen( color ), References.getBlue( color ), 1.0F ); // RGBA
        poseStack.popPose();
        
        super.render( entity, rotation, partialTicks, poseStack, buffer, packedLight );
    }
    
    @Override
    public ResourceLocation getTextureLocation( BugSpitEntity entity ) { return TEXTURE_LOCATION; }
}