package fathertoast.specialmobs;

import fathertoast.specialmobs.ai.*;
import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.config.*;
import fathertoast.specialmobs.entity.projectile.*;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

@SuppressWarnings( "WeakerAccess" )
@Mod( modid = SpecialMobsMod.MOD_ID, name = SpecialMobsMod.NAME, version = SpecialMobsMod.VERSION )
public
class SpecialMobsMod
{
	public static final String MOD_ID  = "specialmobs";
	public static final String NAME    = "Special Mobs";
	public static final String VERSION = "1.0.2_for_mc1.12.2";
	
	/*
	 * Primary features:
	 *  - Re-implement ranged capabilities for (cave) spiders?
	 *
	 * Utility features:
	 *  - Bestiary
	 *
	 * Notes:
	 *  - Texture update packets are untested
	 */
	
	/** Handles operations when different behaviors are needed between the client and server sides. */
	@SidedProxy( modId = SpecialMobsMod.MOD_ID, clientSide = "fathertoast.specialmobs.client.ClientProxy", serverSide = "fathertoast.specialmobs.server.ServerProxy" )
	public static SidedModProxy sidedProxy;
	
	/** The namespace used by this mod. */
	public static final String NAMESPACE       = SpecialMobsMod.MOD_ID + ":";
	/** The path to the textures folder. */
	public static final String TEXTURE_PATH    = SpecialMobsMod.NAMESPACE + "textures/entity/";
	/** The path to the loot tables folder. */
	public static final String LOOT_TABLE_PATH = SpecialMobsMod.NAMESPACE + "entities/";
	
	private static Logger logger;
	
	/** @return The logger used by this mod. */
	public static
	Logger log( ) { return logger; }
	
	private static SimpleNetworkWrapper networkWrapper;
	
	/** @return The network channel for this mod. */
	public static
	SimpleNetworkWrapper network( ) { return networkWrapper; }
	
	@Mod.EventHandler
	public
	void preInit( FMLPreInitializationEvent event )
	{
		logger = event.getModLog( );
		
		Config.load( log( ), "Special_Mobs", event.getModConfigurationDirectory( ) );
		
		boolean isInDev = (Boolean) Launch.blackboard.get( "fml.deobfuscatedEnvironment" );
		if( isInDev ) {
			/* This line updates the loot table asset files.
			 * Assets are copied to a temporary directory on run, so the game needs to be started a second time
			 * before the updated loot table assets can be tested in the game.
			 */
			fathertoast.specialmobs.loot.LootTableHelper.generateBaseLootTables( event.getModConfigurationDirectory( ) );
		}
		
		int id = -1;
		networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel( "SM|FX" );
		if( event.getSide( ) == Side.CLIENT ) {
			network( ).registerMessage( MessageTexture.Handler.class, MessageTexture.class, ++id, Side.CLIENT );
		}
		
		sidedProxy.registerRenderers( );
	}
	
	@Mod.EventHandler
	public
	void init( FMLInitializationEvent event )
	{
		if( Config.get( ).GENERAL.FANCY_FISHING_MOBS ) {
			ResourceLocation key = new ResourceLocation( "cast" );
			SpecialMobsMod.log( ).debug( "Overwriting vanilla property getter! (Ignore duplicate key '{}' message below)", key );
			Items.FISHING_ROD.addPropertyOverride( key, new IAngler.ItemFishingRodPropertyGetter( ) );
		}
		
		SpecialMobsMod.log( ).info( "Registering entities..." );
		long startTime = System.nanoTime( );
		registerEntities( );
		long estimatedTime = System.nanoTime( ) - startTime;
		SpecialMobsMod.log( ).info( "Registered entities in {} ms", estimatedTime / 1.0E6 );
		
		if( Config.get( ).GENERAL.SPAWNING ) {
			MinecraftForge.EVENT_BUS.register( new SpecialMobReplacer( ) );
		}
	}
	
	@Mod.EventHandler
	public
	void postInit( FMLPostInitializationEvent event ) { }
	
	/** Registers the entities in this mod and their resources (e.g., their loot tables). */
	private
	void registerEntities( )
	{
		// Register main mobs
		int registered = 0;
		int id         = -1;
		
		for( EnumMobFamily family : EnumMobFamily.values( ) ) {
			// Populate the class-to-family mappings
			for( Class replaceableClass : family.replaceableClasses ) {
				EnumMobFamily.CLASS_TO_FAMILY_MAP.put( replaceableClass, family );
			}
			
			// Register vanilla replacement
			registerVanillaReplacement( ++id, family );
			
			// Register special variants
			for( EnumMobFamily.Species variant : family.variants ) {
				registerSpecialVariant( ++id, variant );
				registered++;
			}
		}
		SpecialMobsMod.log( ).info( "Registered {} special variants!", registered );
		
		// Register other entities
		registerEntity( ++id, "SMFishHook", EntitySpecialFishHook.class, 64, 10, true );
	}
	
	/**
	 * Registers the vanilla replacement entity for a mob family.
	 *
	 * @param id     The suggested entity id.
	 * @param family The mob family to register for.
	 */
	private
	void registerVanillaReplacement( int id, EnumMobFamily family )
	{
		registerEntity( id, family.vanillaReplacement.unlocalizedName, family.vanillaReplacement.variantClass );
	}
	
	/**
	 * Registers a special variant mob, along with its loot table and spawn egg.
	 *
	 * @param id      The suggested entity id.
	 * @param variant The special variant to register.
	 */
	private
	void registerSpecialVariant( int id, EnumMobFamily.Species variant )
	{
		// Register the entity itself
		registerEntity( id, variant.unlocalizedName, variant.variantClass );
		
		// Register a loot table unique to the entity
		ResourceLocation lootTable = LootTableList.register(
			new ResourceLocation( SpecialMobsMod.LOOT_TABLE_PATH + variant.family.name.toLowerCase( ) + "/" + variant.name.toLowerCase( ) )
		);
		try {
			variant.variantClass.getField( "LOOT_TABLE" ).set( null, lootTable );
		}
		catch( IllegalAccessException | NoSuchFieldException ex ) {
			throw new RuntimeException( "Special variant class for " + variant.unlocalizedName + " has no valid 'LOOT_TABLE' field", ex );
		}
		
		// Register a spawn egg for the entity if they are enabled in the config
		if( Config.get( ).GENERAL.SPAWN_EGGS ) {
			EntityRegistry.registerEgg(
				new ResourceLocation( SpecialMobsMod.NAMESPACE + variant.unlocalizedName ),
				variant.family.eggBaseColor, variant.bestiaryInfo.eggSpotsColor );
		}
	}
	
	/** Registers an entity using the typical entitytracker settings for mobs. */
	private
	void registerEntity( int id, String unlocalizedName, Class< ? extends Entity > entityClass )
	{
		registerEntity( id, unlocalizedName, entityClass, 80, 3, true );
	}
	
	/** Registers an entity using the given entitytracker settings. */
	@SuppressWarnings( "SameParameterValue" )
	private
	void registerEntity( int id, String unlocalizedName, Class< ? extends Entity > entityClass, int trackingRange, int trackingFrequency, boolean trackVelocity )
	{
		EntityRegistry.registerModEntity(
			new ResourceLocation( SpecialMobsMod.NAMESPACE + unlocalizedName ), entityClass, unlocalizedName,
			id, this, trackingRange, trackingFrequency, trackVelocity
		);
	}
}
