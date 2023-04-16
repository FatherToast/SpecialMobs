package fathertoast.specialmobs.common.event;

import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.UUID;

/**
 * Class that tracks all players' current positions and their positions last tick.
 * <p>
 * If we want to get serious here, we could catch teleport/portal events and reset the tracker as well.
 */
@Mod.EventBusSubscriber( modid = SpecialMobs.MOD_ID )
public class PlayerVelocityWatcher {
    
    /** A map that tracks all players in existence. */
    private static final HashMap<UUID, Entry> TRACKER = new HashMap<>();
    
    /** Number of server ticks since the last cleanup. */
    private static int cleanupCounter;
    
    /** @return The player's velocity tracker entry. If none exists, a new one will be created. */
    public static Entry get( Player player ) {
        Entry trackerEntry = TRACKER.get( player.getUUID() );
        if( trackerEntry == null ) {
            trackerEntry = new Entry( player.position() );
            TRACKER.put( player.getUUID(), trackerEntry );
        }
        return trackerEntry;
    }
    
    /** @return The entity's velocity. Uses the player tracker to work for players, too. */
    public static Vec3 getVelocity( Entity entity ) {
        return entity instanceof Player player ? get( player ).velocity() : entity.getDeltaMovement();
    }
    
    /** Called for every player tick event. */
    @SubscribeEvent
    protected static void onPlayerTick( TickEvent.PlayerTickEvent event ) {
        if( event.side.isServer() && event.phase == TickEvent.Phase.END ) {
            Entry trackerEntry = get( event.player );
            trackerEntry.update( event.player.position() );
        }
    }
    
    /** Called for every server tick event. */
    @SubscribeEvent
    protected static void onServerTick( TickEvent.ServerTickEvent event ) {
        // Periodically remove all currently disconnected players;
        //  this is a hugely low priority, so we only do it about once per hour
        if( event.phase == TickEvent.Phase.END && ++cleanupCounter >= 69_420 ) {
            cleanupCounter = 0;
            final PlayerList onlinePlayers = ServerLifecycleHooks.getCurrentServer().getPlayerList();
            TRACKER.keySet().removeIf( ( uuid ) -> onlinePlayers.getPlayer( uuid ) == null );
        }
    }
    
    /** Called when the server starts shutting down. */
    @SubscribeEvent
    protected static void onServerStopping( ServerStoppingEvent event ) {
        cleanupCounter = 0;
        TRACKER.clear();
    }
    
    /** Stores the tracked data for a single player. */
    public static class Entry {
        
        public double xPrev, yPrev, zPrev, x, y, z;
        
        private Entry( Vec3 pos ) { reset( pos ); }
        
        /** @return X-displacement since the last tick. */
        public double dX() { return x - xPrev; }
        
        /** @return Y-displacement since the last tick. */
        public double dY() { return y - yPrev; }
        
        /** @return Z-displacement since the last tick. */
        public double dZ() { return z - zPrev; }
        
        /** @return Displacement since the last tick; this is effectively 'instantaneous velocity'. */
        public Vec3 velocity() { return new Vec3( dX(), dY(), dZ() ); }
        
        /** @return True if speed is non-zero (above a small dead zone). */
        public boolean isMoving() { return Math.abs( dX() ) > 1.0E-4 || Math.abs( dZ() ) > 1.0E-4 || Math.abs( dY() ) > 1.0E-4; }
        
        /** Sets the current and previous positions to the same, current values. */
        public void reset( Vec3 pos ) {
            xPrev = x = pos.x;
            yPrev = y = pos.y;
            zPrev = z = pos.z;
        }
        
        /** Called once per tick to update the current and previous positions. */
        private void update( Vec3 pos ) {
            xPrev = x;
            yPrev = y;
            zPrev = z;
            x = pos.x;
            y = pos.y;
            z = pos.z;
        }
    }
}