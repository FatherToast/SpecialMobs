package toast.specialmobs.common.core;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.event.mod.InitEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.Entrypoint;
import net.modificationstation.stationapi.api.mod.entrypoint.EntrypointManager;
import net.modificationstation.stationapi.api.util.Identifier;
import net.modificationstation.stationapi.api.util.Namespace;
import net.modificationstation.stationapi.api.util.Null;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;

public final class SpecialMobs {
    
    ///
    /// IMPORTANT NOTES:
    ///  - Event listener classes must be listed in our fabric.mod.json!
    ///
    
    
    /* Feature List:
     * (KEY: - = complete in current version, o = incomplete feature from previous version,
     *   + = incomplete new feature, ? = feature to consider adding)
     *
     * MOBS
     *  o creepers
     *  o zombies
     *  o skeletons
     *  o spiders
     *  o slimes
     *  o ghasts
     *  ? pig zombies
     *
     * MOB REPLACER
     *  o spawn weights
     *  o conditions
     */
    
    static {
        EntrypointManager.registerLookup( MethodHandles.lookup() );
    }
    
    /** The global mod instance of Special Mobs. */
    @Entrypoint.Instance
    public static final SpecialMobs INSTANCE = Null.get();
    
    /** This mod's namespace. */
    @Entrypoint.Namespace
    public static final Namespace NAMESPACE = Null.get();
    
    /** A logger instance using this mod's namespace. */
    @Entrypoint.Logger
    public static final Logger LOG = Null.get();
    
    
    @SuppressWarnings( "unused" )
    @EventListener
    private static void serverInit( InitEvent event ) {
        LOG.info( "Server Initialized" );
    }
    
    /** @return An identifier under Special Mob's namespace with the given ID/path. */
    public static Identifier id( String id ) { return Identifier.of( NAMESPACE, id ); }
}
