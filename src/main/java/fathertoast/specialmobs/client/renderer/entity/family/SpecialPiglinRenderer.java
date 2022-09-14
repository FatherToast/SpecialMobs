package fathertoast.specialmobs.client.renderer.entity.family;

import com.mojang.blaze3d.matrix.MatrixStack;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobEyesLayer;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobOverlayLayer;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.PiglinRenderer;
import net.minecraft.client.renderer.entity.model.PiglinModel;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn( Dist.CLIENT )
public class SpecialPiglinRenderer extends PiglinRenderer {
    
    /** The default renderer for non-zombified piglins. */
    public static SpecialPiglinRenderer newBothEars( EntityRendererManager rendererManager ) {
        return new SpecialPiglinRenderer( rendererManager, false );
    }
    
    /** The default renderer for zombified piglins. */
    public static SpecialPiglinRenderer newMissingRightEar( EntityRendererManager rendererManager ) {
        return new SpecialPiglinRenderer( rendererManager, true );
    }
    
    private final float baseShadowRadius;
    
    public SpecialPiglinRenderer( EntityRendererManager rendererManager, boolean missingRightEar ) {
        super( rendererManager, missingRightEar );
        baseShadowRadius = shadowRadius;
        addLayer( new SpecialMobEyesLayer<>( this ) );
        
        final PiglinModel<MobEntity> model = new PiglinModel<>( 0.25F, 64, 64 );
        if( missingRightEar )
            model.earLeft.visible = false; // This is "stage left" - actually on the piglin's right side
        addLayer( new SpecialMobOverlayLayer<>( this, model ) );
    }
    
    @Override
    public ResourceLocation getTextureLocation( MobEntity entity ) {
        return ((ISpecialMob<?>) entity).getSpecialData().getTexture();
    }
    
    @Override
    protected void scale( MobEntity entity, MatrixStack matrixStack, float partialTick ) {
        super.scale( entity, matrixStack, partialTick );
        
        final float scale = ((ISpecialMob<?>) entity).getSpecialData().getRenderScale();
        shadowRadius = baseShadowRadius * scale;
        matrixStack.scale( scale, scale, scale );
    }
}