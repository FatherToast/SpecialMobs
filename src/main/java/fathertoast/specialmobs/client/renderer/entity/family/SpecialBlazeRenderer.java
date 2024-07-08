package fathertoast.specialmobs.client.renderer.entity.family;

import com.mojang.blaze3d.vertex.PoseStack;
import fathertoast.specialmobs.client.renderer.entity.layers.SMModelLayers;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobOverlayLayer;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import net.minecraft.client.model.BlazeModel;
import net.minecraft.client.renderer.entity.BlazeRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Blaze;

public class SpecialBlazeRenderer extends BlazeRenderer {
    
    private final float baseShadowRadius;
    
    public SpecialBlazeRenderer( EntityRendererProvider.Context context ) {
        super( context );
        baseShadowRadius = shadowRadius;
        // Unneeded, the whole model is full brightness
        //addLayer( new SpecialMobEyesLayer<>( this ) );
        addLayer( new SpecialMobOverlayLayer<>( this, new BlazeModel<>( context.bakeLayer( SMModelLayers.BLAZE_OUTER_LAYER ) ) ) );
    }
    
    @Override
    public ResourceLocation getTextureLocation( Blaze entity ) {
        return ((ISpecialMob<?>) entity).getSpecialData().getTexture();
    }
    
    @Override
    protected void scale(Blaze entity, PoseStack matrixStack, float partialTick ) {
        super.scale( entity, matrixStack, partialTick );
        
        final float scale = ((ISpecialMob<?>) entity).getSpecialData().getRenderScale();
        shadowRadius = baseShadowRadius * scale;
        matrixStack.scale( scale, scale, scale );
    }
}