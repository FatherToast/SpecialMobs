package fathertoast.specialmobs.client.renderer.entity.family;

import com.mojang.blaze3d.matrix.MatrixStack;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobEyesLayer;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn( Dist.CLIENT )
public class SpecialSlimeRenderer extends SlimeRenderer {
    
    private final float baseShadowRadius;
    
    public SpecialSlimeRenderer( EntityRendererManager rendererManager ) {
        super( rendererManager );
        baseShadowRadius = shadowRadius;
        addLayer( new SpecialMobEyesLayer<>( this ) );
        // Model doesn't support size parameter
        //addLayer( new SpecialMobOverlayLayer<>( this, new SlimeModel<>( 0.25F ) ) );
    }
    
    @Override
    public ResourceLocation getTextureLocation( SlimeEntity entity ) {
        return ((ISpecialMob<?>) entity).getSpecialData().getTexture();
    }
    
    @Override
    protected void scale( SlimeEntity entity, MatrixStack matrixStack, float partialTick ) {
        super.scale( entity, matrixStack, partialTick );
        
        final float scale = ((ISpecialMob<?>) entity).getSpecialData().getRenderScale();
        shadowRadius = baseShadowRadius * scale * entity.getSize(); // Factor slime size into shadow
        matrixStack.scale( scale, scale, scale );
    }
}