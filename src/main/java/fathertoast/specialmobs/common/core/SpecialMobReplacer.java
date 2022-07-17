package fathertoast.specialmobs.common.core;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;

@Mod.EventBusSubscriber( modid = SpecialMobs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE )
public final class SpecialMobReplacer {
    /** List of data for mobs needing replacement. */
    private static final Deque<MobReplacementEntry> TO_REPLACE = new ArrayDeque<>();
    
    /**
     * Called when any entity is spawned into the world by any means (such as natural/spawner spawns or chunk loading).
     * <p>
     * This checks whether the entity belongs to a special mob family and appropriately marks the mob to be replaced
     * by the tick handler after deciding whether the mob should be spawned as a special variant species.
     *
     * @param event The event data.
     */
    @SubscribeEvent( priority = EventPriority.LOWEST )
    public static void onEntitySpawn( EntityJoinWorldEvent event ) {
        if( event.getWorld().isClientSide() || !Config.MAIN.GENERAL.enableMobReplacement.get() || event.isCanceled() )
            return;
        
        final Entity entity = event.getEntity();
        final MobFamily<?, ?> mobFamily = getReplacingMobFamily( entity );
        if( mobFamily != null ) {
            final World world = event.getWorld();
            final BlockPos entityPos = new BlockPos( entity.position() );
            
            setInitFlag( entity ); // Do this regardless of replacement, should help prevent bizarre save glitches
            
            final boolean isSpecial = shouldMakeNextSpecial( mobFamily, world, entityPos );
            if( shouldReplace( mobFamily, isSpecial ) ) {
                TO_REPLACE.addLast( new MobReplacementEntry( mobFamily, isSpecial, entity, world, entityPos ) );
                
                // Sadly, it's somewhat of a pain to make sure no warnings get logged
                // when dealing with mounts/riders... Maybe someday :(
                event.setCanceled( true );
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
        final CompoundNBT forgeData = entity.getPersistentData();
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
        final CompoundNBT forgeData = entity.getPersistentData();
        forgeData.putBoolean( References.TAG_INIT, true );
    }
    
    /** @return The mob family to replace with, or null if the mob is not replaceable. */
    @Nullable
    private static MobFamily<?, ?> getReplacingMobFamily( @Nullable Entity entity ) {
        if( entity == null || getInitFlag( entity ) ) return null;
        return MobFamily.getReplacementFamily( entity );
    }
    
    /** @return True if the next mob should be made a special variant. */
    private static boolean shouldMakeNextSpecial( MobFamily<?, ?> mobFamily, World world, BlockPos entityPos ) {
        return world.random.nextFloat() < mobFamily.config.GENERAL.specialVariantChance.get(); //TODO environment exceptions
    }
    
    /** @return True if a mob should be replaced. */
    private static boolean shouldReplace( MobFamily<?, ?> mobFamily, boolean isSpecial ) {
        return isSpecial || mobFamily.config.GENERAL.vanillaReplacement.get();
    }
    
    /** Replaces a mob, copying over all its data to the replacement. */
    private static void replace( MobFamily<?, ?> mobFamily, boolean isSpecial, Entity entityToReplace, World world, BlockPos entityPos ) {
        if( !(world instanceof IServerWorld) ) return;
        
        final CompoundNBT tag = new CompoundNBT();
        entityToReplace.saveWithoutId( tag );
        
        // Don't copy UUID
        tag.remove( "UUID" );
        
        final MobFamily.Species<?> species = isSpecial ? mobFamily.nextVariant( world, entityPos ) : mobFamily.vanillaReplacement;
        
        final LivingEntity replacement = species.entityType.get().create( world );
        if( replacement == null ) {
            SpecialMobs.LOG.error( "Failed to create replacement entity '{}'", species.entityType.getId() );
            return;
        }
        
        replacement.load( tag );
        MobHelper.finalizeSpawn( replacement, (IServerWorld) world, world.getCurrentDifficultyAt( entityPos ), null, null );
        
        for( Entity rider : entityToReplace.getPassengers() ) {
            rider.startRiding( replacement, true );
        }
        if( entityToReplace.getVehicle() != null ) {
            replacement.startRiding( entityToReplace.getVehicle(), true );
        }
        
        entityToReplace.remove();
        world.addFreshEntity( replacement );
    }
    
    /** All data needed for a single mob we want to replace. */
    private static class MobReplacementEntry {
        final MobFamily<?, ?> mobFamily;
        final boolean isSpecial;
        
        final LivingEntity entityToReplace;
        final World entityWorld;
        final BlockPos entityPos;
        
        MobReplacementEntry( MobFamily<?, ?> family, boolean special, Entity entity, World world, BlockPos pos ) {
            mobFamily = family;
            isSpecial = special;
            
            entityToReplace = (LivingEntity) entity;
            entityWorld = world;
            entityPos = pos;
        }
    }
}