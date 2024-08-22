package fathertoast.specialmobs.common.mixin;

import fathertoast.specialmobs.common.util.mixin_hooks.CommonMixinHooks;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Blaze.class)
public abstract class BlazeEntityMixin extends Monster {

    protected BlazeEntityMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Redirect(method = "aiStep",
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V", ordinal = 0), require = -1)
    public void onAiStep(Level instance, ParticleOptions particleOptions, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        CommonMixinHooks.handleBlazeSmoke((Blaze) (Object) this);
    }
}
