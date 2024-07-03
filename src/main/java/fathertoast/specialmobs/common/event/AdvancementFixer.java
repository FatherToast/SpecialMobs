package fathertoast.specialmobs.common.event;

import fathertoast.crust.api.event.AdvancementLoadEvent;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.entity.ghast._SpecialGhastEntity;
import fathertoast.specialmobs.common.entity.skeleton._SpecialSkeletonEntity;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Helper class for granting players various vanilla advancements that are
 * impossible to obtain normally because of SM replacing vanilla mobs.
 */
// TODO - Once Crust has been ported, use AdvancementLoadEvent instead of this clunky stuff
public class AdvancementFixer {

    private static final ResourceLocation KILL_A_MOB_ADV = new ResourceLocation( "adventure/kill_a_mob" );
    private static final ResourceLocation KILL_ALL_MOBS_ADV = new ResourceLocation( "adventure/kill_all_mobs" );
    private static final ResourceLocation SNIPER_DUEL_ADV = new ResourceLocation( "adventure/sniper_duel" );
    private static final ResourceLocation RETURN_TO_SENDER_ADV = new ResourceLocation( "nether/return_to_sender" );
    private static final ResourceLocation UNEASY_ALLIANCE_ADV = new ResourceLocation( "nether/uneasy_alliance" );

    private ServerAdvancementManager manager;


    @SubscribeEvent
    public void onServerStarting( ServerStartingEvent event ) {
        manager = event.getServer().getAdvancements();
    }

    @SubscribeEvent
    public void onAdvancementLoad( AdvancementLoadEvent event ) {
        ResourceLocation id = event.getId();

        if ( id.equals( KILL_A_MOB_ADV ) ) {
            for ( MobFamily.Species<?> species : MobFamily.getAllSpecies() ) {
                EntityType<?> entityType = species.entityType.get();
                String entityId = species.entityType.getId().toString();

                event.getBuilder().addCriterion( entityId, KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of( entityType ) ) );
            }
        }
    }

    @SubscribeEvent( priority = EventPriority.LOWEST )
    public void onLivingDeath( LivingDeathEvent event ) {
        LivingEntity livingEntity = event.getEntity();
        DamageSource source = event.getSource();

        if ( !livingEntity.level().isClientSide && source.getEntity() instanceof ServerPlayer player ) {
            if ( livingEntity instanceof ISpecialMob<?> ) {
                Advancement killAMob = getFromId( KILL_A_MOB_ADV );
                if ( notCompleted( player, killAMob )) {
                    maybeGrantKillAMob( (LivingEntity & ISpecialMob<?>) livingEntity, player, source, killAMob );
                }
                Advancement killAllMob = getFromId( KILL_ALL_MOBS_ADV );
                if ( notCompleted( player, killAllMob )) {
                    maybeGrantKillAllMobs( (LivingEntity & ISpecialMob<?>) livingEntity, player, source, killAllMob );
                }
                Advancement sniperDuel = getFromId( SNIPER_DUEL_ADV );
                if ( notCompleted( player, sniperDuel )) {
                    maybeGrantSniperDuel( (LivingEntity & ISpecialMob<?>) livingEntity, player, source, sniperDuel );
                }
                Advancement returnToSender = getFromId( RETURN_TO_SENDER_ADV );
                if ( notCompleted( player, returnToSender )) {
                    maybeGrantReturnToSender( (LivingEntity & ISpecialMob<?>) livingEntity, player, source, returnToSender );
                }
                Advancement uneasyAlliance = getFromId( UNEASY_ALLIANCE_ADV );
                if ( notCompleted( player, uneasyAlliance )) {
                    maybeGrantUneasyAlliance( (LivingEntity & ISpecialMob<?>) livingEntity, player, source, uneasyAlliance );
                }
            }
        }
    }

    /** Checks if a player has killed a Special Mobs mob, and grants the "Monster Hunter" advancement if so. */
    private <T extends LivingEntity & ISpecialMob<?>> void maybeGrantKillAMob( T dead, ServerPlayer player, DamageSource damageSource, Advancement advancement ) {
        if ( damageSource.getEntity() instanceof Player && !dead.level().isClientSide ) {
            // Second parameter doesn't matter, just has to be the reg name of an EntityType
            // that is one of the criteria for the advancement. If only it was EntityType tag based :(((
            player.getAdvancements().award( advancement, "minecraft:creeper");
        }
    }

    private <T extends LivingEntity & ISpecialMob<?>> void maybeGrantKillAllMobs( T dead, ServerPlayer player, DamageSource damageSource, Advancement advancement ) {
        if ( damageSource.getEntity() instanceof Player && !dead.level().isClientSide ) {
            for (EntityType<?> type : dead.getSpecies().family.replaceableTypes) {
                player.getAdvancements().award( advancement, Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(type)).toString() );
            }
        }
    }

    private <T extends LivingEntity & ISpecialMob<?>> void maybeGrantSniperDuel( T dead, ServerPlayer player, DamageSource damageSource, Advancement advancement ) {
        if ( dead instanceof _SpecialSkeletonEntity && damageSource.getDirectEntity() instanceof Projectile ) {
            float x = (float)(dead.getX() - player.getX());
            float z = (float)(dead.getZ() - player.getZ());
            float dist = Mth.sqrt(x * x + z * z);

            if ( dist >= 50.0F ) {
                player.getAdvancements().award( advancement, "killed_skeleton" );
            }
        }
    }

    private <T extends LivingEntity & ISpecialMob<?>> void maybeGrantReturnToSender( T dead, ServerPlayer player, DamageSource damageSource, Advancement advancement ) {
        if ( dead instanceof _SpecialGhastEntity && damageSource.getDirectEntity() instanceof AbstractHurtingProjectile ){
            player.getAdvancements().award( advancement, "killed_ghast" );
        }
    }

    private static <T extends LivingEntity & ISpecialMob<?>> void maybeGrantUneasyAlliance( T dead, ServerPlayer player, DamageSource damageSource, Advancement advancement ) {
        if ( dead instanceof _SpecialGhastEntity && dead.level().dimension().equals( Level.OVERWORLD ) ) {
            player.getAdvancements().award( advancement, "killed_ghast" );
        }
    }

    private boolean notCompleted( ServerPlayer player, @Nullable Advancement advancement ) {
        if ( advancement == null ) {
            return false;
        }
        return !player.getAdvancements().getOrStartProgress( advancement ).isDone();
    }

    @Nullable
    private Advancement getFromId( ResourceLocation advancementId ) {
        return manager.getAdvancement( advancementId );
    }
}
