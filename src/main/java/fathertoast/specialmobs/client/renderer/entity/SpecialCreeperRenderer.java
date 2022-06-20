package fathertoast.specialmobs.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.CreeperModel;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@OnlyIn( Dist.CLIENT )
public class SpecialCreeperRenderer extends CreeperRenderer {
    
    private final float baseShadowRadius;
    
    public SpecialCreeperRenderer( EntityRendererManager rendererManager ) {
        super( rendererManager );
        baseShadowRadius = shadowRadius;
        //        addLayer( new LayerSpecialMobEyes<>( this ) ); TODO render layer impl
        //        addLayer( new LayerSpecialMobOverlay<>( this, new CreeperModel<>( 0.25F ) ) );
    }
    
    @Override
    public ResourceLocation getTextureLocation( CreeperEntity entity ) {
        return super.getTextureLocation( entity );
        //return ((ISpecialMob<?>) entity).getSpecialData().getTexture();TODO textures
    }
    
    @Override
    protected void scale( CreeperEntity entity, MatrixStack matrixStack, float partialTick ) {
        super.scale( entity, matrixStack, partialTick );
        
        final float scale = ((ISpecialMob<?>) entity).getSpecialData().getRenderScale();
        shadowRadius = baseShadowRadius * scale;
        matrixStack.scale( scale, scale, scale );
    }
}