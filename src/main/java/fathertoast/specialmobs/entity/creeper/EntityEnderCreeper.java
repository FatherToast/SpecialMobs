package fathertoast.specialmobs.entity.creeper;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.config.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;

public
class EntityEnderCreeper extends Entity_SpecialCreeper
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xcc00fa );
		info.weight = BestiaryInfo.BASE_WEIGHT_UNCOMMON;
		info.weightExceptions = new EnvironmentListConfig(
			new TargetEnvironment.TargetDimension( DimensionType.THE_END, BestiaryInfo.BASE_WEIGHT * 4 )
		);
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "ender" ) ),
		new ResourceLocation( GET_TEXTURE_PATH( "ender_eyes" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addLootTable( "common", "Enderman loot", LootTableList.ENTITIES_ENDERMAN );
	}
	
	public
	EntityEnderCreeper( World world )
	{
		super( world );
		getSpecialData( ).setDamagedByWater( true );
	}
	
	@Override
	protected
	void initEntityAI( )
	{
		tasks.addTask( 1, new EntityAISwimming( this ) );
		tasks.addTask( 2, new EntityAICreeperSwell( this ) );
		tasks.addTask( 3, new EntityAIAvoidEntity<>( this, EntityOcelot.class, 6.0F, 1.0, 1.2 ) );
		tasks.addTask( 4, new EntityAIAttackMelee( this, 1.0, false ) );
		tasks.addTask( 5, new EntityAIWanderAvoidWater( this, 0.8 ) );
		tasks.addTask( 6, new EntityAIWatchClosest( this, EntityPlayer.class, 8.0F ) );
		tasks.addTask( 6, new EntityAILookIdle( this ) );
		targetTasks.addTask( 1, new EntityEnderCreeper.EntityAITargetBeholder( this ) );
		targetTasks.addTask( 2, new EntityAIHurtByTarget( this, false ) );
		
		initTypeAI( );
	}
	
	@Override
	public
	ResourceLocation[] getDefaultTextures( ) { return TEXTURES; }
	
	@Override
	protected
	ResourceLocation getLootTable( ) { return LOOT_TABLE; }
	
	/** Called to modify the mob's attributes based on the variant. */
	@Override
	protected
	void applyTypeAttributes( )
	{
		experienceValue += 2;
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 20.0 );
	}
	
	@Override
	protected
	void initTypeAI( )
	{
		setCanNotExplodeWhenWet( true );
	}
	
	@Override
	public
	void onLivingUpdate( )
	{
		if( world.isRemote ) {
			for( int i = 0; i < 2; i++ ) {
				world.spawnParticle(
					EnumParticleTypes.PORTAL,
					posX + (rand.nextDouble( ) - 0.5) * width,
					posY + rand.nextDouble( ) * height - 0.25,
					posZ + (rand.nextDouble( ) - 0.5) * width,
					(rand.nextDouble( ) - 0.5) * 2.0, -rand.nextDouble( ), (rand.nextDouble( ) - 0.5) * 2.0
				);
			}
		}
		
		super.onLivingUpdate( );
	}
	
	@Override
	public
	boolean attackEntityFrom( DamageSource source, float amount )
	{
		if( isEntityInvulnerable( source ) ) {
			return false;
		}
		else if( source instanceof EntityDamageSourceIndirect ) {
			for( int i = 0; i < 64; i++ ) {
				if( teleportRandomly( ) ) {
					return true;
				}
			}
			
			return false;
		}
		else {
			boolean success = super.attackEntityFrom( source, amount );
			
			if( source.isUnblockable( ) && rand.nextInt( 10 ) != 0 ) {
				teleportRandomly( );
			}
			
			return success;
		}
	}
	
	// Used by the enderman-like AI.
	private
	boolean shouldAttackPlayer( EntityPlayer player )
	{
		ItemStack hat = player.inventory.armorInventory.get( 3 );
		
		if( !hat.isEmpty( ) && hat.getItem( ) == Item.getItemFromBlock( Blocks.PUMPKIN ) ) {
			return false;
		}
		else {
			Vec3d playerLookVec = player.getLook( 1.0F ).normalize( );
			Vec3d playerToSelfVec = new Vec3d(
				posX - player.posX,
				getEntityBoundingBox( ).minY + (double) getEyeHeight( ) - (player.posY + (double) player.getEyeHeight( )),
				posZ - player.posZ
			);
			double distance = playerToSelfVec.lengthVector( );
			return playerLookVec.dotProduct( playerToSelfVec.normalize( ) ) > 1.0 - 0.025 / distance && player.canEntityBeSeen( this );
		}
	}
	
	// Used by the enderman-like AI.
	private
	boolean teleportRandomly( )
	{
		double targetX = posX + (rand.nextDouble( ) - 0.5) * 64.0;
		double targetY = posY + (double) (rand.nextInt( 64 ) - 32);
		double targetZ = posZ + (rand.nextDouble( ) - 0.5) * 64.0;
		return teleportTo( targetX, targetY, targetZ );
	}
	
	// Used by the enderman-like AI.
	private
	boolean teleportToEntity( Entity entity )
	{
		Vec3d vec3d = new Vec3d( posX - entity.posX, getEntityBoundingBox( ).minY + (double) (height / 2.0F) - entity.posY + (double) entity.getEyeHeight( ), posZ - entity.posZ );
		vec3d = vec3d.normalize( );
		double targetX = posX + (rand.nextDouble( ) - 0.5) * 8.0 - vec3d.x * 16.0;
		double targetY = posY + (double) (rand.nextInt( 16 ) - 8) - vec3d.y * 16.0;
		double targetZ = posZ + (rand.nextDouble( ) - 0.5) * 8.0 - vec3d.z * 16.0;
		return teleportTo( targetX, targetY, targetZ );
	}
	
	// Used by the enderman-like AI.
	private
	boolean teleportTo( double x, double y, double z )
	{
		EnderTeleportEvent event = new EnderTeleportEvent( this, x, y, z, 0 );
		if( MinecraftForge.EVENT_BUS.post( event ) )
			return false;
		boolean success = attemptTeleport( event.getTargetX( ), event.getTargetY( ), event.getTargetZ( ) );
		
		if( success ) {
			world.playSound( null, prevPosX, prevPosY, prevPosZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, getSoundCategory( ), 1.0F, 1.0F );
			playSound( SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F );
		}
		
		return success;
	}
	
	static
	class EntityAITargetBeholder extends EntityAINearestAttackableTarget< EntityPlayer >
	{
		private final EntityEnderCreeper enderCreeper;
		private       EntityPlayer       player;
		private       int                aggroTime;
		private       int                teleportTime;
		
		EntityAITargetBeholder( EntityEnderCreeper entity )
		{
			super( entity, EntityPlayer.class, false );
			enderCreeper = entity;
		}
		
		/**
		 * Returns whether the EntityAIBase should begin execution.
		 */
		public
		boolean shouldExecute( )
		{
			double targetDistance = getTargetDistance( );
			player = enderCreeper.world.getNearestAttackablePlayer(
				enderCreeper.posX, enderCreeper.posY, enderCreeper.posZ,
				targetDistance, targetDistance, null,
				apply -> apply != null && enderCreeper.shouldAttackPlayer( apply )
			);
			return player != null;
		}
		
		/**
		 * Execute a one shot task or start executing a continuous task
		 */
		public
		void startExecuting( )
		{
			aggroTime = 5;
			teleportTime = 0;
		}
		
		/**
		 * Reset the task's internal state. Called when this task is interrupted by another one
		 */
		public
		void resetTask( )
		{
			player = null;
			super.resetTask( );
		}
		
		/**
		 * Returns whether an in-progress EntityAIBase should continue executing
		 */
		public
		boolean shouldContinueExecuting( )
		{
			if( player != null ) {
				if( !enderCreeper.shouldAttackPlayer( player ) ) {
					return false;
				}
				else {
					enderCreeper.faceEntity( player, 10.0F, 10.0F );
					return true;
				}
			}
			return targetEntity != null && targetEntity.isEntityAlive( ) || super.shouldContinueExecuting( );
		}
		
		/**
		 * Keep ticking a continuous task that has already been started
		 */
		public
		void updateTask( )
		{
			if( player != null ) {
				if( --aggroTime <= 0 ) {
					targetEntity = player;
					player = null;
					super.startExecuting( );
				}
			}
			else {
				if( targetEntity != null ) {
					if( enderCreeper.shouldAttackPlayer( targetEntity ) ) {
						if( targetEntity.getDistanceSq( enderCreeper ) < 16.0 ) {
							enderCreeper.teleportRandomly( );
						}
						
						teleportTime = 0;
					}
					else if( targetEntity.getDistanceSq( enderCreeper ) > 256.0 && teleportTime++ >= 30 && enderCreeper.teleportToEntity( targetEntity ) ) {
						teleportTime = 0;
					}
				}
				
				super.updateTask( );
			}
		}
	}
}
