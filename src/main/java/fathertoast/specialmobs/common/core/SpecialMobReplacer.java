package fathertoast.specialmobs.common.core;

import fathertoast.crust.api.config.common.value.environment.EnvironmentContext;
import fathertoast.crust.api.lib.EnvironmentHelper;
import fathertoast.crust.api.lib.NBTHelper;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.SpawnType;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.event.ForgeEventFactory;
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
    /**
     * List of data for mobs needing replacement.
     */
    private static final Deque<MobReplacementEntry> TO_REPLACE = new ArrayDeque<>();
    /**
     * List of data for mobs waiting to check for mob replacement.
     */
    private static final List<DelayedMobReplacementEntry> DELAYED_REPLACE = new ArrayList<>();
    
    /**
     * Returns true if the species is not damaged by water.
     */
    private static final Predicate<MobFamily.Species<?>> WATER_INSENSITIVE_SELECTOR =
            species -> !species.config.GENERAL.isDamagedByWater.get();
    /**
     * Returns true if the species' block height is less than or equal to the base vanilla entity's.
     */
    private static final Predicate<MobFamily.Species<?>> NO_GIANTS_SELECTOR = MobFamily.Species::isNotGiant;
    
    
    /**
     * Called when an entity is spawned in the world.
     * <p>
     * This event may be called before the chunk is loaded; you will cause chunk loading deadlocks
     * if you do not delay your world interactions.
     * <p>
     * If the event is canceled, the entity will not be added to the level.
     *
     * @param event The event data.
     */
    @SubscribeEvent( priority = EventPriority.LOWEST )
    public static void onEntityJoinLevel( EntityJoinLevelEvent event ) {
        if( !event.isCanceled() && !event.getLevel().isClientSide() && !event.loadedFromDisk() &&
                // Trigger a finalize spawn event for silverfish so we (and other mods, I guess) can properly process them
                EntityType.SILVERFISH.equals( event.getEntity().getType() ) && event.getLevel() instanceof ServerLevel level &&
                event.getEntity() instanceof Mob mob && mob.getSpawnType() == null ) {
            ForgeEventFactory.onFinalizeSpawn( mob, level, level.getCurrentDifficultyAt( mob.blockPosition() ),
                    MobSpawnType.TRIGGERED, null, null );
        }
    }
    
    /**
     * Called when a mob is being finalized before being added to the world.
     * <br><br>
     * This checks whether the entity belongs to a special mob family and appropriately marks the mob to be replaced
     * by the tick handler after deciding whether the mob should be spawned as a special variant species.
     *
     * @param event The event data.
     */
    @SubscribeEvent( priority = EventPriority.LOWEST )
    public static void onFinalizeSpawn( MobSpawnEvent.FinalizeSpawn event ) {
        // Check if replacement is even enabled.
        if( !Config.MAIN.GENERAL.enableMobReplacement.get() ) return;
        
        final ServerLevel level = event.getLevel().getLevel();
        final SpawnType spawnType = SpawnType.fromVanilla( event.getSpawnType() );
        
        // Check if the spawn type is one that should be skipped.
        if( spawnType == null || Config.MAIN.GENERAL.useNaturalSpawner.get() && spawnType == SpawnType.NATURAL ||
                Config.MAIN.GENERAL.skippedSpawnTypes.get().contains( spawnType.toString() ) ) return;
        
        final Mob mob = event.getEntity();
        final MobFamily<?, ?> mobFamily = getReplacingMobFamily( mob );
        
        if( mobFamily != null ) {
            // If we are on the server thread, execute immediately so we can cancel the original spawn.
            // If not, schedule the check to be done on the server thread.
            if( level.getServer().isSameThread() ) {
                if( checkShouldReplace( level, mob, mobFamily, spawnType ) )
                    // Cancel spawn event if replacement is happening right away.
                    event.setSpawnCancelled( true );
            }
            else {
                level.getServer().execute( () -> checkShouldReplace( level, mob, mobFamily, spawnType ) );
            }
        }
    }
    
    /**
     * Checks if the given entity should be replaced.
     *
     * @param level     The level the entity is spawning in.
     * @param mob       The entity to consider replacing.
     * @param mobFamily The mob family to use for replacement.
     * @param spawnType The spawn type of the entity we are replacing.
     * @return True if the original entity will be replaced right away. Returns false
     * if the entity shouldn't be replaced, or if it should be replaced but later.
     */
    private static boolean checkShouldReplace( Level level, Mob mob, MobFamily<?, ?> mobFamily, SpawnType spawnType ) {
        final BlockPos entityPos = BlockPos.containing( mob.position() );
        
        // FinalizeSpawn should never be called multiple times on an entity, but who knows.
        setInitFlag( mob );
        
        // If we for whatever reason are not in a loaded chunk, delay replacement.
        if( EnvironmentHelper.isLoaded( level, entityPos ) ) {
            final boolean isSpecial = shouldMakeNextSpecial( mobFamily, level, entityPos );
            
            if( shouldReplace( mobFamily, isSpecial ) ) {
                TO_REPLACE.addLast( new MobReplacementEntry( mobFamily, isSpecial, spawnType, mob, level, entityPos ) );
                return true;
            }
        }
        else {
            DELAYED_REPLACE.add( new DelayedMobReplacementEntry( mobFamily, spawnType, mob, level, entityPos ) );
        }
        return false;
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
                TO_REPLACE.removeFirst().replace();
            }
        }
    }
    
    /**
     * @param mob The entity to test.
     * @return True if this mod's init flag has been set for the entity.
     */
    private static boolean getInitFlag( Mob mob ) {
        final CompoundTag forgeData = mob.getPersistentData();
        return NBTHelper.containsNumber( forgeData, References.TAG_INIT ) &&
                forgeData.getBoolean( References.TAG_INIT );
    }
    
    /**
     * Sets the init flag for an entity, causing the mod to ignore any future loading of the entity into the world.
     *
     * @param mob The entity to set this mod's init flag for.
     */
    private static void setInitFlag( Mob mob ) {
        final CompoundTag forgeData = mob.getPersistentData();
        forgeData.putBoolean( References.TAG_INIT, true );
    }
    
    /**
     * @return The mob family to replace with, or null if the mob is not replaceable.
     */
    @Nullable
    private static MobFamily<?, ?> getReplacingMobFamily( @Nullable Mob mob ) {
        if( mob == null || getInitFlag( mob ) ) return null;
        return MobFamily.getReplacementFamily( mob );
    }
    
    /**
     * @return True if the next mob should be made a special variant.
     * Returns false if something goes wrong.
     */
    private static boolean shouldMakeNextSpecial( MobFamily<?, ?> mobFamily, Level level, BlockPos entityPos ) {
        try {
            return mobFamily.config.GENERAL.specialVariantChance.rollChance( level.random, EnvironmentContext.withTarget( level, entityPos ) );
        }
        catch( Exception ex ) {
            SpecialMobs.LOG.warn( "Could not get special variant chance for mob family '{}'! Is the family's config broken?", mobFamily.name, ex );
            return false;
        }
    }
    
    /**
     * @return True if a mob should be replaced.
     */
    private static boolean shouldReplace( MobFamily<?, ?> mobFamily, boolean isSpecial ) {
        return isSpecial || Config.MAIN.GENERAL.masterVanillaReplacement.get() && mobFamily.config.GENERAL.vanillaReplacement.get();
    }
    
    /**
     * @return A selector that filters out variants that are likely to die a stupid death if chosen.
     */
    @Nullable
    private static Predicate<MobFamily.Species<?>> getVariantFilter( MobFamily<?, ?> mobFamily, Mob mobToReplace,
                                                                     Level level, BlockPos pos ) {
        Predicate<MobFamily.Species<?>> selector = null;
        
        // Note that we do not check for any fluids (water/lava) since that is handled by spawn logic
        if( !mobFamily.vanillaReplacement.bestiaryInfo.isDamagedByWater && // Skip this check if the base vanilla mob dies in water
                level.isRainingAt( pos ) ) {
            selector = WATER_INSENSITIVE_SELECTOR;
        }
        
        // Does not consider overly wide mobs or extra-tall (>1 block taller) mobs
        if( mobFamily.hasAnyGiants() ) {
            final AABB bb = mobToReplace.getBoundingBox();
            final int y = Mth.ceil( bb.maxY );
            
            // Only check the FULL block above current collision - not a perfect representation, but keeps things simple
            if( !level.isUnobstructed( mobToReplace, Shapes.create( new AABB(
                    bb.minX, y, bb.minZ, bb.maxX, y + 1, bb.maxZ ) ) ) ) {
                selector = selector == null ? NO_GIANTS_SELECTOR : selector.and( NO_GIANTS_SELECTOR );
            }
        }
        return selector;
    }
    
    
    /**
     * All data needed for a single mob we want to replace.
     */
    private record MobReplacementEntry( MobFamily<?, ?> mobFamily, boolean isSpecial, SpawnType mobSpawnType,
                                        Mob mobToReplace, Level level, BlockPos pos ) {
        /**
         * Replaces a mob, copying over all its data to the replacement.
         */
        void replace() {
            // Make sure the chunk the entity is in is loaded
            if( !EnvironmentHelper.isLoaded( level, pos ) ) return;
            
            // Pick a replacement
            final MobFamily.Species<?> species = isSpecial ?
                    mobFamily.nextVariant( level, pos, getVariantFilter( mobFamily, mobToReplace, level, pos ) ) :
                    mobFamily.vanillaReplacement;
            
            // Make the replacement and then copy over most data from the mob we are replacing
            final Mob replacement = species.entityType.get().create( level );
            if( replacement == null ) {
                SpecialMobs.LOG.error( "Failed to create replacement entity '{}'", species.entityType.getId() );
                return;
            }
            final CompoundTag tag = new CompoundTag();
            mobToReplace.saveWithoutId( tag );
            tag.remove( "UUID" ); // Don't copy UUID
            replacement.load( tag );
            replacement.setHealth( replacement.getMaxHealth() );
            
            // Spawn the replacement
            MobHelper.finalizeSpawn( replacement, (ServerLevelAccessor) level,
                    level.getCurrentDifficultyAt( pos ), mobSpawnType.toVanilla(), null );
            level.addFreshEntity( replacement );
            
            // Try to maintain riders/mounts
            for( Entity rider : mobToReplace.getPassengers() ) {
                rider.stopRiding();
                rider.startRiding( replacement, true );
            }
            if( mobToReplace.getVehicle() != null ) {
                final Entity vehicle = mobToReplace.getVehicle();
                mobToReplace.stopRiding();
                replacement.startRiding( vehicle, true );
            }
            
            // Finally, obliterate the replaced mob
            mobToReplace.discard();
        }
    }
    
    /**
     * All data needed for a single mob we are waiting to replace.
     */
    private static class DelayedMobReplacementEntry {
        final MobFamily<?, ?> mobFamily;
        final SpawnType mobSpawnType;
        
        final Mob mobToReplace;
        final Level level;
        final BlockPos pos;
        
        int ticksRemaining = 6;
        
        DelayedMobReplacementEntry( MobFamily<?, ?> family, SpawnType spawnType, Mob mob, Level level, BlockPos pos ) {
            mobFamily = family;
            mobSpawnType = spawnType;
            
            mobToReplace = mob;
            this.level = level;
            this.pos = pos;
        }
        
        /**
         * Called each server tick to see if the mob is ready to be replaced. Return true when done.
         */
        boolean update() {
            if( ticksRemaining > 0 ) {
                ticksRemaining--;
            }
            else if( !mobToReplace.isAlive() || mobToReplace.isRemoved() ) {
                return true; // Mob was killed or unloaded before getting replaced
            }
            else if( EnvironmentHelper.isLoaded( level, pos ) ) {
                // It is time to decide!
                final boolean isSpecial = shouldMakeNextSpecial( mobFamily, level, pos );
                if( shouldReplace( mobFamily, isSpecial ) ) {
                    TO_REPLACE.addLast( new MobReplacementEntry( mobFamily, isSpecial, mobSpawnType, mobToReplace, level, pos ) );
                    mobToReplace.discard();
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