package fathertoast.specialmobs.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public abstract class SpecialMobTintedLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    
    public SpecialMobTintedLayer( RenderLayerParent<T, M> renderer ) { super( renderer ); }
    
    /** @return The color to tint this layer. */
    protected abstract int getColor( T entity );
    
    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing,
                       float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch ) {
        if( entity.isInvisible() ) return;
        
        final ResourceLocation overlayTexture = ((ISpecialMob<?>) entity).getSpecialData().getTextureOverlay();
        if( overlayTexture == null ) return;
        
        final int color = getColor( entity );
        renderColoredCutoutModel( getParentModel(), overlayTexture, poseStack, buffer, packedLight, entity,
                References.getRed( color ), References.getGreen( color ), References.getBlue( color ) );
    }
}