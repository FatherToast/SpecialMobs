package fathertoast.specialmobs.client.renderer.entity.family;

import com.mojang.blaze3d.vertex.PoseStack;
import fathertoast.specialmobs.client.renderer.entity.layers.SMModelLayers;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobEyesLayer;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobOverlayLayer;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import net.minecraft.client.model.SilverfishModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SilverfishRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Silverfish;

public class SpecialSilverfishRenderer extends SilverfishRenderer {
    
    private final float baseShadowRadius;
    
    public SpecialSilverfishRenderer( EntityRendererProvider.Context context ) {
        super( context );
        baseShadowRadius = shadowRadius;
        addLayer( new SpecialMobEyesLayer<>( this ) );
        addLayer( new SpecialMobOverlayLayer<>( this, new SilverfishModel<>( context.bakeLayer( SMModelLayers.SILVERFISH_OUTER_LAYER ) ) ) );
    }
    
    @Override
    public ResourceLocation getTextureLocation( Silverfish entity ) {
        return ((ISpecialMob<?>) entity).getSpecialData().getTexture();
    }
    
    @Override
    protected void scale(Silverfish entity, PoseStack poseStack, float partialTick ) {
        super.scale( entity, poseStack, partialTick );
        
        final float scale = ((ISpecialMob<?>) entity).getSpecialData().getRenderScale();
        shadowRadius = baseShadowRadius * scale;
        poseStack.scale( scale, scale, scale );
    }
}