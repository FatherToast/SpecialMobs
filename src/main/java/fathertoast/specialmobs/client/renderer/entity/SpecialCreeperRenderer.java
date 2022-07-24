package fathertoast.specialmobs.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialCreeperChargeLayer;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobEyesLayer;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobOverlayLayer;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.layers.CreeperChargeLayer;
import net.minecraft.client.renderer.entity.model.CreeperModel;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn( Dist.CLIENT )
public class SpecialCreeperRenderer extends CreeperRenderer {
    
    private final float baseShadowRadius;
    
    public SpecialCreeperRenderer( EntityRendererManager rendererManager ) {
        super( rendererManager );
        baseShadowRadius = shadowRadius;
        // Get rid of this one since we have our own implementation
        layers.removeIf( ( layer ) -> layer instanceof CreeperChargeLayer );
        
        addLayer( new SpecialMobEyesLayer<>( this ) );
        addLayer( new SpecialMobOverlayLayer<>( this, new CreeperModel<>( 0.25F ) ) );
        addLayer( new SpecialCreeperChargeLayer<>( this, new CreeperModel<>( 2.0F ) ) );
    }
    
    @Override
    public ResourceLocation getTextureLocation( CreeperEntity entity ) {
        return ((ISpecialMob<?>) entity).getSpecialData().getTexture();
    }
    
    @Override
    protected void scale( CreeperEntity entity, MatrixStack matrixStack, float partialTick ) {
        super.scale( entity, matrixStack, partialTick );
        
        final float scale = ((ISpecialMob<?>) entity).getSpecialData().getRenderScale();
        shadowRadius = baseShadowRadius * scale;
        matrixStack.scale( scale, scale, scale );
    }
}