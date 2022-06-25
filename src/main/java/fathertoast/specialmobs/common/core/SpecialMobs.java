package fathertoast.specialmobs.common.core;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.core.register.SMEntities;
import fathertoast.specialmobs.common.core.register.SMItems;
import fathertoast.specialmobs.common.event.BiomeEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
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
     *  o general
     *      o entity replacer
     *      o dimension-sensitive configs
     *      o environment-sensitive configs
     *      ? natural spawning
     *  o entities
     *      - nbt-driven capabilities (special mob data)
     *      o fish hook
     *      o bug projectile
     *      + bestiary
     *      ? configurable stats
     *  - monster families (see doc for specifics)
     *      - creepers
     *          - chance to spawn charged during thunderstorms
     *          + scope
     *      - zombies
     *          o villager infection
     *          + transformations
     *          - ranged attack AI (using bow)
     *          - use shields
     *          + drowned
     *      o zombified piglins
     *          o ranged attack AI (using bow)
     *          o use shields
     *      - skeletons
     *          - use shields
     *          - babies
     *      - wither skeletons
     *          - use shields
     *          - babies
     *      o slimes
     *          o use attack damage attribute
     *      o magma cubes
     *          o use attack damage attribute
     *      - spiders
     *          o ranged attack AI
     *      - cave spiders
     *          o ranged attack AI
     *      - silverfish
     *          ? ranged attack AI
     *          + puffer
     *      - endermen
     *      o witches
     *          o ability to equip held items
     *      o ghasts
     *          o melee attack AI
     *      o blazes
     *          o melee attack AI
     *      ? piglins
     *      ? hoglins
     *      ? zoglins
     *      ? endermites
     *      ? guardians
     *      ? shulkers
     *      ? phantoms
     *      + the goat
     */
    
    /** Our mod ID. */
    @SuppressWarnings( "SpellCheckingInspection" )
    public static final String MOD_ID = "specialmobs";
    
    /** The path to the textures folder. */
    public static final String TEXTURE_PATH = "textures/entity/";
    
    /** Logger instance for the mod. */
    public static final Logger LOG = LogManager.getLogger( MOD_ID );
    
    //** Our mod's packet handler; takes care of networking and sending messages. */
    //@SuppressWarnings( "FieldCanBeLocal" )
    //private final PacketHandler packetHandler = new PacketHandler();
    
    public SpecialMobs() {
        Config.initialize();
        
        //packetHandler.registerMessages();
        
        //MinecraftForge.EVENT_BUS.register( new SMEventListener() );
        MinecraftForge.EVENT_BUS.register( new BiomeEvents() );
        
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        eventBus.addListener( SMEntities::createAttributes );
        
        SMEntities.REGISTRY.register( eventBus );
        SMItems.REGISTRY.register( eventBus );
        
        MobFamily.initBestiary();
    }
    
    /** @return A ResourceLocation with the mod's namespace. */
    public static ResourceLocation resourceLoc( String path ) { return new ResourceLocation( MOD_ID, path ); }
    
    /** @return Returns a Forge registry entry as a string, or "null" if it is null. */
    public static String toString( @Nullable ForgeRegistryEntry<?> regEntry ) { return regEntry == null ? "null" : toString( regEntry.getRegistryName() ); }
    
    /** @return Returns the resource location as a string, or "null" if it is null. */
    public static String toString( @Nullable ResourceLocation res ) { return res == null ? "null" : res.toString(); }
}