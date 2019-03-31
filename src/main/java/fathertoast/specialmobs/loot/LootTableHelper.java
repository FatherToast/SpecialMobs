package fathertoast.specialmobs.loot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fathertoast.specialmobs.*;
import fathertoast.specialmobs.bestiary.*;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;

@ParametersAreNonnullByDefault
public
class LootTableHelper
{
	/** The Gson format to generate files with. */
	private static final Gson GSON_LOOT_TABLES =
		new GsonBuilder( )
			.registerTypeAdapter( RandomValueRange.class, new RandomValueRange.Serializer( ) )
			.registerTypeAdapter( LootPool.class, new LootPool.Serializer( ) )
			.registerTypeAdapter( LootTable.class, new LootTable.Serializer( ) )
			.registerTypeHierarchyAdapter( LootEntry.class, new LootEntry.Serializer( ) )
			.registerTypeHierarchyAdapter( LootFunction.class, new LootFunctionManager.Serializer( ) )
			.registerTypeHierarchyAdapter( LootCondition.class, new LootConditionManager.Serializer( ) )
			.registerTypeHierarchyAdapter( LootContext.EntityTarget.class, new LootContext.EntityTarget.Serializer( ) )
			.setPrettyPrinting( ).create( );
	
	/**
	 * Generates the base loot tables to be included in this mod's resources.
	 */
	public static
	void generateBaseLootTables( File configDir )
	{
		// Extra safeguards to help prevent running this code outside of the development environment
		File runDir = configDir.getParentFile( );
		if( !"run".equals( runDir.getName( ) ) ) {
			return; // Silently ignore this call
		}
		File projectDir    = runDir.getParentFile( );
		File lootTablesDir = new File( projectDir, "src/main/resources/assets/" + SpecialMobsMod.MOD_ID + "/loot_tables/entities/" );
		if( !lootTablesDir.exists( ) ) {
			return; // Silently ignore this call
		}
		
		SpecialMobsMod.log( ).warn( "Generating base loot tables..." );
		long startTime = System.nanoTime( );
		
		SpecialMobsMod.log( ).warn( "Loot table directory: '{}'", lootTablesDir.getAbsolutePath( ) );
		deleteAllLootTables( lootTablesDir );
		createAllLootTables( lootTablesDir );
		
		long estimatedTime = System.nanoTime( ) - startTime;
		SpecialMobsMod.log( ).warn( "Generated base loot tables in {} ms", estimatedTime / 1.0E6 );
	}
	
	/**
	 * Recursively destroys all files in a directory.
	 */
	private static
	void deleteAllLootTables( File directory )
	{
		File[] files = directory.listFiles( );
		if( files != null ) {
			for( File file : files ) {
				if( file.isDirectory( ) ) {
					deleteAllLootTables( file );
				}
				if( !file.delete( ) ) {
					SpecialMobsMod.log( ).error( "Failed to delete file: '{}'", file.getPath( ) );
				}
			}
		}
	}
	
	/**
	 * Creates all base loot table files.
	 */
	private static
	void createAllLootTables( File lootTablesDir )
	{
		//noinspection ResultOfMethodCallIgnored
		lootTablesDir.mkdirs( );
		for( EnumMobFamily family : EnumMobFamily.values( ) ) {
			File familyDir = new File( lootTablesDir, family.name.toLowerCase( ) + "/" );
			//noinspection ResultOfMethodCallIgnored
			familyDir.mkdirs( );
			
			for( EnumMobFamily.Species variant : family.variants ) {
				File lootTableFile = new File( familyDir, variant.name.toLowerCase( ) + ".json" );
				try {
					LootTableBuilder lootTableBuilder   = new LootTableBuilder( );
					Method           speciesLootBuilder = variant.variantClass.getMethod( "BUILD_LOOT_TABLE", LootTableBuilder.class );
					
					speciesLootBuilder.invoke( null, lootTableBuilder );
					
					//noinspection ResultOfMethodCallIgnored
					lootTableFile.createNewFile( );
					FileWriter out = new FileWriter( lootTableFile );
					GSON_LOOT_TABLES.toJson( lootTableBuilder.toLootTable( ), LootTable.class, out );
					out.close( );
				}
				catch( Exception ex ) {
					SpecialMobsMod.log( ).error( "Failed to generate loot table: '{}'", lootTableFile.getPath( ), ex );
				}
			}
		}
	}
}
