package fathertoast.specialmobs.common.util.mixin_hooks;

import fathertoast.specialmobs.common.entity.blaze.CinderBlazeEntity;
import net.minecraft.entity.monster.BlazeEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;

public class CommonMixinHooks {

    public static void handleBlazeSmoke(BlazeEntity blaze) {
        IParticleData smoke = blaze instanceof CinderBlazeEntity ? ParticleTypes.SMOKE : ParticleTypes.LARGE_SMOKE;
        blaze.level.addParticle(smoke, blaze.getRandomX(0.5D), blaze.getRandomY(), blaze.getRandomZ(0.5D), 0.0D, 0.0D, 0.0D);
    }
}
