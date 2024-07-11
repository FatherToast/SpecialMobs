package fathertoast.specialmobs.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class SpecialMobOverlayLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    
    private final M layerModel;
    
    public SpecialMobOverlayLayer( RenderLayerParent<T, M> renderer, M model ) {
        super( renderer );
        layerModel = model;
    }


    @Override
    public void render( PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch ) {

        final ResourceLocation overlayTexture = ((ISpecialMob<?>) entity).getSpecialData().getTextureOverlay();
        if( overlayTexture == null ) return;
        
        coloredCutoutModelCopyLayerRender( getParentModel(), layerModel, overlayTexture, poseStack, buffer,
                packedLight, entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTicks,
                1.0F, 1.0F, 1.0F ); // RGB
    }
}