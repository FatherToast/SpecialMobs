package fathertoast.specialmobs.common.core;

import fathertoast.specialmobs.common.config.Config;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

@Mod( SpecialMobs.MOD_ID )
public class SpecialMobs {
    
    /* Feature List:
     *  TODO
     */
    
    /** Our mod ID. */
    @SuppressWarnings( "SpellCheckingInspection" )
    public static final String MOD_ID = "specialmobs";
    
    /** Logger instance for the mod. */
    public static final Logger LOG = LogManager.getLogger( MOD_ID );
    
    //** Our mod's packet handler; takes care of networking and sending messages. */
    //@SuppressWarnings( "FieldCanBeLocal" )
    //private final PacketHandler packetHandler = new PacketHandler();
    
    //** Mod API instance **/
    //private final INaturalAbsorption modApi = new NaturalAbsorptionAPI();
    
    
    public SpecialMobs() {
        Config.initialize();
        
        //packetHandler.registerMessages();
        //CraftingUtil.registerConditions();
        
        //MinecraftForge.EVENT_BUS.register( new NAEventListener() );
        //MinecraftForge.EVENT_BUS.register( new HeartManager() );
        
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        //modBus.addListener( this::onInterModProcess );
        //modBus.addListener( HeartManager::onEntityAttributeCreation );
        
        //NAItems.ITEMS.register( modBus );
        //NAAttributes.ATTRIBUTES.register( modBus );
        //NAEnchantments.ENCHANTMENTS.register( modBus );
        //NALootModifiers.LOOT_MODIFIER_SERIALIZERS.register( modBus );
        
        //if( ModList.get().isLoaded( "tconstruct" ) ) {
        //    NaturalAbsorptionTC.init( modBus );
        //}
    }
    
//    /**
//     * Hands the mod API to mods that ask for it.
//     */
//    private void onInterModProcess( InterModProcessEvent event ) {
//        event.getIMCStream().forEach( ( message ) -> {
//            if( message.getMethod().equals( "getNaturalAbsorptionAPI" ) ) {
//                Supplier<Function<INaturalAbsorption, Void>> supplier = message.getMessageSupplier();
//                supplier.get().apply( modApi );
//            }
//        } );
//    }
    
    /** @return A ResourceLocation with the mod's namespace. */
    public static ResourceLocation resourceLoc( String path ) { return new ResourceLocation( MOD_ID, path ); }
    
    /** @return Returns a Forge registry entry as a string, or "null" if it is null. */
    public static String toString( @Nullable ForgeRegistryEntry<?> regEntry ) { return regEntry == null ? "null" : toString( regEntry.getRegistryName() ); }
    
    /** @return Returns the resource location as a string, or "null" if it is null. */
    public static String toString( @Nullable ResourceLocation res ) { return res == null ? "null" : res.toString(); }
}