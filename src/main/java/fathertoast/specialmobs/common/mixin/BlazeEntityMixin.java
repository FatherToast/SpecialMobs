package fathertoast.specialmobs.common.mixin;

import fathertoast.specialmobs.common.util.mixin_hooks.CommonMixinHooks;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Blaze.class)
public abstract class BlazeEntityMixin extends Monster {

    protected BlazeEntityMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }


    @ModifyArg(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"), require = -1)
    public ParticleOptions modifyParticleOptions(ParticleOptions particleOptions) {
        return CommonMixinHooks.getBlazeSmoke((Blaze) (Object) this);
    }
}
