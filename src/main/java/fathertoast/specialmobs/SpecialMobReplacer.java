package fathertoast.specialmobs;

import fathertoast.specialmobs.bestiary.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayDeque;
import java.util.Deque;

public
class SpecialMobReplacer
{
	/**
	 * Used to keep track of vanilla entities that have already been passed over by the mod
	 * for replacement; only used when 'replace vanilla' options are disabled.
	 */
	private static final String TAG_INIT = "SpecialMobsInit";
	
	/**
	 * @param entity The entity to test.
	 *
	 * @return True if this mod's init flag has been set for the entity.
	 */
	private static
	boolean getInitFlag( Entity entity )
	{
		NBTTagCompound forgeData = entity.getEntityData( );
		if( forgeData.hasKey( TAG_INIT, 99 ) ) {
			return forgeData.getBoolean( TAG_INIT );
		}
		return false;
	}
	
	/**
	 * Sets the init flag for an entity, causing the mod to ignore any future loading of the entity into the world.
	 *
	 * @param entity The entity to set this mod's init flag for.
	 */
	private static
	void setInitFlag( Entity entity )
	{
		NBTTagCompound forgeData = entity.getEntityData( );
		forgeData.setBoolean( TAG_INIT, true );
	}
	
	// List of data for mobs needing replacement.
	private final Deque< MobReplacementEntry > toBeReplaced = new ArrayDeque<>( );
	
	/**
	 * Called when any entity is spawned into the world by any means (such as natural/spawner spawns or chunk loading).
	 * <p>
	 * This checks whether the entity belongs to a special mob family and appropriately marks the mob to be replaced
	 * by the tick handler after deciding whether the mob should be spawned as a special variant species.
	 *
	 * @param event The event data.
	 */
	@SubscribeEvent( priority = EventPriority.LOWEST )
	public
	void onEntitySpawn( EntityJoinWorldEvent event )
	{
		if( event.getWorld( ).isRemote ) {
			return;
		}
		
		Entity        entity    = event.getEntity( );
		EnumMobFamily mobFamily = getReplacingMobFamily( entity );
		if( mobFamily != null ) {
			World    world     = event.getWorld( );
			BlockPos entityPos = new BlockPos( entity );
			
			boolean isSpecial = shouldMakeNextSpecial( mobFamily, world, entityPos );
			if( shouldReplace( mobFamily, isSpecial ) ) {
				toBeReplaced.addLast( new MobReplacementEntry( mobFamily, isSpecial, entity, world, entityPos ) );
				
				// Sadly, it's somewhat of a pain to make sure no warnings get logged
				// when dealing with mounts/riders... Maybe someday :(
				event.setCanceled( true );
			}
			else {
				setInitFlag( entity );
			}
		}
	}
	
	/**
	 * Called each server tick.
	 * <p>
	 * Executes all pending mob replacements.
	 *
	 * @param event The event data.
	 */
	@SubscribeEvent( priority = EventPriority.NORMAL )
	public
	void onServerTick( TickEvent.ServerTickEvent event )
	{
		if( event.phase == TickEvent.Phase.END ) {
			while( !toBeReplaced.isEmpty( ) ) {
				MobReplacementEntry replacement = toBeReplaced.removeFirst( );
				replace( replacement.mobFamily, replacement.isSpecial, replacement.entityToReplace, replacement.entityWorld, replacement.entityPos );
			}
		}
	}
	
	private
	EnumMobFamily getReplacingMobFamily( Entity entity )
	{
		EnumMobFamily mobFamily = EnumMobFamily.CLASS_TO_FAMILY_MAP.get( entity.getClass( ) );
		if( mobFamily != null && !mobFamily.config.REPLACE_VANILLA && getInitFlag( entity ) ) {
			return null;
		}
		return mobFamily;
	}
	
	private
	boolean shouldMakeNextSpecial( EnumMobFamily mobFamily, World world, BlockPos entityPos )
	{
		return world.rand.nextFloat( ) < mobFamily.config.getSpecialChance( world, entityPos );
	}
	
	private
	boolean shouldReplace( EnumMobFamily mobFamily, boolean isSpecial )
	{
		return isSpecial || mobFamily.config.REPLACE_VANILLA;
	}
	
	private
	void replace( EnumMobFamily mobFamily, boolean isSpecial, Entity entityToReplace, World world, BlockPos entityPos )
	{
		NBTTagCompound tag = new NBTTagCompound( );
		entityToReplace.writeToNBT( tag );
		
		EnumMobFamily.Species variant = isSpecial ? mobFamily.nextVariant( world, entityPos ) : mobFamily.vanillaReplacement;
		
		EntityLiving replacement;
		try {
			replacement = variant.constructor.newInstance( world );
		}
		catch( Exception ex ) {
			SpecialMobsMod.log( ).error( "Encountered an exception while constructing entity '{}'", SpecialMobsMod.NAMESPACE + variant.unlocalizedName );
			return;
		}
		replacement.readFromNBT( tag );
		replacement.onInitialSpawn( world.getDifficultyForLocation( entityPos ), null );
		
		for( Entity rider : entityToReplace.getPassengers( ) ) {
			rider.startRiding( replacement, true );
		}
		if( entityToReplace.isRiding( ) ) {
			replacement.startRiding( entityToReplace.getRidingEntity( ), true );
		}
		
		world.spawnEntity( replacement );
	}
	
	private static
	class MobReplacementEntry
	{
		final EnumMobFamily mobFamily;
		final boolean       isSpecial;
		
		final EntityLiving entityToReplace;
		final World        entityWorld;
		final BlockPos     entityPos;
		
		MobReplacementEntry( EnumMobFamily family, boolean special, Entity entity, World world, BlockPos pos )
		{
			mobFamily = family;
			isSpecial = special;
			
			entityToReplace = (EntityLiving) entity;
			entityWorld = world;
			entityPos = pos;
		}
	}
}
