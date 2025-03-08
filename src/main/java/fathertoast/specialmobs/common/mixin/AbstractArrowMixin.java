package fathertoast.specialmobs.common.mixin;

import fathertoast.specialmobs.common.util.mixin_hooks.CommonMixinHooks;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Projectile {

    protected AbstractArrowMixin(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }


    @ModifyVariable(
            method = "onHitEntity",
            at = @At(
                    value = "LOAD",
                    ordinal = 0
            ),
            name = "flag",
            index = 7,
            ordinal = 0
    )
    public boolean modify_onHitEntity(boolean originalValue, EntityHitResult hitResult) {
        return CommonMixinHooks.modifyAbstractArrowOnHitEntity(hitResult, originalValue);
    }
}
