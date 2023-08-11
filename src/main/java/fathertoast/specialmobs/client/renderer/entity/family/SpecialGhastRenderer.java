package fathertoast.specialmobs.client.renderer.entity.family;

import com.mojang.blaze3d.vertex.PoseStack;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.entity.SpecialMobData;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.GhastRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Ghast;

public class SpecialGhastRenderer extends GhastRenderer {
    
    private final float baseShadowRadius;
    
    public SpecialGhastRenderer( EntityRendererProvider.Context context ) {
        super( context );
        baseShadowRadius = shadowRadius;
        // Would require some big changes to make glowing eyes animate with the base texture
        //addLayer( new SpecialMobEyesLayer<>( this ) );
        // Model doesn't support size parameter - overlay texture is applied to the animation instead
        //addLayer( new SpecialMobOverlayLayer<>( this, new GhastModel<>( 0.25F ) ) );
    }
    
    @Override
    public ResourceLocation getTextureLocation( Ghast entity ) {
        final SpecialMobData<?> data = ((ISpecialMob<?>) entity).getSpecialData();
        return entity.isCharging() && data.getTextureOverlay() != null ? data.getTextureOverlay() : data.getTexture();
    }
    
    @Override
    protected void scale(Ghast entity, PoseStack poseStack, float partialTick ) {
        super.scale( entity, poseStack, partialTick );
        
        final float scale = ((ISpecialMob<?>) entity).getSpecialData().getRenderScale();
        shadowRadius = baseShadowRadius * scale;
        poseStack.scale( scale, scale, scale );
    }
}