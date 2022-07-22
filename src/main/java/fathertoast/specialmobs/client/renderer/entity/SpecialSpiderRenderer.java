package fathertoast.specialmobs.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobEyesLayer;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.layers.SpiderEyesLayer;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn( Dist.CLIENT )
public class SpecialSpiderRenderer extends SpiderRenderer<SpiderEntity> {
    
    private final float baseShadowRadius;
    
    public SpecialSpiderRenderer( EntityRendererManager rendererManager ) {
        super( rendererManager );
        // Get rid of this one since we have our own implementation
        layers.removeIf( ( layer ) -> layer instanceof SpiderEyesLayer );
        
        baseShadowRadius = shadowRadius;
        addLayer( new SpecialMobEyesLayer<>( this ) );
        // Model doesn't support size parameter
        //addLayer( new SpecialMobOverlayLayer<>( this, new SpiderModel<>( 0.25F ) ) );
    }
    
    @Override
    public ResourceLocation getTextureLocation( SpiderEntity entity ) {
        return ((ISpecialMob<?>) entity).getSpecialData().getTexture();
    }
    
    @Override
    protected void scale( SpiderEntity entity, MatrixStack matrixStack, float partialTick ) {
        super.scale( entity, matrixStack, partialTick );
        
        final float scale = ((ISpecialMob<?>) entity).getSpecialData().getRenderScale();
        shadowRadius = baseShadowRadius * scale;
        matrixStack.scale( scale, scale, scale );
    }
}