package fathertoast.specialmobs.client.renderer.entity.family;

import com.mojang.blaze3d.vertex.PoseStack;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobEyesLayer;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobOverlayLayer;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PiglinRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public class SpecialPiglinRenderer extends PiglinRenderer {
    
    /** The default renderer for non-zombified piglins. */
    public static SpecialPiglinRenderer newBothEars( EntityRendererProvider.Context context ) {
        return new SpecialPiglinRenderer( context, false );
    }
    
    /** The default renderer for zombified piglins. */
    public static SpecialPiglinRenderer newMissingRightEar( EntityRendererProvider.Context context ) {
        return new SpecialPiglinRenderer( context, true );
    }
    
    private final float baseShadowRadius;
    
    public SpecialPiglinRenderer( EntityRendererProvider.Context context, boolean missingRightEar ) {
        super( context, ModelLayers.PIGLIN, ModelLayers.PIGLIN_INNER_ARMOR, ModelLayers.PIGLIN_OUTER_ARMOR, missingRightEar );
        baseShadowRadius = shadowRadius;
        addLayer( new SpecialMobEyesLayer<>( this ) );
        
        final PiglinModel<Mob> model = new PiglinModel<>( context.getModelSet().bakeLayer( ModelLayers.PIGLIN ) );
        if( missingRightEar )
            model.rightEar.visible = false; // This is "stage left" - actually on the piglin's right side
        addLayer( new SpecialMobOverlayLayer<>( this, model ) );
    }
    
    @Override
    public ResourceLocation getTextureLocation( Mob entity ) {
        return ((ISpecialMob<?>) entity).getSpecialData().getTexture();
    }
    
    @Override
    protected void scale( Mob entity, PoseStack poseStack, float partialTick ) {
        super.scale( entity, poseStack, partialTick );
        
        final float scale = ((ISpecialMob<?>) entity).getSpecialData().getRenderScale();
        shadowRadius = baseShadowRadius * scale;
        poseStack.scale( scale, scale, scale );
    }
}