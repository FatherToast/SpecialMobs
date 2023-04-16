package fathertoast.specialmobs.common.event;

import fathertoast.specialmobs.common.core.register.SMEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GameEvents {

    @SubscribeEvent( priority = EventPriority.NORMAL )
    public void onLivingHurt( LivingHurtEvent event ) {
        final LivingEntity entity = event.getEntity();

        if( entity != null && event.getSource() != DamageSource.OUT_OF_WORLD && !event.getSource().isBypassMagic() &&
                entity.hasEffect( SMEffects.VULNERABILITY.get() ) ) {

            final MobEffectInstance vulnerability = entity.getEffect( SMEffects.VULNERABILITY.get() );
            if( vulnerability == null ) return;

            // Take 25% more damage per effect level (vs. Damage Resistance's 20% less per level)
            event.setAmount( Math.max( event.getAmount() * (1.0F + 0.25F * (vulnerability.getAmplifier() + 1)), 0.0F ) );
        }
    }

    @SubscribeEvent( priority = EventPriority.NORMAL )
    public void onLivingFall( LivingFallEvent event ) {
        final LivingEntity entity = event.getEntity();

        if( entity != null && entity.hasEffect( SMEffects.WEIGHT.get() ) ) {

            final MobEffectInstance weight = entity.getEffect( SMEffects.WEIGHT.get() );
            if( weight == null ) return;

            // Increase effective fall distance by ~33% per effect level
            event.setDamageMultiplier( event.getDamageMultiplier() * (1.0F + 0.3334F * (weight.getAmplifier() + 1)) );
        }
    }
}