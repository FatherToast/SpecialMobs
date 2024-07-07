package fathertoast.specialmobs.client.renderer.entity.family;

import com.mojang.blaze3d.vertex.PoseStack;
import fathertoast.specialmobs.client.renderer.entity.layers.SMModelLayers;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialCreeperChargeLayer;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobEyesLayer;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobOverlayLayer;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.CreeperPowerLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn( Dist.CLIENT )
public class SpecialCreeperRenderer extends CreeperRenderer {
    
    private final float baseShadowRadius;
    
    public SpecialCreeperRenderer( EntityRendererProvider.Context context ) {
        super( context );
        // Get rid of this one since we have our own implementation
        layers.removeIf( ( layer ) -> layer instanceof CreeperPowerLayer );

        baseShadowRadius = shadowRadius;
        addLayer( new SpecialMobEyesLayer<>( this ) );
        addLayer( new SpecialMobOverlayLayer<>( this, new CreeperModel<>( context.bakeLayer( SMModelLayers.CREEPER_OUTER_LAYER ) ) ) );
        addLayer( new SpecialCreeperChargeLayer( this, context.getModelSet() ) );
    }
    
    @Override
    public ResourceLocation getTextureLocation( Creeper entity ) {
        return ((ISpecialMob<?>) entity).getSpecialData().getTexture();
    }
    
    @Override
    protected void scale( Creeper entity, PoseStack poseStack, float partialTick ) {
        super.scale( entity, poseStack, partialTick );
        
        final float scale = ((ISpecialMob<?>) entity).getSpecialData().getRenderScale();
        shadowRadius = baseShadowRadius * scale;
        poseStack.scale( scale, scale, scale );
    }
}