package fathertoast.specialmobs.client.renderer.entity.family;

import com.mojang.blaze3d.vertex.PoseStack;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MagmaCubeRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.MagmaCube;

public class SpecialMagmaCubeRenderer extends MagmaCubeRenderer {
    
    private final float baseShadowRadius;
    
    public SpecialMagmaCubeRenderer( EntityRendererProvider.Context rendererManager ) {
        super( rendererManager );
        baseShadowRadius = shadowRadius;
        // Unneeded, the whole model is full brightness
        //addLayer( new SpecialMobEyesLayer<>( this ) );
        // Model doesn't support size parameter
        //addLayer( new SpecialMobOverlayLayer<>( this, new MagmaCubeModel<>( 0.25F ) ) );
    }
    
    @Override
    public ResourceLocation getTextureLocation( MagmaCube entity ) {
        return ((ISpecialMob<?>) entity).getSpecialData().getTexture();
    }
    
    @Override
    protected void scale(MagmaCube entity, PoseStack poseStack, float partialTick ) {
        super.scale( entity, poseStack, partialTick );
        
        final float scale = ((ISpecialMob<?>) entity).getSpecialData().getRenderScale();
        shadowRadius = baseShadowRadius * scale * entity.getSize(); // Factor slime size into shadow
        poseStack.scale( scale, scale, scale );
    }
}