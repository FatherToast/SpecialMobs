package fathertoast.specialmobs.bestiary;

import fathertoast.specialmobs.config.*;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public
enum EnumMobFamily
{
	CREEPER(
		"Creeper", "creepers", 0x0da70b, new Class[] { EntityCreeper.class },
		"Dark", "Death", "Dirt", "Doom", "Drowning", "Ender", "Fire", "Gravel", "Jumping", "Lightning", "Mini", "Splitting"
	),
	
	ZOMBIE(
		"Zombie", "zombies", 0x00afaf, new Class[] { EntityZombie.class, EntityHusk.class },
		"Brute", "Fire", "Fishing", "Giant", "Hungry", "Husk", "Plague"
	),
	ZOMBIE_PIGMAN(
		"PigZombie", "zombie pigmen", 0xea9393, new Class[] { EntityPigZombie.class },
		"Brute", "Fishing", "Giant", "Hungry", "Knight", "Plague", "Vampire"
	),
	
	SKELETON(
		"Skeleton", "skeletons", 0xc1c1c1, new Class[] { EntitySkeleton.class, EntityStray.class },
		"Brute", "Fire", "Gatling", "Giant", "Knight", "Ninja", "Poison", "Sniper", "Spitfire", "Stray"
	),
	WITHER_SKELETON(
		"WitherSkeleton", "wither skeletons", 0x141414, new Class[] { EntityWitherSkeleton.class },
		"Brute", "Gatling", "Giant", "Knight", "Ninja", "Sniper", "Spitfire"
	),
	
	SLIME(
		"Slime", "slimes", 0x51a03e, new Class[] { EntitySlime.class },
		"Blackberry", "Blueberry", "Caramel", "Grape", "Lemon", "Strawberry", "Watermelon"
	),
	MAGMA_CUBE(
		"LavaSlime", "magma cubes", 0x340000, new Class[] { EntityMagmaCube.class },
		"Flying", "Hardened", "Sticky", "Volatile"
	),
	
	SPIDER(
		"Spider", "spiders", 0x342d27, new Class[] { EntitySpider.class },
		"Baby", "Desert", "Flying", "Giant", "Hungry", "Mother", "Pale", "Poison", "Web", "Witch"
	),
	CAVE_SPIDER(
		"CaveSpider", "cave spiders", 0x0c424e, new Class[] { EntityCaveSpider.class },
		"Baby", "Flying", "Mother", "Web", "Witch"
	),
	
	SILVERFISH(
		"Silverfish", "silverfish", 0x6e6e6e, new Class[] { EntitySilverfish.class },
		"Blinding", "Fishing", "Flying", "Poison", "Tough"
	),
	
	ENDERMAN(
		"Enderman", "endermen", 0x161616, new Class[] { EntityEnderman.class },
		"Blinding", "Icy", "Lightning", "Mini", "Mirage", "Thief"
	),
	
	WITCH(
		"Witch", "witches", 0x340000, new Class[] { EntityWitch.class },
		"Domination", "Shadows", "Undead", "Wilds", "Wind"
	),
	
	GHAST(
		"Ghast", "ghasts", 0xf9f9f9, new Class[] { EntityGhast.class },
		"Baby", "Fighter", "King", "Queen", "Unholy"
	),
	
	BLAZE(
		"Blaze", "blazes", 0xf6b201, new Class[] { EntityBlaze.class },
		"Cinder", "Conflagration", "Ember", "Hellfire", "Inferno", "Jolt", "Wildfire"
	);
	
	public static final Map< Class, EnumMobFamily > CLASS_TO_FAMILY_MAP = new HashMap<>( );
	
	public final String name;
	public final String key;
	
	public final int eggBaseColor;
	
	public final Class[] replaceableClasses;
	
	public final Species   vanillaReplacement;
	public final Species[] variants;
	
	public Config.FamilyConfig config;
	
	EnumMobFamily( String familyName, String familyKey, int eggColor, Class[] replaceable, String... variantNames )
	{
		name = familyName;
		key = familyKey;
		eggBaseColor = eggColor;
		replaceableClasses = replaceable;
		
		String classPathRoot = "fathertoast.specialmobs.entity." + name.toLowerCase( ) + ".Entity";
		vanillaReplacement = new Species( this, classPathRoot );
		variants = makeVariants( classPathRoot, variantNames );
	}
	
	private
	Species[] makeVariants( String classPathRoot, String[] names )
	{
		Species[] familyVariants = new Species[ names.length ];
		for( int i = 0; i < familyVariants.length; i++ ) {
			familyVariants[ i ] = new Species( this, classPathRoot, names[ i ] );
		}
		return familyVariants;
	}
	
	/** Pick a new species from this family, based on the location. */
	public
	EnumMobFamily.Species nextVariant( World world, BlockPos pos )
	{
		// Build weights for the current location
		int   totalWeight    = 0;
		int[] variantWeights = new int[ variants.length ];
		for( int i = 0; i < variants.length; i++ ) {
			int weight = config.getVariantWeight( world, pos, variants[ i ] );
			if( weight > 0 ) {
				totalWeight += weight;
				variantWeights[ i ] = weight;
			}
		}
		
		// Pick one item at random
		if( totalWeight > 0 ) {
			int weight = world.rand.nextInt( totalWeight );
			for( int i = 0; i < variants.length; i++ ) {
				if( variantWeights[ i ] > 0 ) {
					weight -= variantWeights[ i ];
					if( weight < 0 ) {
						return variants[ i ];
					}
				}
			}
		}
		return vanillaReplacement;
	}
	
	public static
	class Species
	{
		public final EnumMobFamily family;
		public final String        name;
		public final String        unlocalizedName;
		
		public final Class< EntityLiving > variantClass;
		public final BestiaryInfo          bestiaryInfo;
		
		public final Constructor< ? extends EntityLiving > constructor;
		
		private
		Species( EnumMobFamily variantFamily, String classPathRoot )
		{
			family = variantFamily;
			name = variantFamily.name;
			unlocalizedName = "Special" + name;
			
			// Get class for vanilla replacement
			try {
				// We use an underscore in the vanilla replacement classname to find it easily in the hierarchy
				//noinspection unchecked
				variantClass = (Class< EntityLiving >) Class.forName( classPathRoot + "_" + unlocalizedName );
				
				bestiaryInfo = (BestiaryInfo) variantClass.getMethod( "GET_BESTIARY_INFO" ).invoke( null );
			}
			catch( ClassNotFoundException ex ) {
				throw new RuntimeException( "Failed to find vanilla replacement class for mob family " + name, ex );
			}
			catch( IllegalAccessException | NoSuchMethodException | InvocationTargetException ex ) {
				throw new RuntimeException( "Vanilla replacement class for " + unlocalizedName + " has no valid 'GET_BESTIARY_INFO( )' method", ex );
			}
			
			constructor = ReflectionHelper.findConstructor( variantClass, World.class );
		}
		
		private
		Species( EnumMobFamily variantFamily, String classPathRoot, String variantName )
		{
			family = variantFamily;
			name = variantName;
			unlocalizedName = name + family.name;
			
			// Get class for special variant
			try {
				//noinspection unchecked
				variantClass = (Class< EntityLiving >) Class.forName( classPathRoot + unlocalizedName );
				
				bestiaryInfo = (BestiaryInfo) variantClass.getMethod( "GET_BESTIARY_INFO" ).invoke( null );
			}
			catch( ClassNotFoundException ex ) {
				throw new RuntimeException( "Failed to find special variant class for mob species " + unlocalizedName, ex );
			}
			catch( IllegalAccessException | NoSuchMethodException | InvocationTargetException ex ) {
				throw new RuntimeException( "Special variant class for " + unlocalizedName + " has no valid 'GET_BESTIARY_INFO( )' method", ex );
			}
			
			constructor = ReflectionHelper.findConstructor( variantClass, World.class );
		}
	}
}
