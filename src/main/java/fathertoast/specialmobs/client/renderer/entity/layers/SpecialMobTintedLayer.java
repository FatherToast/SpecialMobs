package fathertoast.specialmobs.client.renderer.entity.layers;

import com.mojang.blaze3d.matrix.MatrixStack;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn( Dist.CLIENT )
public abstract class SpecialMobTintedLayer<T extends LivingEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> {
    
    public SpecialMobTintedLayer( IEntityRenderer<T, M> renderer ) { super( renderer ); }
    
    /** @return The color to tint this layer. */
    protected abstract int getColor( T entity );
    
    @Override
    public void render( MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, T entity, float limbSwing,
                        float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch ) {
        if( entity.isInvisible() ) return;
        
        final ResourceLocation overlayTexture = ((ISpecialMob<?>) entity).getSpecialData().getTextureOverlay();
        if( overlayTexture == null ) return;
        
        final int color = getColor( entity );
        renderColoredCutoutModel( getParentModel(), overlayTexture, matrixStack, buffer, packedLight, entity,
                References.getRed( color ), References.getGreen( color ), References.getBlue( color ) );
    }
}