package fathertoast.specialmobs.common.event;

import fathertoast.crust.api.event.advancement.AdvancementLoadEvent;
import fathertoast.crust.api.event.advancement.IModifiableAdvancement;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.core.register.SMTags;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
public class AdvancementFixer {
    
    private static final ResourceLocation KILL_A_MOB_ADV = ResourceLocation.withDefaultNamespace( "adventure/kill_a_mob" );
    private static final ResourceLocation KILL_ALL_MOBS_ADV = ResourceLocation.withDefaultNamespace( "adventure/kill_all_mobs" );
    private static final ResourceLocation SNIPER_DUEL_ADV = ResourceLocation.withDefaultNamespace( "adventure/sniper_duel" );
    private static final ResourceLocation RETURN_TO_SENDER_ADV = ResourceLocation.withDefaultNamespace( "nether/return_to_sender" );
    private static final ResourceLocation UNEASY_ALLIANCE_ADV = ResourceLocation.withDefaultNamespace( "nether/uneasy_alliance" );
    
    /** The current server's {@link ServerAdvancementManager} instance. */
    private ServerAdvancementManager manager;
    
    
    @SubscribeEvent
    public void onServerStarting( ServerStartingEvent event ) {
        manager = event.getServer().getAdvancements();
    }
    
    @SubscribeEvent
    public void onAdvancementLoad( AdvancementLoadEvent event ) {
        final ResourceLocation id = event.getId();
        final IModifiableAdvancement advancement = event.getAdvancement();
        
        // Killing any Special Mobs mob grants the "kill a mob" advancement.
        if( id.equals( KILL_A_MOB_ADV ) ) {
            advancement.setRequirementsStrategy( RequirementsStrategy.OR );
            
            for( MobFamily.Species<?> species : MobFamily.getAllSpecies() ) {
                EntityType<?> entityType = species.entityType.get();
                // noinspection ConstantConditions
                String entityId = species.entityType.getId().toString();
                
                advancement.addCriterion( entityId, KilledTrigger.TriggerInstance.playerKilledEntity( EntityPredicate.Builder.entity().of( entityType ) ), false );
                advancement.setRequirementsStrategy( RequirementsStrategy.OR );
            }
        }
        // Make it so Special Mob's replacement skeleton can grant the "sniper duel" advancement.
        else if( id.equals( SNIPER_DUEL_ADV ) ) {
            final CriterionTriggerInstance criterion = KilledTrigger.TriggerInstance.playerKilledEntity(
                    EntityPredicate.Builder.entity()
                            .of( MobFamily.SKELETON.vanillaReplacement.entityType.get() )
                            .distance( DistancePredicate.horizontal( MinMaxBounds.Doubles.atLeast( 50.0D ) ) ),
                    DamageSourcePredicate.Builder.damageType()
                            .tag( TagPredicate.is( DamageTypeTags.IS_PROJECTILE ) ) );
            
            advancement.addCriterion( "killed_special_skeleton", criterion, false );
            advancement.setRequirementsStrategy( RequirementsStrategy.OR );
        }
        // Make it so Special Mob's replacement ghast can grant the "return to sender" advancement.
        else if( id.equals( RETURN_TO_SENDER_ADV ) ) {
            final CriterionTriggerInstance criterion = KilledTrigger.TriggerInstance.playerKilledEntity(
                    EntityPredicate.Builder.entity()
                            .of( MobFamily.GHAST.vanillaReplacement.entityType.get() ),
                    DamageSourcePredicate.Builder.damageType()
                            .tag( TagPredicate.is( DamageTypeTags.IS_PROJECTILE ) )
                            .direct( EntityPredicate.Builder.entity().of( EntityType.FIREBALL ) ) );
            
            advancement.addCriterion( "killed_special_ghast", criterion, false );
            advancement.setRequirementsStrategy( RequirementsStrategy.OR );
        }
        // Make it so any ghast variant in the Forge ghast tag can grant the "uneasy alliance" advancement.
        else if( id.equals( UNEASY_ALLIANCE_ADV ) ) {
            final CriterionTriggerInstance criterion = KilledTrigger.TriggerInstance.playerKilledEntity(
                    EntityPredicate.Builder.entity()
                            .of( SMTags.EntityTypes.GHASTS )
                            .located( LocationPredicate.inDimension( Level.OVERWORLD ) ) );
            
            advancement.addCriterion( "killed_any_ghast", criterion, false );
            advancement.setRequirementsStrategy( RequirementsStrategy.OR );
        }
    }
    
    /**
     * Called when a living entity dies.
     */
    @SubscribeEvent( priority = EventPriority.LOWEST )
    public void onLivingDeath( LivingDeathEvent event ) {
        LivingEntity livingEntity = event.getEntity();
        DamageSource source = event.getSource();
        
        // noinspection resource
        if( !livingEntity.level().isClientSide && source.getEntity() instanceof ServerPlayer player ) {
            if( livingEntity instanceof ISpecialMob<?> ) {
                Advancement killAllMob = getFromId( KILL_ALL_MOBS_ADV );
                if( notCompleted( player, killAllMob ) ) {
                    maybeGrantKillAllMobs( (LivingEntity & ISpecialMob<?>) livingEntity, player, source, killAllMob );
                }
            }
        }
    }
    
    /**
     * "Manual" patch for the "kill all mobs" advancement,
     * as it is a bit awkward to try and modify its criteria.
     */
    private <T extends LivingEntity & ISpecialMob<?>> void maybeGrantKillAllMobs( T dead, ServerPlayer player, DamageSource damageSource, Advancement advancement ) {
        // noinspection resource
        if( damageSource.getEntity() instanceof Player && !dead.level().isClientSide ) {
            for( EntityType<?> type : dead.getSpecies().family.replaceableTypes ) {
                player.getAdvancements().award( advancement, Objects.requireNonNull( ForgeRegistries.ENTITY_TYPES.getKey( type ) ).toString() );
            }
        }
    }
    
    /** @return True if the player has not yet completed the given advancement. */
    private boolean notCompleted( ServerPlayer player, @Nullable Advancement advancement ) {
        if( advancement == null ) return false;
        return !player.getAdvancements().getOrStartProgress( advancement ).isDone();
    }
    
    /**
     * @return The advancement mapped to the given ID.
     * Returns null if the advancement manager does not
     * contain the advancement.
     */
    @Nullable
    @SuppressWarnings( "SameParameterValue" )
    private Advancement getFromId( ResourceLocation advancementId ) {
        return manager.getAdvancement( advancementId );
    }
}