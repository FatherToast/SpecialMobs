package fathertoast.specialmobs.common.core;

import fathertoast.crust.api.lib.EnvironmentHelper;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.spider.MotherSpiderEntity;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;

@Mod.EventBusSubscriber( modid = SpecialMobs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE )
public final class SpecialMobReplacer {
    /** List of data for mobs needing replacement. */
    private static final Deque<MobReplacementEntry> TO_REPLACE = new ArrayDeque<>();
    /** List of data for mobs waiting to check for mob replacement. */
    private static final List<DelayedMobReplacementEntry> DELAYED_REPLACE = new ArrayList<>();
    
    /** Returns true if the species is not damaged by water. */
    private static final Predicate<MobFamily.Species<?>> WATER_INSENSITIVE_SELECTOR =
            ( species ) -> !species.config.GENERAL.isDamagedByWater.get();
    /** Returns true if the species' block height is less than or equal to the base vanilla entity's. */
    private static final Predicate<MobFamily.Species<?>> NO_GIANTS_SELECTOR = MobFamily.Species::isNotGiant;


    /**
     * Called when a mob is being finalized before being added to the world and passed
     * to {@link EntityJoinLevelEvent}.
     * <p>
     * Here we check for spawns that are potentially unsafe to process later, such as chunk
     * gen spawns or structure spawns, and mark them as already initialised by SM if so to skip them.
     *
     * @param event The event data.
     */
    @SubscribeEvent
    public static void onMobSpawn( MobSpawnEvent.FinalizeSpawn event ) {
        final MobSpawnType spawnType = event.getSpawnType();

        if ( spawnType == MobSpawnType.CHUNK_GENERATION || spawnType == MobSpawnType.STRUCTURE ) {
            setInitFlag( event.getEntity() );
            setInitFlag( event.getEntity() );
        }
    }

    /**
     * Called when any entity is spawned into the world by any means (such as natural/spawner spawns or chunk loading).
     * <p>
     * This checks whether the entity belongs to a special mob family and appropriately marks the mob to be replaced
     * by the tick handler after deciding whether the mob should be spawned as a special variant species.
     *
     * @param event The event data.
     */
    @SubscribeEvent( priority = EventPriority.LOWEST )
    public static void onEntityJoinLevel( EntityJoinLevelEvent event ) {
        if( event.getLevel().isClientSide() || event.loadedFromDisk() || !Config.MAIN.GENERAL.enableMobReplacement.get() )
            return;
        
        final Entity entity = event.getEntity();
        final MobFamily<?, ?> mobFamily = getReplacingMobFamily( entity );

        if( mobFamily != null ) {
            final Level level = event.getLevel();
            final BlockPos entityPos = BlockPos.containing( entity.position() );

            setInitFlag( entity ); // Do this regardless of replacement, should help prevent bizarre save glitches

            if( EnvironmentHelper.isLoaded( level, entityPos ) ) {
                final boolean isSpecial = shouldMakeNextSpecial( mobFamily, level, entityPos );
                if( shouldReplace( mobFamily, isSpecial ) ) {
                    TO_REPLACE.addLast( new MobReplacementEntry( mobFamily, isSpecial, entity, level, entityPos ) );
                    
                    // Sadly, it's somewhat of a pain to make sure no warnings get logged
                    // when dealing with mounts/riders... Maybe someday :(
                    event.setCanceled( true );
                }
            }
            else {
                DELAYED_REPLACE.add( new DelayedMobReplacementEntry( mobFamily, entity, level, entityPos ) );
            }
        }
    }
    
    /**
     * Called each server tick.
     * <p>
     * Executes all pending mob replacements.
     *
     * @param event The event data.
     */
    @SubscribeEvent( priority = EventPriority.NORMAL )
    public static void onServerTick( TickEvent.ServerTickEvent event ) {
        if( event.phase == TickEvent.Phase.END ) {
            if( !DELAYED_REPLACE.isEmpty() ) {
                DELAYED_REPLACE.removeIf( DelayedMobReplacementEntry::update );
            }
            
            while( !TO_REPLACE.isEmpty() ) {
                final MobReplacementEntry replacement = TO_REPLACE.removeFirst();
                replace( replacement.mobFamily, replacement.isSpecial, replacement.entityToReplace, replacement.entityWorld, replacement.entityPos );
            }
        }
    }
    
    /**
     * @param entity The entity to test.
     * @return True if this mod's init flag has been set for the entity.
     */
    private static boolean getInitFlag( Entity entity ) {
        final CompoundTag forgeData = entity.getPersistentData();
        if( forgeData.contains( References.TAG_INIT, References.NBT_TYPE_NUMERICAL ) ) {
            return forgeData.getBoolean( References.TAG_INIT );
        }
        return false;
    }
    
    /**
     * Sets the init flag for an entity, causing the mod to ignore any future loading of the entity into the world.
     *
     * @param entity The entity to set this mod's init flag for.
     */
    private static void setInitFlag( Entity entity ) {
        final CompoundTag forgeData = entity.getPersistentData();
        forgeData.putBoolean( References.TAG_INIT, true );
    }
    
    /** @return The mob family to replace with, or null if the mob is not replaceable. */
    @Nullable
    private static MobFamily<?, ?> getReplacingMobFamily( @Nullable Entity entity ) {
        if( entity == null || getInitFlag( entity ) ) return null;
        return MobFamily.getReplacementFamily( entity );
    }
    
    /** @return True if the next mob should be made a special variant. */
    private static boolean shouldMakeNextSpecial( MobFamily<?, ?> mobFamily, Level level, BlockPos entityPos ) {
        return level.random.nextDouble() < mobFamily.config.GENERAL.specialVariantChance.get( level, entityPos );
    }
    
    /** @return True if a mob should be replaced. */
    private static boolean shouldReplace( MobFamily<?, ?> mobFamily, boolean isSpecial ) {
        return isSpecial || Config.MAIN.GENERAL.masterVanillaReplacement.get() && mobFamily.config.GENERAL.vanillaReplacement.get();
    }
    
    /** Replaces a mob, copying over all its data to the replacement. */
    private static void replace( MobFamily<?, ?> mobFamily, boolean isSpecial, Entity entityToReplace, Level level, BlockPos entityPos ) {
        // Make sure the chunk the entity is in is loaded
        if( !(level instanceof ServerLevelAccessor) || !EnvironmentHelper.isLoaded(level, entityPos) ) return;
        
        final CompoundTag tag = new CompoundTag();
        entityToReplace.saveWithoutId( tag );
        
        // Don't copy UUID
        tag.remove( "UUID" );
        
        final MobFamily.Species<?> species = isSpecial ?
                mobFamily.nextVariant( level, entityPos, getVariantFilter( mobFamily, entityToReplace, level, entityPos ) ) :
                mobFamily.vanillaReplacement;
        
        final LivingEntity replacement = species.entityType.get().create( level );
        if( replacement == null ) {
            SpecialMobs.LOG.error( "Failed to create replacement entity '{}'", species.entityType.getId() );
            return;
        }
        
        replacement.load( tag );
        replacement.setHealth( (float) replacement.getAttributeValue( Attributes.MAX_HEALTH ) );
        MobHelper.finalizeSpawn( replacement, (ServerLevelAccessor) level, level.getCurrentDifficultyAt( entityPos ), null, null );
        
        level.addFreshEntity( replacement );
        
        for( Entity rider : entityToReplace.getPassengers() ) {
            rider.stopRiding();
            rider.startRiding( replacement, true );
        }
        if( entityToReplace.getVehicle() != null ) {
            final Entity vehicle = entityToReplace.getVehicle();
            entityToReplace.stopRiding();
            replacement.startRiding( vehicle, true );
        }
        
        entityToReplace.discard();
    }
    
    /** @return A selector that filters out variants that are likely to die a stupid death if chosen. */
    @Nullable
    private static Predicate<MobFamily.Species<?>> getVariantFilter( MobFamily<?, ?> mobFamily, Entity entityToReplace,
                                                                     Level level, BlockPos entityPos ) {
        Predicate<MobFamily.Species<?>> selector = null;
        
        // Note that we do not check for any fluids (water/lava) since that is handled by spawn logic
        if( !mobFamily.vanillaReplacement.bestiaryInfo.isDamagedByWater && // Skip this check if the base vanilla mob dies in water
                level.isRainingAt( entityPos ) ) {
            selector = WATER_INSENSITIVE_SELECTOR;
        }
        
        // Does not consider overly wide mobs or extra-tall (>1 block taller) mobs
        if( mobFamily.hasAnyGiants() ) {
            final AABB bb = entityToReplace.getBoundingBox();
            final int y = Mth.ceil( bb.maxY );
            // Only check the FULL block above current collision - not a perfect representation, but keeps things simple
            if( !level.isUnobstructed( entityToReplace, Shapes.create(
                    new AABB( bb.minX, y, bb.minZ, bb.maxX, y + 1, bb.maxZ ) ) ) ) {
                selector = selector == null ? NO_GIANTS_SELECTOR : selector.and( NO_GIANTS_SELECTOR );
            }
        }
        
        return selector;
    }
    
    /** All data needed for a single mob we want to replace. */
    private static class MobReplacementEntry {
        final MobFamily<?, ?> mobFamily;
        final boolean isSpecial;
        
        final LivingEntity entityToReplace;
        final Level entityWorld;
        final BlockPos entityPos;
        
        MobReplacementEntry( MobFamily<?, ?> family, boolean special, Entity entity, Level level, BlockPos pos ) {
            mobFamily = family;
            isSpecial = special;
            
            entityToReplace = (LivingEntity) entity;
            entityWorld = level;
            entityPos = pos;
        }
    }
    
    /** All data needed for a single mob we are waiting to replace. */
    private static class DelayedMobReplacementEntry {
        final MobFamily<?, ?> mobFamily;
        
        final LivingEntity entityToReplace;
        final Level entityLevel;
        final BlockPos entityPos;
        
        int ticksRemaining = 6;
        
        DelayedMobReplacementEntry( MobFamily<?, ?> family, Entity entity, Level level, BlockPos pos ) {
            mobFamily = family;
            
            entityToReplace = (LivingEntity) entity;
            entityLevel = level;
            entityPos = pos;
        }
        
        /** Called each server tick to see if the mob is ready to be replaced. Return true when done. */
        boolean update() {
            if( ticksRemaining > 0 ) {
                ticksRemaining--;
            }
            else if( !entityToReplace.isAlive() ) {
                return true; // Entity was killed or unloaded before getting replaced
            }
            else if( EnvironmentHelper.isLoaded( entityLevel, entityPos ) ) {
                // It is time to decide!
                final boolean isSpecial = shouldMakeNextSpecial( mobFamily, entityLevel, entityPos );
                if( shouldReplace( mobFamily, isSpecial ) ) {
                    TO_REPLACE.addLast( new MobReplacementEntry( mobFamily, isSpecial, entityToReplace, entityLevel, entityPos ) );
                    entityToReplace.discard();
                }
                return true;
            }
            else {
                ticksRemaining = 5;
            }
            return false;
        }
    }
}