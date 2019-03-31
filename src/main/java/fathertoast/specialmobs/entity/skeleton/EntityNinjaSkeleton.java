package fathertoast.specialmobs.entity.skeleton;

import com.google.common.base.Optional;
import fathertoast.specialmobs.ai.*;
import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public
class EntityNinjaSkeleton extends Entity_SpecialSkeleton implements INinja
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x333366 );
		return info;
	}
	
	private static final int AI_PRIORITY_NINJA = 0;
	
	private static final DataParameter< Boolean >                 IS_HIDING    = EntityDataManager.createKey( EntityNinjaSkeleton.class, DataSerializers.BOOLEAN );
	private static final DataParameter< Optional< IBlockState > > HIDING_BLOCK = EntityDataManager.createKey( EntityNinjaSkeleton.class, DataSerializers.OPTIONAL_BLOCK_STATE );
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( "textures/entity/skeleton/skeleton.png" ),
		null,
		new ResourceLocation( GET_TEXTURE_PATH( "ninja_overlay" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		
		BlockSilverfish.EnumType[] blockDisguiseTypes = BlockSilverfish.EnumType.values( );
		ItemStack[]                infestedBlocks     = new ItemStack[ blockDisguiseTypes.length ];
		for( int i = 0; i < infestedBlocks.length; i++ ) {
			infestedBlocks[ i ] = new ItemStack( Blocks.MONSTER_EGG, 1, blockDisguiseTypes[ i ].getMetadata( ) );
		}
		loot.addUncommonDrop( "uncommon", "Infested block", infestedBlocks );
	}
	
	private boolean canHide = true;
	
	public
	EntityNinjaSkeleton( World world ) { super( world ); }
	
	protected
	void entityInit( )
	{
		super.entityInit( );
		dataManager.register( IS_HIDING, Boolean.FALSE );
		dataManager.register( HIDING_BLOCK, Optional.absent( ) );
	}
	
	@Override
	protected
	void initEntityAI( )
	{
		tasks.addTask( AI_PRIORITY_NINJA, new EntityAINinja<>( this ) );
		super.initEntityAI( );
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
		
		getSpecialData( ).multAttribute( SharedMonsterAttributes.MOVEMENT_SPEED, 1.2 );
	}
	
	@Override
	protected
	void initTypeAI( )
	{
		setRangedAI( 1.0, 10, 9.0F );
	}
	
	@Override
	protected
	void setEquipmentBasedOnDifficulty( DifficultyInstance difficulty )
	{
		super.setEquipmentBasedOnDifficulty( difficulty );
		
		if( !(getHeldItemMainhand( ).getItem( ) instanceof ItemBow) || rand.nextBoolean( ) ) {
			setItemStackToSlot( EntityEquipmentSlot.MAINHAND, new ItemStack( Items.IRON_SWORD ) );
		}
		setCanPickUpLoot( true );
	}
	
	// INinja implementations below this ================
	
	// Gets whether the ninja should not move.
	@Override
	public
	boolean isInDisguise( ) { return getDataManager( ).get( IS_HIDING ); }
	
	// Sets the ninja as an immovable object.
	@Override
	public
	void setInDisguise( boolean disguised )
	{
		if( disguised != isInDisguise( ) ) {
			getDataManager( ).set( IS_HIDING, disguised );
			if( disguised ) {
				posX = Math.floor( posX ) + 0.5;
				posY = Math.floor( posY );
				posZ = Math.floor( posZ ) + 0.5;
			}
		}
	}
	
	// Gets the block being hidden as, or null if not hiding.
	@Override
	public
	IBlockState getDisguiseBlock( )
	{
		if( isEntityAlive( ) ) {
			return (IBlockState) ((Optional) getDataManager( ).get( HIDING_BLOCK )).orNull( );
		}
		return null;
	}
	
	// Sets the block being hidden as, set to null to cancel hiding.
	@Override
	public
	void setDisguiseBlock( IBlockState block )
	{
		getDataManager( ).set( HIDING_BLOCK, Optional.fromNullable( block ) );
		canHide = false;
		
		// Smoke puff when emerging from disguise
		if( block == null ) {
			spawnExplosionParticle( );
		}
	}
	
	// Called each tick while this entity is alive.
	@Override
	public
	void onLivingUpdate( )
	{
		if( !world.isRemote ) {
			// Only enter hiding when
			if( canHide ) {
				EntityAINinja.startHiding( this );
			}
			else if(
				     onGround && (
					     getAttackTarget( ) == null ||
					     getAttackTarget( ) instanceof EntityPlayer && ((EntityPlayer) getAttackTarget( )).capabilities.isCreativeMode
				     ) &&
				     getDisguiseBlock( ) == null
			) {
				canHide = true;
			}
		}
		
		super.onLivingUpdate( );
	}
	
	// Overridden to modify attack effects.
	@Override
	protected
	void onTypeAttack( Entity target )
	{
		setDisguiseBlock( null );
		if( target instanceof EntityLivingBase ) {
			setAttackTarget( (EntityLivingBase) target );
		}
	}
	
	// Called when the entity is attacked.
	@Override
	public
	boolean attackEntityFrom( DamageSource damageSource, float damage )
	{
		if( super.attackEntityFrom( damageSource, damage ) ) {
			setDisguiseBlock( null );
			if( damageSource.getTrueSource( ) instanceof EntityLivingBase ) {
				setAttackTarget( (EntityLivingBase) damageSource.getTrueSource( ) );
			}
			return true;
		}
		return false;
	}
	
	// Called when the entity is right-clicked by a player.
	@Override
	public
	boolean processInteract( EntityPlayer player, EnumHand hand )
	{
		// Attack if the player tries to right click the "block"
		if( !world.isRemote && getDisguiseBlock( ) != null ) {
			setDisguiseBlock( null );
			setAttackTarget( player );
		}
		return super.processInteract( player, hand );
	}
	
	// Moves this entity.
	@Override
	public
	void move( MoverType type, double x, double y, double z )
	{
		if( isInDisguise( ) && type != MoverType.PISTON ) {
			motionY = 0.0;
		}
		else {
			super.move( type, x, y, z );
		}
	}
	
	// Sets this entity on fire.
	@Override
	public
	void setFire( int time )
	{
		if( !isInDisguise( ) ) {
			super.setFire( time );
		}
	}
	
	// Returns true if this entity should push and be pushed by other entities when colliding.
	@Override
	public
	boolean canBePushed( ) { return !isInDisguise( ); }
}
