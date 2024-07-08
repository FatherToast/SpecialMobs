package fathertoast.specialmobs.client.renderer.entity.family;

import com.mojang.blaze3d.vertex.PoseStack;
import fathertoast.specialmobs.client.renderer.entity.layers.SMModelLayers;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobEyesLayer;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobOverlayLayer;
import fathertoast.specialmobs.client.renderer.entity.model.SpecialPiglinModel;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;

public class SpecialPiglinRenderer extends HumanoidMobRenderer<Mob, SpecialPiglinModel<Mob>> {
    
    /** The default renderer for non-zombified piglins. */
    public static SpecialPiglinRenderer newBothEars( EntityRendererProvider.Context context ) {
        return new SpecialPiglinRenderer( context, false, false );
    }
    
    /** The default renderer for zombified piglins. */
    public static SpecialPiglinRenderer newMissingRightEar( EntityRendererProvider.Context context ) {
        return new SpecialPiglinRenderer( context, true, true );
    }
    
    private final float baseShadowRadius;
    
    public SpecialPiglinRenderer( EntityRendererProvider.Context context, boolean missingRightEar, boolean isZombie ) {
        super( context, createModel( context, isZombie ? SMModelLayers.ZOMBIFIED_PIGLIN : SMModelLayers.PIGLIN, missingRightEar ), 0.5F, 1.0019531F, 1.0F, 1.0019531F );
        baseShadowRadius = shadowRadius;
        addLayer(
                new HumanoidArmorLayer<>(this,
                new HumanoidArmorModel<>( context.bakeLayer( isZombie ? SMModelLayers.ZOMBIFIED_PIGLIN_INNER_ARMOR : SMModelLayers.PIGLIN_INNER_ARMOR ) ),
                new HumanoidArmorModel<>( context.bakeLayer( isZombie ? SMModelLayers.ZOMBIFIED_PIGLIN_OUTER_ARMOR : SMModelLayers.PIGLIN_OUTER_ARMOR ) ),
                context.getModelManager() )
        );
        addLayer( new SpecialMobEyesLayer<>( this ) );

        final SpecialPiglinModel<Mob> overlayModel = new SpecialPiglinModel<>( context.bakeLayer( isZombie ? SMModelLayers.ZOMBIFIED_PIGLIN_OUTER_LAYER: SMModelLayers.PIGLIN_OUTER_LAYER ) );
        if( missingRightEar )
            overlayModel.rightEar.visible = false; // This is "stage left" - actually on the piglin's right side
        addLayer( new SpecialMobOverlayLayer<>( this, overlayModel ) );
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

    private static SpecialPiglinModel<Mob> createModel( EntityRendererProvider.Context context, ModelLayerLocation layerLocation, boolean missingRightEar ) {
        SpecialPiglinModel<Mob> model = new SpecialPiglinModel<>( context.bakeLayer( layerLocation ) );

        if ( missingRightEar ) {
            model.rightEar.visible = false;
        }
        return model;
    }

    protected boolean isShaking( Mob mob ) {
        return super.isShaking( mob ) || mob instanceof AbstractPiglin && ( (AbstractPiglin) mob ).isConverting();
    }
}