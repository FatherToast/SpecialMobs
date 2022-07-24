package fathertoast.specialmobs.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MagmaCubeRenderer;
import net.minecraft.entity.monster.MagmaCubeEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn( Dist.CLIENT )
public class SpecialMagmaCubeRenderer extends MagmaCubeRenderer {
    
    private final float baseShadowRadius;
    
    public SpecialMagmaCubeRenderer( EntityRendererManager rendererManager ) {
        super( rendererManager );
        baseShadowRadius = shadowRadius;
        // Unneeded, the whole model is full brightness
        //addLayer( new SpecialMobEyesLayer<>( this ) );
        // Model doesn't support size parameter
        //addLayer( new SpecialMobOverlayLayer<>( this, new MagmaCubeModel<>( 0.25F ) ) );
    }
    
    @Override
    public ResourceLocation getTextureLocation( MagmaCubeEntity entity ) {
        return ((ISpecialMob<?>) entity).getSpecialData().getTexture();
    }
    
    @Override
    protected void scale( MagmaCubeEntity entity, MatrixStack matrixStack, float partialTick ) {
        super.scale( entity, matrixStack, partialTick );
        
        final float scale = ((ISpecialMob<?>) entity).getSpecialData().getRenderScale();
        shadowRadius = baseShadowRadius * scale * entity.getSize(); // Factor slime size into shadow
        matrixStack.scale( scale, scale, scale );
    }
}