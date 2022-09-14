package fathertoast.specialmobs.client.renderer.entity.layers;

import com.mojang.blaze3d.matrix.MatrixStack;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn( Dist.CLIENT )
public class SpecialMobOverlayLayer<T extends LivingEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> {
    
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
        
        coloredCutoutModelCopyLayerRender( getParentModel(), layerModel, overlayTexture, matrixStack, buffer,
                packedLight, entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTicks,
                1.0F, 1.0F, 1.0F ); // RGB
    }
}