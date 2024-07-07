package fathertoast.specialmobs.client.renderer.entity.family;

import com.mojang.blaze3d.vertex.PoseStack;
import fathertoast.specialmobs.client.renderer.entity.layers.SMModelLayers;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobEyesLayer;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobOverlayLayer;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Slime;

public class SpecialSlimeRenderer extends SlimeRenderer {
    
    private final float baseShadowRadius;
    
    public SpecialSlimeRenderer( EntityRendererProvider.Context context ) {
        super( context );
        baseShadowRadius = shadowRadius;
        addLayer( new SpecialMobEyesLayer<>( this ) );
        addLayer( new SpecialMobOverlayLayer<>( this, new SlimeModel<>( context.bakeLayer( SMModelLayers.SLIME_OUTER_LAYER ) ) ) );
    }
    
    @Override
    public ResourceLocation getTextureLocation( Slime entity ) {
        return ((ISpecialMob<?>) entity).getSpecialData().getTexture();
    }
    
    @Override
    protected void scale(Slime entity, PoseStack poseStack, float partialTick ) {
        super.scale( entity, poseStack, partialTick );
        
        final float scale = ((ISpecialMob<?>) entity).getSpecialData().getRenderScale();
        shadowRadius = baseShadowRadius * scale * entity.getSize(); // Factor slime size into shadow
        poseStack.scale( scale, scale, scale );
    }
}