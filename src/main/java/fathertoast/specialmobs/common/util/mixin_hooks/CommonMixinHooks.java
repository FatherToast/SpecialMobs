package fathertoast.specialmobs.common.util.mixin_hooks;

import fathertoast.specialmobs.common.core.register.SMTags;
import fathertoast.specialmobs.common.entity.blaze.CinderBlazeEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Blaze;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class CommonMixinHooks {

    public static void handleBlazeSmoke( Blaze blaze ) {
        ParticleOptions smoke = blaze instanceof CinderBlazeEntity
                ? ParticleTypes.SMOKE
                : ParticleTypes.LARGE_SMOKE;

        blaze.level().addParticle( smoke,
                blaze.getRandomX( 0.5D ),
                blaze.getRandomY(),
                blaze.getRandomZ( 0.5D ),
                0.0D,
                0.0D,
                0.0D
        );
    }

    public static void handleIsZombified( EntityType<?> entityType, CallbackInfoReturnable<Boolean> cir ) {
        if ( entityType.is( SMTags.ZOMBIFIED_PIGLINS ) ) {
            cir.setReturnValue( true );
        }
    }
}
