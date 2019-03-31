package fathertoast.specialmobs.config;

import fathertoast.specialmobs.*;
import fathertoast.specialmobs.bestiary.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashSet;
import java.util.Set;


/**
 * This helper class manages and stores references to user-defined configurations.
 */
public
class Config
{
	// Returns the main config.
	public static
	Config get( )
	{
		return Config.INSTANCE;
	}
	
	public static
	void load( Logger logger, String fileName, File configDir )
	{
		Config.log = logger;
		Config.log.info( "Loading configs..." );
		long startTime = System.nanoTime( );
		
		// Global mod config
		Config.configLoading = new Configuration( new File( configDir, fileName + ".cfg" ) );
		Config.configLoading.load( );
		Config.INSTANCE = new Config( );
		Config.configLoading.save( );
		Config.configLoading = null;
		
		long estimatedTime = System.nanoTime( ) - startTime;
		Config.log.info( "Loaded configs in {} ms", estimatedTime / 1.0E6 );
	}
	
	private
	Config( ) { }
	
	public final GENERAL GENERAL = new GENERAL( );
	
	public
	class GENERAL extends PropertyCategory
	{
		@Override
		String name( ) { return "_general"; }
		
		@Override
		String comment( )
		{
			return "General and/or miscellaneous options.";
		}
		
		/*
		public final boolean DEBUG = prop(
			"_debug_mode", false,
			"If true, the mod will start up in debug mode."
		);
		*/
		
		public final boolean DISABLE_NAUSEA = prop(
			"disable_nausea_effects", false,
			"If true, prevents any of this mod\'s mobs from applying nausea when they normally would.\n" +
			"Use this if the screen warping from nausea hurts your face or makes you sick."
		);
		
		public final boolean FANCY_FISHING_MOBS = prop(
			"fancy_fishing_mobs", true,
			"Overrides the default fishing rod item animation so that it is compatible with fishing mobs from this mod.\n" +
			"Disable this if it causes problems with another mod for some reason. Fishing mobs will instead render a stick while casting."
		);
		
		public final float RANDOM_SCALING = prop(
			"random_scaling", 0.15F,
			"When greater than 0, mobs will have a random render scale applied. This is a visual effect only.\n" +
			"With a value of 0.2, mob scale can vary by 20% (+/- 10% from normal size).\n" +
			"This only affects special variants and vanilla-replacement mobs."
		);
		
		public final boolean SPAWN_EGGS = prop(
			"spawn_eggs", true,
			"If true, the mod will attempt to generate a spawn egg for each variant mob.\n" +
			"Disable this if your \'misc\' creative tab is too full, or if you need to for whatever reason."
		);
		
		public final boolean SPAWNING = prop(
			"spawning", true,
			"Set this to false to disable mob spawning from this mod entirely. Do this if you want\n" +
			"to handle the spawning through another mod."
		);
	}
	
	public final CREEPERS CREEPERS = new CREEPERS( );
	
	public
	class CREEPERS extends FamilyConfig
	{
		CREEPERS( ) { super( EnumMobFamily.CREEPER ); }
		
		public final float CHARGED_CHANCE = prop(
			"_charged_chance", 0.01F,
			"Chance for creepers to spawn charged during thunderstorms."
		);
	}
	
	public final ZOMBIES ZOMBIES = new ZOMBIES( );
	
	public
	class ZOMBIES extends FamilyConfig
	{
		ZOMBIES( ) { super( EnumMobFamily.ZOMBIE ); }
		
		public final float BOW_CHANCE = prop(
			"_bow_chance", 0.05F,
			"Chance for valid zombies to spawn with bows."
		);
		
		public final float CHICKEN_JOCKEY_CHANCE = prop(
			"_chicken_jockey_chance", 1.0F,
			"Chance for baby zombies to spawn riding on a pre-existing chicken near their spawn point.\n" +
			"The vanilla chance for this is 5%, like the forced chicken jockey version.\n" +
			"See the Forge config for zombie baby chance (forge.cfg)."
		);
		
		public final float CHICKEN_JOCKEY_FORCED_CHANCE = prop(
			"_chicken_jockey_forced_chance", 0.05F,
			"Chance for baby zombies to spawn with a chicken mount.\n" +
			"See the Forge config for zombie baby chance (forge.cfg)."
		);
	}
	
	public final ZOMBIE_PIGMEN ZOMBIE_PIGMEN = new ZOMBIE_PIGMEN( );
	
	public
	class ZOMBIE_PIGMEN extends FamilyConfig
	{
		ZOMBIE_PIGMEN( ) { super( EnumMobFamily.ZOMBIE_PIGMAN ); }
		
		public final float BOW_CHANCE = prop(
			"_bow_chance", 0.2F,
			"Chance for valid zombie pigmen to spawn with bows."
		);
		
		public final float CHICKEN_JOCKEY_CHANCE = prop(
			"_chicken_jockey_chance", 1.0F,
			"Chance for baby zombie pigmen to spawn riding on a pre-existing chicken near their spawn point.\n" +
			"The vanilla chance for this is 5%, like the forced chicken jockey version.\n" +
			"See the Forge config for zombie baby chance (forge.cfg)."
		);
		
		public final float CHICKEN_JOCKEY_FORCED_CHANCE = prop(
			"_chicken_jockey_forced_chance", 0.05F,
			"Chance for baby zombie pigmen to spawn with a chicken mount.\n" +
			"See the Forge config for zombie baby chance (forge.cfg)."
		);
	}
	
	public final SKELETONS SKELETONS = new SKELETONS( );
	
	public
	class SKELETONS extends FamilyConfig
	{
		SKELETONS( ) { super( EnumMobFamily.SKELETON ); }
		
		public final float BABY_CHANCE = prop(
			"_baby_chance", 0.05F,
			"Chance for skeletons to spawn as babies."
		);
		
		public final float BOW_CHANCE = prop(
			"_bow_chance", 0.95F,
			"Chance for valid skeletons to spawn with bows."
		);
	}
	
	public final WITHER_SKELETONS WITHER_SKELETONS = new WITHER_SKELETONS( );
	
	public
	class WITHER_SKELETONS extends FamilyConfig
	{
		WITHER_SKELETONS( ) { super( EnumMobFamily.WITHER_SKELETON ); }
		
		public final float BABY_CHANCE = prop(
			"_baby_chance", 0.05F,
			"Chance for wither skeletons to spawn as babies."
		);
		
		public final float BOW_CHANCE = prop(
			"_bow_chance", 0.05F,
			"Chance for valid wither skeletons to spawn with bows."
		);
	}
	
	public final SLIMES SLIMES = new SLIMES( );
	
	public
	class SLIMES extends FamilyConfig
	{
		SLIMES( ) { super( EnumMobFamily.SLIME ); }
		
		public final boolean TINY_SLIME_DAMAGE = prop(
			"_tiny_slime_damage", true,
			"Setting this to false makes tiny slimes incapable of dealing damage, like in vanilla.\n" +
			"I don\'t recommend disabling this, but the option is here if you want to revert the change."
		);
	}
	
	public final MAGMA_CUBES MAGMA_CUBES = new MAGMA_CUBES( );
	
	public
	class MAGMA_CUBES extends FamilyConfig
	{
		MAGMA_CUBES( ) { super( EnumMobFamily.MAGMA_CUBE ); }
	}
	
	public final SPIDERS SPIDERS = new SPIDERS( );
	
	public
	class SPIDERS extends FamilyConfig
	{
		SPIDERS( ) { super( EnumMobFamily.SPIDER ); }
	}
	
	public final CAVE_SPIDERS CAVE_SPIDERS = new CAVE_SPIDERS( );
	
	public
	class CAVE_SPIDERS extends FamilyConfig
	{
		CAVE_SPIDERS( ) { super( EnumMobFamily.CAVE_SPIDER ); }
	}
	
	public final SILVERFISH SILVERFISH = new SILVERFISH( );
	
	public
	class SILVERFISH extends FamilyConfig
	{
		SILVERFISH( ) { super( EnumMobFamily.SILVERFISH ); }
		
		public final float AGGRESSIVE_CHANCE = prop(
			"_aggressive_chance", 0.2F,
			"Chance for silverfish to spawn already calling for reinforcements."
		);
	}
	
	public final ENDERMEN ENDERMEN = new ENDERMEN( );
	
	public
	class ENDERMEN extends FamilyConfig
	{
		ENDERMEN( ) { super( EnumMobFamily.ENDERMAN ); }
		
		private final HashSet< TargetBlock > CARRIABLE_BLOCKS = prop(
			"_carriable_blocks", new Block[] {
				Blocks.GRASS, Blocks.MYCELIUM, Blocks.DIRT, Blocks.CLAY, Blocks.SAND, Blocks.GRAVEL,
				Blocks.YELLOW_FLOWER, Blocks.RED_FLOWER, Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM,
				Blocks.PUMPKIN, Blocks.MELON_BLOCK, Blocks.CACTUS,
				Blocks.TNT, Blocks.NETHERRACK
			},
			"Set of blocks that can be moved around by endermen. Removing all entries here disables enderman griefing.\n" +
			"Other mods can still add/remove blocks from this list, so be aware of which mods do.\n" +
			"Unlike most config options here, this affects vanilla endermen in addition to the endermen from this mod (and probably other mods)."
		);
		
		{
			// Fetch the vanilla carriable block set
			Set< Block > carriable = ObfuscationHelper.EntityEnderman_CARRIABLE_BLOCKS.get( );
			
			// Replace default carriable blocks with the blocks defined in the config
			if( carriable != null ) {
				carriable.clear( );
			}
			for( TargetBlock target : CARRIABLE_BLOCKS ) {
				EntityEnderman.setCarriable( target.BLOCK, true );
			}
		}
	}
	
	public final WITCHES WITCHES = new WITCHES( );
	
	public
	class WITCHES extends FamilyConfig
	{
		WITCHES( ) { super( EnumMobFamily.WITCH ); }
	}
	
	public final GHASTS GHASTS = new GHASTS( );
	
	public
	class GHASTS extends FamilyConfig
	{
		GHASTS( ) { super( EnumMobFamily.GHAST ); }
	}
	
	public final BLAZES BLAZES = new BLAZES( );
	
	public
	class BLAZES extends FamilyConfig
	{
		BLAZES( ) { super( EnumMobFamily.BLAZE ); }
	}
	
	// Assigns config options to all special variants in a family.
	public static abstract
	class FamilyConfig extends PropertyCategory
	{
		@Override
		String name( ) { return KEY.replace( ' ', '_' ); }
		
		@Override
		String comment( )
		{
			return "Options related to special " + KEY + ".";
		}
		
		@Override
		float[] defaultFltRange( )
		{
			return PropertyCategory.R_FLT_ONE;
		}
		
		final EnumMobFamily mobFamily;
		
		public final boolean REPLACE_VANILLA;
		
		private final float                 SPECIAL_CHANCE;
		private final EnvironmentListConfig SPECIAL_CHANCE_EXCEPTIONS;
		
		FamilyConfig( EnumMobFamily family )
		{
			super( family.key );
			if( family.config != null ) {
				throw new IllegalArgumentException( "Mob family '" + family.name + "' already has a config attached" );
			}
			mobFamily = family;
			family.config = this;
			
			REPLACE_VANILLA = prop(
				"_replace_vanilla", true,
				"When true, all vanilla " + family.key + " are replaced by \'vanilla-like\' versions.\n" +
				"This enables all of the mod's options that apply to special variants to also apply to vanilla mobs,\n" +
				"in addition to all the nbt editing capabilities that special mobs have."
			);
			
			SPECIAL_CHANCE = prop(
				"_special_chance", 0.6F,
				"The chance for " + family.key + " to spawn as special variants."
			);
			SPECIAL_CHANCE_EXCEPTIONS = prop(
				"_special_chance_exceptions", new TargetEnvironment.TargetDimension[] {
					new TargetEnvironment.TargetDimension( DimensionType.OVERWORLD, 0.25F ),
					new TargetEnvironment.TargetDimension( DimensionType.NETHER, 0.33F ),
					new TargetEnvironment.TargetDimension( DimensionType.THE_END, 0.33F )
				},
				"The chance for " + family.key + " to spawn as special variants when spawning in particular locations.\n" +
				"More specific locations take priority over others (biome < biome* < dimension < global setting)."
			);
			
			// Load the config settings for all this family's variant weighting
			for( EnumMobFamily.Species variant : mobFamily.variants ) {
				String name = variant.name.toLowerCase( );
				variant.bestiaryInfo.weight = prop(
					name + "_weight", variant.bestiaryInfo.weight,
					"The weight for the \'" + name + "\' variant to be picked when " + family.key + " spawn as special variants."
				);
				variant.bestiaryInfo.weightExceptions = prop(
					name + "_weight_exceptions", variant.bestiaryInfo.weightExceptions.ENTRIES.toArray( new TargetEnvironment[ 0 ] ),
					"The weight for the \'" + name + "\' variant to be picked in particular locations."
				);
			}
		}
		
		public
		float getSpecialChance( World world, BlockPos pos )
		{
			return SPECIAL_CHANCE_EXCEPTIONS.getValueForLocation( world, pos, SPECIAL_CHANCE );
		}
		
		public
		int getVariantWeight( World world, BlockPos pos, EnumMobFamily.Species variant )
		{
			return (int) variant.bestiaryInfo.weightExceptions.getValueForLocation( world, pos, variant.bestiaryInfo.weight );
		}
	}
	
	
	static         Logger        log;
	// Config file currently being loaded. Null when not loading any file.
	private static Configuration configLoading;
	private static Config        INSTANCE;
	
	// Contains basic implementations for all config option types, along with some useful constants.
	@SuppressWarnings( { "unused", "SameParameterValue" } )
	private static abstract
	class PropertyCategory
	{
		/** Range: { -INF, INF } */
		static final double[] R_DBL_ALL = { Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY };
		/** Range: { 0.0, INF } */
		static final double[] R_DBL_POS = { 0.0, Double.POSITIVE_INFINITY };
		/** Range: { 0.0, 1.0 } */
		static final double[] R_DBL_ONE = { 0.0, 1.0 };
		
		/** Range: { -INF, INF } */
		static final float[] R_FLT_ALL = { Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY };
		/** Range: { 0.0, INF } */
		static final float[] R_FLT_POS = { 0.0F, Float.POSITIVE_INFINITY };
		/** Range: { 0.0, 1.0 } */
		static final float[] R_FLT_ONE = { 0.0F, 1.0F };
		
		/** Range: { MIN, MAX } */
		static final int[] R_INT_ALL       = { Integer.MIN_VALUE, Integer.MAX_VALUE };
		/** Range: { -1, MAX } */
		static final int[] R_INT_TOKEN_NEG = { -1, Integer.MAX_VALUE };
		/** Range: { 0, MAX } */
		static final int[] R_INT_POS0      = { 0, Integer.MAX_VALUE };
		/** Range: { 1, MAX } */
		static final int[] R_INT_POS1      = { 1, Integer.MAX_VALUE };
		/** Range: { 0, SRT } */
		static final int[] R_INT_SRT_POS   = { 0, Short.MAX_VALUE };
		/** Range: { 0, 255 } */
		static final int[] R_INT_BYT_UNS   = { 0, 0xff };
		/** Range: { 0, 127 } */
		static final int[] R_INT_BYT_POS   = { 0, Byte.MAX_VALUE };
		
		// Support for dynamically generated config categories.
		final String KEY;
		
		PropertyCategory( String key )
		{
			KEY = key;
			Config.configLoading.addCustomCategoryComment( name( ), comment( ) );
		}
		
		PropertyCategory( )
		{
			this( null );
		}
		
		abstract
		String name( );
		
		abstract
		String comment( );
		
		double[] defaultDblRange( )
		{
			return PropertyCategory.R_DBL_POS;
		}
		
		float[] defaultFltRange( )
		{
			return PropertyCategory.R_FLT_POS;
		}
		
		int[] defaultIntRange( )
		{
			return PropertyCategory.R_INT_POS0;
		}
		
		IBlockState prop( String key, IBlockState defaultValue, String comment )
		{
			String   target = cprop( key, defaultValue, comment ).getString( );
			String[] pair   = target.split( " ", 2 );
			
			IBlockState block = TargetBlock.getStringAsBlock( pair[ 0 ] );
			if( pair.length > 1 ) {
				//noinspection deprecation Block#getStateFromMeta(int) will be removed in the future. Ignore this for now.
				return block.getBlock( ).getStateFromMeta( Integer.parseInt( pair[ 1 ].trim( ) ) );
			}
			return block;
		}
		
		Property cprop( String key, IBlockState defaultValue, String comment )
		{
			String defaultId = Block.REGISTRY.getNameForObject( defaultValue.getBlock( ) ).toString( )
			                   + " " + defaultValue.getBlock( ).getMetaFromState( defaultValue );
			comment = amendComment( comment, "Block", defaultId, "mod_id:block_id, mod_id:block_id meta" );
			return Config.configLoading.get( name( ), key, defaultId, comment );
		}
		
		HashSet< TargetBlock > prop( String key, Block[] defaultValues, String comment )
		{
			TargetBlock[] wrappedDefaultValues = new TargetBlock[ defaultValues.length ];
			for( int i = 0; i < wrappedDefaultValues.length; i++ ) {
				wrappedDefaultValues[ i ] = new TargetBlock( defaultValues[ i ] );
			}
			return prop( key, wrappedDefaultValues, comment );
		}
		
		HashSet< TargetBlock > prop( String key, TargetBlock[] defaultValues, String comment )
		{
			return TargetBlock.newBlockSet( cprop( key, defaultValues, comment ).getStringList( ) );
		}
		
		Property cprop( String key, TargetBlock[] defaultValues, String comment )
		{
			String[] defaultIds = new String[ defaultValues.length ];
			for( int i = 0; i < defaultIds.length; i++ ) {
				defaultIds[ i ] = Block.REGISTRY.getNameForObject( defaultValues[ i ].BLOCK ).toString( );
				if( defaultValues[ i ].BLOCK_DATA >= 0 ) {
					defaultIds[ i ] = defaultIds[ i ] + " " + defaultValues[ i ].BLOCK_DATA;
				}
			}
			comment = amendComment( comment, "Block_Array", defaultIds, "mod_id:block_id, mod_id:block_id meta, mod_id:*" );
			return Config.configLoading.get( name( ), key, defaultIds, comment );
		}
		
		EntityListConfig prop( String key, EntryEntity[] defaultValues, String comment )
		{
			return new EntityListConfig( cprop( key, defaultValues, comment ).getStringList( ) );
		}
		
		Property cprop( String key, EntryEntity[] defaultValues, String comment )
		{
			String[] defaultIds = new String[ defaultValues.length ];
			for( int i = 0; i < defaultIds.length; i++ ) {
				defaultIds[ i ] = defaultValues[ i ].toString( );
			}
			comment = amendComment( comment, "Entity_Array", defaultIds, "entity_id <extra_data>, ~entity_id <extra_data>" );
			return Config.configLoading.get( name( ), key, defaultIds, comment );
		}
		
		EnvironmentListConfig prop( String key, TargetEnvironment[] defaultValues, String comment )
		{
			return new EnvironmentListConfig( cprop( key, defaultValues, comment ).getStringList( ) );
		}
		
		Property cprop( String key, TargetEnvironment[] defaultValues, String comment )
		{
			String[] defaultIds = new String[ defaultValues.length ];
			for( int i = 0; i < defaultIds.length; i++ ) {
				defaultIds[ i ] = defaultValues[ i ].toString( );
			}
			comment = amendComment( comment, "Environment_Array", defaultIds, "biome/mod_id:biome_id=value, biome/mod_id:prefix*=value, dimension/dimension_id=value" );
			return Config.configLoading.get( name( ), key, defaultIds, comment );
		}
		
		boolean prop( String key, boolean defaultValue, String comment )
		{
			return cprop( key, defaultValue, comment ).getBoolean( );
		}
		
		Property cprop( String key, boolean defaultValue, String comment )
		{
			comment = amendComment( comment, "Boolean", defaultValue, new Object[] { true, false } );
			return Config.configLoading.get( name( ), key, defaultValue, comment );
		}
		
		boolean[] prop( String key, boolean[] defaultValues, String comment )
		{
			return cprop( key, defaultValues, comment ).getBooleanList( );
		}
		
		Property cprop( String key, boolean[] defaultValues, String comment )
		{
			comment = amendComment( comment, "Boolean_Array", ArrayUtils.toObject( defaultValues ), new Object[] { true, false } );
			return Config.configLoading.get( name( ), key, defaultValues, comment );
		}
		
		int prop( String key, int defaultValue, String comment )
		{
			return cprop( key, defaultValue, comment ).getInt( );
		}
		
		int prop( String key, int defaultValue, String comment, int... range )
		{
			return cprop( key, defaultValue, comment, range ).getInt( );
		}
		
		Property cprop( String key, int defaultValue, String comment )
		{
			return cprop( key, defaultValue, comment, defaultIntRange( ) );
		}
		
		Property cprop( String key, int defaultValue, String comment, int... range )
		{
			comment = amendComment( comment, "Integer", defaultValue, range[ 0 ], range[ 1 ] );
			return Config.configLoading.get( name( ), key, defaultValue, comment, range[ 0 ], range[ 1 ] );
		}
		
		int[] prop( String key, int[] defaultValues, String comment )
		{
			return cprop( key, defaultValues, comment ).getIntList( );
		}
		
		int[] prop( String key, int[] defaultValues, String comment, int... range )
		{
			return cprop( key, defaultValues, comment, range ).getIntList( );
		}
		
		Property cprop( String key, int[] defaultValues, String comment )
		{
			return cprop( key, defaultValues, comment, defaultIntRange( ) );
		}
		
		Property cprop( String key, int[] defaultValues, String comment, int... range )
		{
			comment = amendComment( comment, "Integer_Array", ArrayUtils.toObject( defaultValues ), range[ 0 ], range[ 1 ] );
			return Config.configLoading.get( name( ), key, defaultValues, comment, range[ 0 ], range[ 1 ] );
		}
		
		float prop( String key, float defaultValue, String comment )
		{
			return (float) cprop( key, defaultValue, comment ).getDouble( );
		}
		
		float prop( String key, float defaultValue, String comment, float... range )
		{
			return (float) cprop( key, defaultValue, comment, range ).getDouble( );
		}
		
		Property cprop( String key, float defaultValue, String comment )
		{
			return cprop( key, defaultValue, comment, defaultFltRange( ) );
		}
		
		Property cprop( String key, float defaultValue, String comment, float... range )
		{
			comment = amendComment( comment, "Float", defaultValue, range[ 0 ], range[ 1 ] );
			return Config.configLoading.get( name( ), key, prettyFloatToDouble( defaultValue ), comment, prettyFloatToDouble( range[ 0 ] ), prettyFloatToDouble( range[ 1 ] ) );
		}
		
		double prop( String key, double defaultValue, String comment )
		{
			return cprop( key, defaultValue, comment ).getDouble( );
		}
		
		double prop( String key, double defaultValue, String comment, double... range )
		{
			return cprop( key, defaultValue, comment, range ).getDouble( );
		}
		
		Property cprop( String key, double defaultValue, String comment )
		{
			return cprop( key, defaultValue, comment, defaultDblRange( ) );
		}
		
		Property cprop( String key, double defaultValue, String comment, double... range )
		{
			comment = amendComment( comment, "Double", defaultValue, range[ 0 ], range[ 1 ] );
			return Config.configLoading.get( name( ), key, defaultValue, comment, range[ 0 ], range[ 1 ] );
		}
		
		double[] prop( String key, double[] defaultValues, String comment )
		{
			return cprop( key, defaultValues, comment ).getDoubleList( );
		}
		
		double[] prop( String key, double[] defaultValues, String comment, double... range )
		{
			return cprop( key, defaultValues, comment, range ).getDoubleList( );
		}
		
		Property cprop( String key, double[] defaultValues, String comment )
		{
			return cprop( key, defaultValues, comment, defaultDblRange( ) );
		}
		
		Property cprop( String key, double[] defaultValues, String comment, double... range )
		{
			comment = amendComment( comment, "Double_Array", ArrayUtils.toObject( defaultValues ), range[ 0 ], range[ 1 ] );
			return Config.configLoading.get( name( ), key, defaultValues, comment, range[ 0 ], range[ 1 ] );
		}
		
		String prop( String key, String defaultValue, String comment, String valueDescription )
		{
			return cprop( key, defaultValue, comment, valueDescription ).getString( );
		}
		
		String prop( String key, String defaultValue, String comment, String... validValues )
		{
			return cprop( key, defaultValue, comment, validValues ).getString( );
		}
		
		Property cprop( String key, String defaultValue, String comment, String valueDescription )
		{
			comment = amendComment( comment, "String", defaultValue, valueDescription );
			return Config.configLoading.get( name( ), key, defaultValue, comment, new String[ 0 ] );
		}
		
		Property cprop( String key, String defaultValue, String comment, String... validValues )
		{
			comment = amendComment( comment, "String", defaultValue, validValues );
			return Config.configLoading.get( name( ), key, defaultValue, comment, validValues );
		}
		
		private
		String amendComment( String comment, String type, Object[] defaultValues, String description )
		{
			return amendComment( comment, type, "{ " + toReadable( defaultValues ) + " }", description );
		}
		
		private
		String amendComment( String comment, String type, Object[] defaultValues, Object min, Object max )
		{
			return amendComment( comment, type, "{ " + toReadable( defaultValues ) + " }", min, max );
		}
		
		private
		String amendComment( String comment, String type, Object[] defaultValues, Object[] validValues )
		{
			return amendComment( comment, type, "{ " + toReadable( defaultValues ) + " }", validValues );
		}
		
		private
		String amendComment( String comment, String type, Object defaultValue, String description )
		{
			return comment + "\n   >> " + type + ":[ " + "Value={ " + description + " }, Default=" + defaultValue + " ]";
		}
		
		private
		String amendComment( String comment, String type, Object defaultValue, Object min, Object max )
		{
			return comment + "\n   >> " + type + ":[ " + "Range={ " + min + ", " + max + " }, Default=" + defaultValue + " ]";
		}
		
		private
		String amendComment( String comment, String type, Object defaultValue, Object[] validValues )
		{
			if( validValues.length < 2 )
				throw new IllegalArgumentException( "Attempted to create config with no options!" );
			
			return comment + "\n   >> " + type + ":[ " + "Valid_Values={ " + toReadable( validValues ) + " }, Default=" + defaultValue + " ]";
		}
		
		private
		double prettyFloatToDouble( float f )
		{
			return Double.parseDouble( Float.toString( f ) );
		}
		
		private
		String toReadable( Object[] array )
		{
			if( array.length <= 0 )
				return "";
			
			StringBuilder commentBuilder = new StringBuilder( );
			for( Object value : array ) {
				commentBuilder.append( value ).append( ", " );
			}
			return commentBuilder.substring( 0, commentBuilder.length( ) - 2 );
		}
	}
}
