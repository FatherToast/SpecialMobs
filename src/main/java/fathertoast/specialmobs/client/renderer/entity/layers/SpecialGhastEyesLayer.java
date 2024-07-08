package fathertoast.specialmobs.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Ghast;

public class SpecialGhastEyesLayer<T extends Ghast, M extends EntityModel<T>> extends EyesLayer<T, M> {
    private final RenderType FALLBACK = RenderType.eyes( new ResourceLocation( "textures/entity/spider_eyes.png" ) );
    private final ResourceLocation eyes;
    private final ResourceLocation shootEyes;
    
    
    public SpecialGhastEyesLayer( RenderLayerParent<T, M> renderer, ResourceLocation eyes, ResourceLocation shootEyes ) {
        super( renderer );
        this.eyes = eyes;
        this.shootEyes = shootEyes;
    }
    
    @Override
    public void render( PoseStack poseStack, MultiBufferSource buffer, int packedLight, T ghast, float limbSwing,
                       float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch ) {
        
        final VertexConsumer vertexConsumer = buffer.getBuffer( RenderType.entityCutout( ghast.isCharging() ? shootEyes : eyes ) );
        getParentModel().renderToBuffer( poseStack, vertexConsumer, LightTexture.pack( 15, 15 ), OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F );
    }
    
    @Override
    public RenderType renderType() {
        SpecialMobs.LOG.warn( "Something is attempting to get eye layer 'render type' for some reason! :(" );
        return FALLBACK;
    }
}