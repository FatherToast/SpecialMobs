package fathertoast.specialmobs.client.renderer.entity.family;

import com.mojang.blaze3d.matrix.MatrixStack;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.entity.SpecialMobData;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.GhastRenderer;
import net.minecraft.entity.monster.GhastEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn( Dist.CLIENT )
public class SpecialGhastRenderer extends GhastRenderer {
    
    private final float baseShadowRadius;
    
    public SpecialGhastRenderer( EntityRendererManager rendererManager ) {
        super( rendererManager );
        baseShadowRadius = shadowRadius;
        // Would require some big changes to make glowing eyes animate with the base texture
        //addLayer( new SpecialMobEyesLayer<>( this ) );
        // Model doesn't support size parameter - overlay texture is applied to the animation instead
        //addLayer( new SpecialMobOverlayLayer<>( this, new GhastModel<>( 0.25F ) ) );
    }
    
    @Override
    public ResourceLocation getTextureLocation( GhastEntity entity ) {
        final SpecialMobData<?> data = ((ISpecialMob<?>) entity).getSpecialData();
        return entity.isCharging() && data.getTextureOverlay() != null ? data.getTextureOverlay() : data.getTexture();
    }
    
    @Override
    protected void scale( GhastEntity entity, MatrixStack matrixStack, float partialTick ) {
        super.scale( entity, matrixStack, partialTick );
        
        final float scale = ((ISpecialMob<?>) entity).getSpecialData().getRenderScale();
        shadowRadius = baseShadowRadius * scale;
        matrixStack.scale( scale, scale, scale );
    }
}