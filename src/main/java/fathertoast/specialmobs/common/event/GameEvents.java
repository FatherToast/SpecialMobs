package fathertoast.specialmobs.common.event;

import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GameEvents {

    @SubscribeEvent( priority = EventPriority.NORMAL )
    public void onLivingHurt( LivingHurtEvent event ) {

    }

    @SubscribeEvent( priority = EventPriority.NORMAL )
    public void onLivingFall( LivingFallEvent event ) {

    }
}