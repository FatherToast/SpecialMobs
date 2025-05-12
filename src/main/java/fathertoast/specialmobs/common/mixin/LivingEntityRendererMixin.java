package fathertoast.specialmobs.common.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import fathertoast.specialmobs.common.util.mixin_hooks.ClientMixinHooks;
import fathertoast.specialmobs.common.util.mixin_hooks.CommonMixinHooks;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {

    protected LivingEntityRendererMixin( EntityRendererProvider.Context context ) {
        super(context);
    }


    @Inject(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;setupRotations(Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V",
                    ordinal = 0
            )
    )
    public void onRender( T entity, float rotation, float partialTick, PoseStack poseStack,
                          MultiBufferSource bufferSource, int packedLight, CallbackInfo ci ) {
        ClientMixinHooks.handleLivingEntityRender( entity, rotation, partialTick, poseStack, bufferSource, packedLight, ci );
    }
}
