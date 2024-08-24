package fathertoast.specialmobs.client.renderer.entity.species;

import com.mojang.blaze3d.vertex.PoseStack;
import fathertoast.specialmobs.client.renderer.entity.layers.SMModelLayers;
import fathertoast.specialmobs.client.renderer.entity.model.SlabGhastModel;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.entity.SpecialMobData;
import fathertoast.specialmobs.common.entity.ghast.SlabGhastEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SlabGhastRenderer extends MobRenderer<SlabGhastEntity, SlabGhastModel<SlabGhastEntity>> {

    private final float baseShadowRadius;

    public SlabGhastRenderer( EntityRendererProvider.Context context ) {
        super( context, new SlabGhastModel<>( context.bakeLayer( SMModelLayers.SLAB_GHAST ) ), 1.5F );
        baseShadowRadius = shadowRadius;
    }

    @Override
    public ResourceLocation getTextureLocation( SlabGhastEntity entity ) {
        final SpecialMobData<?> data = ((ISpecialMob<?>) entity).getSpecialData();
        return entity.isCharging() && data.getTextureAnimation() != null ? data.getTextureAnimation() : data.getTexture();
    }

    @Override
    protected void scale( SlabGhastEntity entity, PoseStack poseStack, float partialTick ) {
        // The base scale of 4.5 is taken from GhastRenderer
        final float scale = 4.5F * ((ISpecialMob<?>) entity).getSpecialData().getRenderScale();
        shadowRadius = baseShadowRadius * scale;
        poseStack.scale( scale, scale, scale );
    }
}
