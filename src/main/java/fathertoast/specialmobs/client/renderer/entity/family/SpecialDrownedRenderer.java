package fathertoast.specialmobs.client.renderer.entity.family;

import com.mojang.blaze3d.matrix.MatrixStack;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobEyesLayer;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobOverlayLayer;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import net.minecraft.client.renderer.entity.DrownedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import net.minecraft.client.renderer.entity.model.DrownedModel;
import net.minecraft.entity.monster.DrownedEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn( Dist.CLIENT )
public class SpecialDrownedRenderer extends DrownedRenderer {
    
    private final float baseShadowRadius;
    
    public SpecialDrownedRenderer( EntityRendererManager rendererManager ) {
        super( rendererManager );
        baseShadowRadius = shadowRadius;
        // Get rid of this one since we have our own implementation
        layers.removeIf( ( layer ) -> layer instanceof DrownedOuterLayer );
        
        addLayer( new SpecialMobEyesLayer<>( this ) );
        addLayer( new SpecialMobOverlayLayer<>( this, new DrownedModel<>( 0.25F, 0.0F, 64, 64 ) ) );
    }
    
    @Override
    public ResourceLocation getTextureLocation( DrownedEntity entity ) {
        return ((ISpecialMob<?>) entity).getSpecialData().getTexture();
    }
    
    @Override
    protected void scale( DrownedEntity entity, MatrixStack matrixStack, float partialTick ) {
        super.scale( entity, matrixStack, partialTick );
        
        final float scale = ((ISpecialMob<?>) entity).getSpecialData().getRenderScale();
        shadowRadius = baseShadowRadius * scale;
        matrixStack.scale( scale, scale, scale );
    }
}