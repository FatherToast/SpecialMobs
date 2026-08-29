package fathertoast.specialmobs.common.util.mixin_hooks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fathertoast.specialmobs.client.ReadMeConfig;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class ClientMixinHooks {
    
    /**
     * Called from {@link fathertoast.specialmobs.common.mixin.LivingEntityRendererMixin#onRender(LivingEntity, float, float, PoseStack, MultiBufferSource, int, CallbackInfo)}.
     * <br><br>
     * Makes all living entities slowly rotate when secret mode is enabled. Very funny stuff.
     */
    public static <T extends LivingEntity> void handleLivingEntityRender( T entity, float rotation, float partialTick,
                                                                          PoseStack poseStack, MultiBufferSource bufferSource,
                                                                          int packedLight, CallbackInfo cir ) {
        if( !ReadMeConfig.INSTANCE.secretMode.get() ) return;
        
        poseStack.mulPose( Axis.YP.rotationDegrees( (entity.tickCount + partialTick) * 2.5F ) );
    }
}