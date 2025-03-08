package fathertoast.specialmobs.common.mixin;

import fathertoast.specialmobs.common.core.register.SMTags;
import fathertoast.specialmobs.common.util.mixin_hooks.CommonMixinHooks;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin extends AbstractArrow {

    protected ThrownTridentMixin(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;doPostHurtEffects(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/Entity;)V")
            ),
            method = "onHitEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getType()Lnet/minecraft/world/entity/EntityType;"),
            cancellable = true
    )
    public void inject_onHitEntity( EntityHitResult hitResult, CallbackInfo ci ) {
        CommonMixinHooks.handleTridentOnHitEntity( hitResult, ci );
    }
}
