package fathertoast.specialmobs.common.core;

import fathertoast.specialmobs.common.compat.top.SMTheOneProbe;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.core.register.SMEffects;
import fathertoast.specialmobs.common.core.register.SMEntities;
import fathertoast.specialmobs.common.core.register.SMItems;
import fathertoast.specialmobs.common.event.BiomeEvents;
import fathertoast.specialmobs.common.event.GameEvents;
import fathertoast.specialmobs.common.network.PacketHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

@Mod( SpecialMobs.MOD_ID )
public class SpecialMobs {
    
    /* TODO List:
     *  Reimplement all old features (see list below)
     *  Utility features:
     *      - Bestiary
     */
    
    /* Feature List: //TODO; list may not be complete
     * (KEY: - = complete in current version, o = incomplete feature from previous version,
     *       + = incomplete new feature, ? = feature to consider adding)
     *  - general
     *      - entity replacer
     *      - environment-sensitive configs
     *      + natural spawning
     *          o nether spawns
     *          o end spawns
     *          ? ocean/water spawns
     *  - potions
     *      - vulnerability (opposite of resistance)
     *      ? gravity (opposite of levitation)
     *  - entities
     *      - nbt-driven capabilities (special mob data)
     *      - fish hook
     *      - bug spit
     *      + bestiary
     *      - configurable stats
     *  - monster families (see doc for specifics)
     *      - creepers
     *          - chance to spawn charged during thunderstorms
     *          + scope - perhaps delay this until 1.18 where spyglasses will be in the game
     *      - zombies
     *          - transformations (husk -> any other non-water-sensitive zombie -> analogous drowned)
     *          - ranged attack AI (using bow)
     *          - use shields
     *      + drowned
     *      - zombified piglins
     *          - ranged attack AI (using bow)
     *          + ranged attack AI (using crossbow)
     *          - use shields
     *      - skeletons
     *          - use shields
     *          - melee chance
     *          - babies
     *      - wither skeletons
     *          - use shields
     *          - bow chance
     *          - babies
     *      - slimes
     *          - smallest size can deal damage
     *      - magma cubes
     *      - spiders
     *          - ranged attack AI (spitter)
     *      - cave spiders
     *          - ranged attack AI (spitter)
     *          + natural spawning
     *      - silverfish
     *          - ranged attack AI (spitter)
     *      - endermen
     *      - witches
     *          - ability to equip held items (wonky)
     *          - uses splash speed instead of regular
     *      - ghasts
     *          - melee attack AI
     *      - blazes
     *          - melee attack AI
     *      ? hoglins
     *      ? zoglins
     *      + guardians
     *          + vortex
     *      ? shulkers
     *      + phantoms
     *          + natural spawning
     *      + the goat
     */
    
    /** Our mod ID. */
    @SuppressWarnings( "SpellCheckingInspection" )
    public static final String MOD_ID = "specialmobs";
    
    /** Logger instance for the mod. */
    public static final Logger LOG = LogManager.getLogger( MOD_ID );
    
    /** Our mod's packet handler; takes care of networking and sending messages. */
    @SuppressWarnings( "FieldCanBeLocal" )
    private final PacketHandler packetHandler = new PacketHandler();
    
    public SpecialMobs() {
        Config.initialize();
        
        packetHandler.registerMessages();
        
        MinecraftForge.EVENT_BUS.register( new BiomeEvents() );
        MinecraftForge.EVENT_BUS.register( new GameEvents() );
        
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        eventBus.addListener( SMEntities::createAttributes );
        eventBus.addListener( this::sendIMCMessages );
        
        SMEffects.REGISTRY.register( eventBus );
        SMEntities.REGISTRY.register( eventBus );
        SMItems.REGISTRY.register( eventBus );
    }
    
    // TODO - This could very well help out the config malformation issue
    //        Only problem here is that this event is never fired apparently.
    //        Perhaps DeferredWorkQueue.runLater() could work (ignore deprecation, simply marked for removal)
    public void onParallelDispatch( FMLConstructModEvent event ) {
        event.enqueueWork( Config::initialize );
    }
    
    public void sendIMCMessages( InterModEnqueueEvent event ) {
        if( ModList.get().isLoaded( "theoneprobe" ) ) {
            InterModComms.sendTo( "theoneprobe", "getTheOneProbe", SMTheOneProbe::new );
        }
    }
    
    /** @return A ResourceLocation with the mod's namespace. */
    public static ResourceLocation resourceLoc( String path ) { return new ResourceLocation( MOD_ID, path ); }
    
    /** @return Returns a Forge registry entry as a string, or "null" if it is null. */
    public static String toString( @Nullable ForgeRegistryEntry<?> regEntry ) { return regEntry == null ? "null" : toString( regEntry.getRegistryName() ); }
    
    /** @return Returns the resource location as a string, or "null" if it is null. */
    public static String toString( @Nullable ResourceLocation res ) { return res == null ? "null" : res.toString(); }
}