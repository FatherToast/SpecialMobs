package fathertoast.specialmobs.client.renderer.entity.family;

import com.mojang.blaze3d.matrix.MatrixStack;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobEyesLayer;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobOverlayLayer;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.layers.EndermanEyesLayer;
import net.minecraft.client.renderer.entity.model.EndermanModel;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn( Dist.CLIENT )
public class SpecialEndermanRenderer extends EndermanRenderer {
    
    private final float baseShadowRadius;
    
    public SpecialEndermanRenderer( EntityRendererManager rendererManager ) {
        super( rendererManager );
        // Get rid of this one since we have our own implementation
        layers.removeIf( ( layer ) -> layer instanceof EndermanEyesLayer );
        
        baseShadowRadius = shadowRadius;
        addLayer( new SpecialMobEyesLayer<>( this ) );
        addLayer( new SpecialMobOverlayLayer<>( this, new EndermanModel<>( 0.25F ) ) );
    }
    
    @Override
    public ResourceLocation getTextureLocation( EndermanEntity entity ) {
        return ((ISpecialMob<?>) entity).getSpecialData().getTexture();
    }
    
    @Override
    protected void scale( EndermanEntity entity, MatrixStack matrixStack, float partialTick ) {
        super.scale( entity, matrixStack, partialTick );
        
        final float scale = ((ISpecialMob<?>) entity).getSpecialData().getRenderScale();
        shadowRadius = baseShadowRadius * scale;
        matrixStack.scale( scale, scale, scale );
    }
}