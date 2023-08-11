package fathertoast.specialmobs.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class SpecialMobEyesLayer<T extends Entity, M extends EntityModel<T>> extends EyesLayer<T, M> {
    private static final RenderType FALLBACK = RenderType.eyes( new ResourceLocation( "textures/entity/spider_eyes.png" ) );
    
    public SpecialMobEyesLayer( RenderLayerParent<T, M> renderer ) {
        super( renderer );
    }
    
    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing,
                       float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch ) {
        final ResourceLocation eyesTexture = ((ISpecialMob<?>) entity).getSpecialData().getTextureEyes();
        if( eyesTexture == null ) return;
        
        final VertexConsumer vertexConsumer = buffer.getBuffer( RenderType.eyes( eyesTexture ) );
        getParentModel().renderToBuffer( poseStack, vertexConsumer, LightTexture.pack( 15, 15 ), OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F ); // RGBA
    }
    
    @Override
    public RenderType renderType() {
        SpecialMobs.LOG.warn( "Something is attempting to get eye layer 'render type' for some reason! :(" );
        return FALLBACK;
    }
}