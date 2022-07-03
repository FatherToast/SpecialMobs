package fathertoast.specialmobs.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialGhastEyesLayer;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobEyesLayer;
import fathertoast.specialmobs.client.renderer.entity.misc.SMRenderTypes;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.entity.SpecialMobData;
import fathertoast.specialmobs.common.entity.ghast.CorporealShiftGhastEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public class CorporealShiftGhastRenderer extends MobRenderer<CorporealShiftGhastEntity, CorporealShiftGhastModel<CorporealShiftGhastEntity>> {

    private static final Function<ResourceLocation, RenderType> INCORPOREAL = (resourceLocation) -> SMRenderTypes.entityCutoutNoCullBlend(resourceLocation, SMRenderTypes.INCORPOREAL_ALPHA);

    private static final ResourceLocation EYES = SpecialMobs.resourceLoc("textures/entity/ghast/corporeal_shift_eyes.png");
    private static final ResourceLocation SHOOT_EYES = SpecialMobs.resourceLoc("textures/entity/ghast/corporeal_shift_shoot_eyes.png");

    private final float baseShadowRadius;

    public CorporealShiftGhastRenderer(EntityRendererManager rendererManager) {
        super(rendererManager, new CorporealShiftGhastModel<>(), 1.5F);
        addLayer(new SpecialGhastEyesLayer<>(this, EYES, SHOOT_EYES ));
        baseShadowRadius = shadowRadius;
    }

    @Override
    public void render(CorporealShiftGhastEntity ghast, float rotation, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        model.renderType = ghast.isCorporeal() ? RenderType::entityCutoutNoCull : INCORPOREAL;
        super.render(ghast, rotation, partialTicks, matrixStack, buffer, packedLight);
    }


    @Override
    public ResourceLocation getTextureLocation( CorporealShiftGhastEntity entity ) {
        final SpecialMobData<?> data = ((ISpecialMob<?>) entity).getSpecialData();
        return entity.isCharging() && data.hasOverlayTexture() ? data.getTextureOverlay() : data.getTexture();
    }

    @Override
    protected void scale( CorporealShiftGhastEntity entity, MatrixStack matrixStack, float partialTick ) {
        final float scale = 4.5F + ((ISpecialMob<?>) entity).getSpecialData().getRenderScale();
        shadowRadius = baseShadowRadius * scale;
        matrixStack.scale( scale, scale, scale );
    }
}
