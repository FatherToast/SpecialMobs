package toast.specialMobs;

import java.util.ArrayDeque;

import net.minecraft.entity.EntityLiving;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class TickHandler
{
    // Stack of entities that need to be spawned.
    public static ArrayDeque<ReplacementEntry> entityStack = new ArrayDeque();

    public TickHandler() {
        FMLCommonHandler.instance().bus().register(this);
    }

    /**
     * Called each tick.
     * TickEvent.Type type = the type of tick.
     * Side side = the side this tick is on.
     * TickEvent.Phase phase = the phase of this tick (START, END).
     *
     * @param event The event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (!TickHandler.entityStack.isEmpty()) {
                ReplacementEntry entry;
                for (int limit = 10; limit-- > 0;) {
                    entry = TickHandler.entityStack.pollFirst();
                    if (entry == null) {
                        break;
                    }
                    entry.replace();
                }
            }
        }
    }

    // Puts the mob into the stack.
    public static void markEntityToBeReplaced(EntityLiving entity) {
        TickHandler.entityStack.add(new ReplacementEntry(entity));
    }
}