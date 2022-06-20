package fathertoast.specialmobs.client.renderer.entity.layers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.AbstractEyesLayer;
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
public class SpecialMobEyesLayer<T extends Entity, M extends EntityModel<T>> extends AbstractEyesLayer<T, M> {
    private final RenderType FALLBACK = RenderType.eyes( new ResourceLocation( "textures/entity/spider_eyes.png" ) );
    
    public SpecialMobEyesLayer( IEntityRenderer<T, M> renderer ) {
        super( renderer );
    }
    
    // I have no clue what all these floats mean, but also I don't really care right now.  Would be nice to know eventually.
    @Override
    public void render( MatrixStack matrixStack, IRenderTypeBuffer buffer, int layer, T entity,
                        float a, float b, float c, float d, float e, float f ) {
        final ResourceLocation eyesTexture = ((ISpecialMob<?>) entity).getSpecialData().getTextureEyes();
        if( eyesTexture == null ) return;
        
        //TODO does not work; for some reason, all the transparency renders as white
        IVertexBuilder ivertexbuilder = buffer.getBuffer( RenderType.eyes( eyesTexture ) );
        this.getParentModel().renderToBuffer( matrixStack, ivertexbuilder, 15728640, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F );
    }
    
    @Override
    public RenderType renderType() {
        SpecialMobs.LOG.warn( "Something is attempting to get eye layer 'render type' for some reason! :(" );
        return FALLBACK;
    }
}