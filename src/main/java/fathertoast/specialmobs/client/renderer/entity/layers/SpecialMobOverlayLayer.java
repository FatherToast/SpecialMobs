package fathertoast.specialmobs.client.renderer.entity.layers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@OnlyIn( Dist.CLIENT )
public class SpecialMobOverlayLayer<T extends Entity, M extends EntityModel<T>> extends LayerRenderer<T, M> {
    
    private final M layerModel;
    
    public SpecialMobOverlayLayer( IEntityRenderer<T, M> renderer, M model ) {
        super( renderer );
        layerModel = model;
    }

    @Override
    public void render( MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, T entity,
                        float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch ) {
        final ResourceLocation overlayTexture = ((ISpecialMob<?>) entity).getSpecialData().getTextureOverlay();
        if( overlayTexture == null ) return;
        
        //TODO this renders oddly dark, look for way to fix lighting
        
        //float f0 = (float) entity.tickCount + c;
        layerModel.prepareMobModel( entity, limbSwing, limbSwingAmount, partialTicks );
        this.getParentModel().copyPropertiesTo( layerModel );
        IVertexBuilder vertexBuilder = buffer.getBuffer( RenderType.entityCutoutNoCull( overlayTexture, false ) );
        layerModel.setupAnim( entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch );
        layerModel.renderToBuffer( matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY,
                0.5F, 0.5F, 0.5F, 1.0F );
    }
}