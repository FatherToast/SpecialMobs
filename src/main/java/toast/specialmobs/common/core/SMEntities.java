package toast.specialmobs.common.core;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.entity.Entity;
import net.modificationstation.stationapi.api.event.entity.EntityRegisterEvent;

public final class SMEntities {
    
    @SuppressWarnings( "unused" )
    @EventListener
    public static void registerEntities( EntityRegisterEvent event ) {
    
    }
    
    /** Convenience method for registering an entity. */
    private static void register( EntityRegisterEvent event, String name, Class<? extends Entity> entityClass ) {
        event.register( SpecialMobs.id( name ), entityClass );
    }
    
    
    private SMEntities() { }
}
