package fathertoast.specialmobs.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobEyesLayer;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobOverlayLayer;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.entity.ai.INinja;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.model.SkeletonModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.data.EmptyModelData;

public class NinjaSkeletonRenderer extends SkeletonRenderer {

    private final float baseShadowRadius;
    private BlockRendererDispatcher blockRenderer;

    public NinjaSkeletonRenderer(EntityRendererManager rendererManager ) {
        super( rendererManager );
        baseShadowRadius = shadowRadius = shadowStrength = 0.0F;
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
        addLayer( new SpecialMobEyesLayer<>( this ) );
        addLayer( new SpecialMobOverlayLayer<>( this, new SkeletonModel<>( ) ) );
    }

    @Override
    public void render(AbstractSkeletonEntity skeletonEntity, float f1, float f2, MatrixStack matrixStack, IRenderTypeBuffer buffer, int i) {
        BlockState disguiseState = ((INinja)skeletonEntity).getDisguiseBlock();

        if (disguiseState == null) {
            super.render(skeletonEntity, f1, f2, matrixStack, buffer, i);
        }
        else {
            renderBlockDisguise((AbstractSkeletonEntity & INinja) skeletonEntity, disguiseState, f1, f2, matrixStack, buffer, i);
        }
    }

    private <T extends AbstractSkeletonEntity & INinja> void renderBlockDisguise(T ninja, BlockState state, float f1, float f2, MatrixStack matrixStack, IRenderTypeBuffer buffer, int i) {
        matrixStack.pushPose();

        matrixStack.translate(-0.5D, 0.0D, -0.5D);
        this.blockRenderer.renderBlock(Blocks.LECTERN.defaultBlockState(), matrixStack, buffer, i, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);

        matrixStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractSkeletonEntity entity ) {
        return ((ISpecialMob<?>) entity).getSpecialData().getTexture();
    }

    @Override
    protected void scale(AbstractSkeletonEntity entity, MatrixStack matrixStack, float partialTick ) {
        super.scale( entity, matrixStack, partialTick );

        final float scale = ((ISpecialMob<?>) entity).getSpecialData().getRenderScale();
        shadowRadius = baseShadowRadius * scale;
        matrixStack.scale( scale, scale, scale );
    }
}
