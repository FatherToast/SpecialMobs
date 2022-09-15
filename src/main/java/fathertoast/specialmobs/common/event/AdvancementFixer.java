package fathertoast.specialmobs.common.event;

import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.entity.ghast._SpecialGhastEntity;
import fathertoast.specialmobs.common.entity.skeleton._SpecialSkeletonEntity;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Helper class for granting players various vanilla advancements that are
 * impossible to obtain normally because of SM replacing vanilla mobs.
 */
public class AdvancementFixer {

    private static final ResourceLocation KILL_A_MOB_ADV = new ResourceLocation( "adventure/kill_a_mob" );
    private static final ResourceLocation KILL_ALL_MOBS_ADV = new ResourceLocation( "adventure/kill_all_mobs" );
    private static final ResourceLocation SNIPER_DUEL_ADV = new ResourceLocation( "adventure/sniper_duel" );
    private static final ResourceLocation RETURN_TO_SENDER_ADV = new ResourceLocation( "nether/return_to_sender" );
    private static final ResourceLocation UNEASY_ALLIANCE_ADV = new ResourceLocation( "nether/uneasy_alliance" );

    private AdvancementManager manager;


    @SubscribeEvent
    public void onServerStarting( FMLServerStartingEvent event ) {
        manager = event.getServer().getAdvancements();
    }

    @SubscribeEvent( priority = EventPriority.LOWEST )
    public void onLivingDeath( LivingDeathEvent event ) {
        LivingEntity livingEntity = event.getEntityLiving();
        DamageSource source = event.getSource();

        if ( !livingEntity.level.isClientSide && source.getEntity() instanceof ServerPlayerEntity ) {
            if ( livingEntity instanceof ISpecialMob<?> ) {
                ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();

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
    private <T extends LivingEntity & ISpecialMob<?>> void maybeGrantKillAMob( T dead, ServerPlayerEntity player, DamageSource damageSource, Advancement advancement ) {
        if ( damageSource.getEntity() instanceof PlayerEntity && !dead.level.isClientSide ) {
            // Second parameter doesn't matter, just has to be the reg name of an EntityType
            // that is one of the criteria for the advancement. If only it was EntityType tag based :(((
            player.getAdvancements().award( advancement, "minecraft:creeper");
        }
    }

    private <T extends LivingEntity & ISpecialMob<?>> void maybeGrantKillAllMobs( T dead, ServerPlayerEntity player, DamageSource damageSource, Advancement advancement ) {
        if ( damageSource.getEntity() instanceof PlayerEntity && !dead.level.isClientSide ) {
            for (EntityType<?> type : dead.getSpecies().family.replaceableTypes) {
                player.getAdvancements().award( advancement, Objects.requireNonNull(type.getRegistryName()).toString() );
            }
        }
    }

    private <T extends LivingEntity & ISpecialMob<?>> void maybeGrantSniperDuel( T dead, ServerPlayerEntity player, DamageSource damageSource, Advancement advancement ) {
        if ( dead instanceof _SpecialSkeletonEntity && damageSource.getDirectEntity() instanceof ProjectileEntity ) {
            float x = (float)(dead.getX() - player.getX());
            float z = (float)(dead.getZ() - player.getZ());
            float dist = MathHelper.sqrt(x * x + z * z);

            if ( dist >= 50.0F ) {
                player.getAdvancements().award( advancement, "killed_skeleton" );
            }
        }
    }

    private <T extends LivingEntity & ISpecialMob<?>> void maybeGrantReturnToSender( T dead, ServerPlayerEntity player, DamageSource damageSource, Advancement advancement ) {
        if ( dead instanceof _SpecialGhastEntity && damageSource.getDirectEntity() instanceof AbstractFireballEntity ){
            player.getAdvancements().award( advancement, "killed_ghast" );
        }
    }

    private static <T extends LivingEntity & ISpecialMob<?>> void maybeGrantUneasyAlliance( T dead, ServerPlayerEntity player, DamageSource damageSource, Advancement advancement ) {
        if ( dead instanceof _SpecialGhastEntity && dead.level.dimension().equals( World.OVERWORLD ) ) {
            player.getAdvancements().award( advancement, "killed_ghast" );
        }
    }

    private boolean notCompleted( ServerPlayerEntity player, @Nullable Advancement advancement ) {
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
