package fathertoast.specialmobs.common.event;

import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.core.register.SMEffects;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

public class GameEvents {

    private static final ResourceLocation MONSTER_HUNTER_ADV = new ResourceLocation("adventure/kill_a_mob");
    private boolean warnAdvancement = true;


    @SubscribeEvent( priority = EventPriority.NORMAL )
    public void onLivingHurt( LivingHurtEvent event ) {
        if( event.getEntityLiving() != null && event.getSource() != DamageSource.OUT_OF_WORLD && !event.getSource().isBypassMagic() &&
                event.getEntityLiving().hasEffect( SMEffects.VULNERABILITY.get() ) ) {

            final EffectInstance vulnerability = event.getEntityLiving().getEffect( SMEffects.VULNERABILITY.get() );
            if( vulnerability == null ) return;

            // Take 25% more damage per effect level (vs. Damage Resistance's 20% less per level)
            event.setAmount( Math.max( event.getAmount() * (1.0F + 0.25F * (vulnerability.getAmplifier() + 1)), 0.0F ) );
        }
    }

    @SubscribeEvent( priority = EventPriority.NORMAL )
    public void onLivingFall( LivingFallEvent event ) {
        if( event.getEntityLiving() != null && event.getEntityLiving().hasEffect( SMEffects.WEIGHT.get() ) ) {

            final EffectInstance weight = event.getEntityLiving().getEffect( SMEffects.WEIGHT.get() );
            if( weight == null ) return;

            // Increase effective fall distance by ~33% per effect level
            event.setDamageMultiplier( event.getDamageMultiplier() * (1.0F + 0.3334F * (weight.getAmplifier() + 1)) );
        }
    }

    @SubscribeEvent( priority = EventPriority.LOWEST )
    public void onLivingDeath( LivingDeathEvent event ) {
        grantMonsterHunter( event.getEntityLiving(), event.getSource() );
    }

    /** Checks if a player has killed a Special Mobs mob, and grants the "Monster Hunter" advancement if so. */
    private void grantMonsterHunter( LivingEntity dead, DamageSource damageSource ) {
        if ( dead instanceof ISpecialMob<?> ) {
            if ( damageSource.getEntity() instanceof PlayerEntity && !dead.level.isClientSide ) {
                ServerPlayerEntity player = (ServerPlayerEntity) damageSource.getEntity();
                MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
                AdvancementManager manager = server.getAdvancements();
                Advancement advancement = manager.getAdvancement(MONSTER_HUNTER_ADV);

                if (advancement == null) {
                    if (warnAdvancement) {
                        SpecialMobs.LOG.error("Could not fetch the Monster Hunter advancement from the AdvancementManger!");
                        warnAdvancement = false;
                    }
                    return;
                }
                // Second parameter doesn't matter, just has to be the reg name of an EntityType
                // that is one of the criteria for the advancement. If only it was EntityType tag based :(((
                player.getAdvancements().award(advancement, "minecraft:creeper");
            }
        }
    }
}