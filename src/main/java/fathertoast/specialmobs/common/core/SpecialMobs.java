package fathertoast.specialmobs.common.core;

import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.core.register.*;
import fathertoast.specialmobs.common.event.AdvancementFixer;
import fathertoast.specialmobs.common.event.GameEvents;
import fathertoast.specialmobs.common.event.NaturalSpawnManager;
import fathertoast.specialmobs.common.network.PacketHandler;
import fathertoast.specialmobs.common.util.SMDispenserBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

@Mod( SpecialMobs.MOD_ID )
public class SpecialMobs {
    /* Feature List:
     * (KEY: - = complete in current version, o = incomplete feature from previous version,
     *       + = incomplete new feature, ? = feature to consider adding)
     *  - general
     *      - mob replacer
     *      - environment-sensitive configs
     *  - natural spawning
     *      - copied spawns
     *          - vanilla spiders -> vanilla cave spiders
     *          - vanilla endermen -> ender creepers
     *      - ocean/river spawns
     *          - drowning creepers
     *          - blueberry slimes
     *      - nether spawns
     *          - vanilla wither skeletons (outside of fortresses)
     *          - vanilla blazes (outside of fortresses)
     *          - fire creepers/zombies/spiders
     *          ? warped/crimson mobs
     *      + phantom spawns
     *  - potions
     *      - vulnerability (opposite of resistance)
     *      - weight (opposite of levitation)
     *  - blocks
     *      - infested coral (spawns puffer silverfish)
     *      - melting ice (similar to frosted ice)
     *  - entities
     *      + TODO bestiary
     *      - configurable, nbt-driven stats (bestiary info + special mob data)
     *      - configurable weapon type chance
     *      - bone shrapnel
     *      - bug spit
     *      - fish hook
     *      + Vanishing boat (for pirate skeleton)
     *  - monster families (see doc for specifics)
     *      - creepers
     *          - Scope creeper
     *          - chance to spawn charged during thunderstorms
     *          - chance to become supercharged when charged
     *          - explosion stats (while wet, while burning, when shot)
     *      - zombies
     *          - transformations (husk -> any other non-water-sensitive zombie -> analogous drowned)
     *          - ranged attack AI (using bow)
     *          - use shields
     *      - drowned
     *          - use shields
     *          - bug fixes (can move in shallow water, alert regular zombies)
     *      - zombified piglins
     *          - ranged attack AI (using bow)
     *          + ranged attack AI (using crossbow)
     *          - use shields
     *      - skeletons
     *          + Pirate skeleton
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
     *      - silverfish
     *          - ranged attack AI (spitter)
     *          - chance to spawn already calling for reinforcements
     *      - endermen
     *      - witches
     *          - ability to equip held items (wonky)
     *          - use splash speed instead of regular
     *      - ghasts
     *          + Slab ghast
     *          - melee attack AI
     *          - remove vertical targeting restriction
     *      - blazes
     *          - melee attack AI
     *          - configurable fireball attack
     *      + guardians
     *          + vortex
     *      + phantoms
     *      + the goat
     *      ? hoglins
     *      ? zoglins
     *      ? shulkers
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
        
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        SMBlocks.REGISTRY.register( modEventBus );
        SMItems.REGISTRY.register( modEventBus );
        SMCreativeTabs.REGISTRY.register( modEventBus );
        SMEntities.REGISTRY.register( modEventBus );
        SMEffects.REGISTRY.register( modEventBus );
        SMBiomeMods.REGISTRY.register( modEventBus );

        modEventBus.addListener( SMEntities::createAttributes );
        modEventBus.addListener( SMCreativeTabs::buildCreativeContents );
        modEventBus.addListener( this::setup );
        modEventBus.addListener( this::sendIMCMessages );
        modEventBus.addListener( NaturalSpawnManager::registerSpawnPlacements );

        MinecraftForge.EVENT_BUS.register( new GameEvents() );
        MinecraftForge.EVENT_BUS.register( new AdvancementFixer() );
    }
    
    public void setup( FMLCommonSetupEvent event ) {
        event.enqueueWork(() -> {
            SMDispenserBehavior.registerBehaviors();
        });
    }
    
    public void sendIMCMessages( InterModEnqueueEvent event ) {

    }
    
    /** @return A ResourceLocation with the mod's namespace. */
    public static ResourceLocation resourceLoc(String path ) { return new ResourceLocation( MOD_ID, path ); }
    
    /** @return Returns a Forge registry entry as a string, or "null" if it is null. */
    public static <T> String toString( @Nullable T regEntry, IForgeRegistry<T> registry ) {
        return regEntry == null || !registry.containsValue(regEntry)
                ? "null"
                : toString( registry.getKey(regEntry) );
    }
    
    /** @return Returns the resource location as a string, or "null" if it is null. */
    public static String toString( @Nullable ResourceLocation res ) { return res == null ? "null" : res.toString(); }
}