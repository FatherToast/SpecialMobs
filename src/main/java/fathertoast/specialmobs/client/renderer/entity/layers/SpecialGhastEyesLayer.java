package fathertoast.specialmobs.client.renderer.entity.layers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.AbstractEyesLayer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.monster.GhastEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn( Dist.CLIENT )
public class SpecialGhastEyesLayer<T extends GhastEntity, M extends EntityModel<T>> extends AbstractEyesLayer<T, M> {
    private final RenderType FALLBACK = RenderType.eyes( new ResourceLocation( "textures/entity/spider_eyes.png" ) );
    private final ResourceLocation eyes;
    private final ResourceLocation shootEyes;
    
    
    public SpecialGhastEyesLayer( IEntityRenderer<T, M> renderer, ResourceLocation eyes, ResourceLocation shootEyes ) {
        super( renderer );
        this.eyes = eyes;
        this.shootEyes = shootEyes;
    }
    
    @Override
    public void render( MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, T ghast, float limbSwing,
                        float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch ) {
        
        final IVertexBuilder vertexBuilder = buffer.getBuffer( RenderType.entityCutout( ghast.isCharging() ? shootEyes : eyes ) );
        getParentModel().renderToBuffer( matrixStack, vertexBuilder, LightTexture.pack( 15, 15 ), OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F );
    }
    
    @Override
    public RenderType renderType() {
        SpecialMobs.LOG.warn( "Something is attempting to get eye layer 'render type' for some reason! :(" );
        return FALLBACK;
    }
}