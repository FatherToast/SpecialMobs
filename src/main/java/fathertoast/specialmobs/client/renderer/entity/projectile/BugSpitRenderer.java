package fathertoast.specialmobs.client.renderer.entity.projectile;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import fathertoast.specialmobs.common.entity.projectile.BugSpitEntity;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.LlamaSpitModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn( Dist.CLIENT )
public class BugSpitRenderer extends EntityRenderer<BugSpitEntity> {
    private static final ResourceLocation TEXTURE_LOCATION = References.getEntityBaseTexture( "projectile", "bug_spit" );
    
    private final LlamaSpitModel<BugSpitEntity> model = new LlamaSpitModel<>();
    
    public BugSpitRenderer( EntityRendererManager rendererManager ) { super( rendererManager ); }
    
    @Override
    public void render( BugSpitEntity entity, float rotation, float partialTicks,
                        MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight ) {
        matrixStack.pushPose();
        matrixStack.translate( 0.0, 0.15, 0.0 );
        matrixStack.mulPose( Vector3f.YP.rotationDegrees( MathHelper.lerp( partialTicks, entity.yRotO, entity.yRot ) - 90.0F ) );
        matrixStack.mulPose( Vector3f.ZP.rotationDegrees( MathHelper.lerp( partialTicks, entity.xRotO, entity.xRot ) ) );
        
        model.setupAnim( entity, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F );
        final int color = entity.getColor();
        final IVertexBuilder vertexBuilder = buffer.getBuffer( model.renderType( TEXTURE_LOCATION ) );
        model.renderToBuffer( matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY,
                References.getRed( color ), References.getGreen( color ), References.getBlue( color ), 1.0F ); // RGBA
        matrixStack.popPose();
        
        super.render( entity, rotation, partialTicks, matrixStack, buffer, packedLight );
    }
    
    @Override
    public ResourceLocation getTextureLocation( BugSpitEntity entity ) { return TEXTURE_LOCATION; }
}