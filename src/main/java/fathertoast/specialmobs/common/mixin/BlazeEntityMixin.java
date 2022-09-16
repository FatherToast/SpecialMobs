package fathertoast.specialmobs.common.mixin;

import fathertoast.specialmobs.common.util.mixin_hooks.CommonMixinHooks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.BlazeEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(BlazeEntity.class)
public abstract class BlazeEntityMixin extends MonsterEntity {

    protected BlazeEntityMixin(EntityType<? extends MonsterEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "aiStep",
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playLocalSound(DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FFZ)V")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particles/IParticleData;DDDDDD)V", ordinal = 0))
    public void onAiStep(World instance, IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        CommonMixinHooks.handleBlazeSmoke((BlazeEntity) (Object) this);
    }

}
