package fathertoast.specialmobs.common.util.mixin_hooks;

import fathertoast.specialmobs.common.core.register.SMTags;
import fathertoast.specialmobs.common.entity.blaze.CinderBlazeEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.system.CallbackI;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class CommonMixinHooks {

    public static ParticleOptions getBlazeSmoke( Blaze blaze ) {
        return blaze instanceof CinderBlazeEntity
                ? ParticleTypes.SMOKE
                : ParticleTypes.LARGE_SMOKE;
    }

    public static void handleIsZombified( EntityType<?> entityType, CallbackInfoReturnable<Boolean> cir ) {
        if ( entityType.is( SMTags.EntityTypes.ZOMBIFIED_PIGLINS ) ) {
            cir.setReturnValue( true );
        }
    }

    public static void handleTridentOnHitEntity( EntityHitResult hitResult, CallbackInfo ci ) {
        if ( hitResult.getEntity().getType().is( SMTags.EntityTypes.ENDERMEN ) )
            ci.cancel();
    }

    public static boolean modifyAbstractArrowOnHitEntity( EntityHitResult hitResult, boolean originalValue ) {
        return originalValue || hitResult.getEntity().getType().is( SMTags.EntityTypes.ENDERMEN );
    }
}
