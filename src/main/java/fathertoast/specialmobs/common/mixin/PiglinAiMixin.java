package fathertoast.specialmobs.common.mixin;

import fathertoast.specialmobs.common.util.mixin_hooks.CommonMixinHooks;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinAi.class)
public abstract class PiglinAiMixin {

    /**
     * Tiny mixin to make piglins freak out over special zombified piglin variants
     */
    @Inject(method = "isZombified", at = @At("HEAD"), cancellable = true)
    private static void onIsZombified(EntityType<?> entityType, CallbackInfoReturnable<Boolean> cir) {
        CommonMixinHooks.handleIsZombified(entityType, cir);
    }
}
