package fathertoast.specialmobs.entity.lavaslime;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.event.ForgeEventFactory;

public
class EntityVolatileLavaSlime extends Entity_SpecialLavaSlime
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x331133 );
		return info;
	}
	
	private static final String TAG_FUSE_TIME = "FuseTime";
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "volatile" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addCommonDrop( "common", "Gunpowder", Items.GUNPOWDER );
	}
	
	private int fuseTime = 0;
	
	public
	EntityVolatileLavaSlime( World world ) { super( world ); }
	
	@Override
	protected
	EntitySlime getSplitSlime( ) { return new EntityVolatileLavaSlime( world ); }
	
	@Override
	public
	ResourceLocation[] getDefaultTextures( ) { return TEXTURES; }
	
	@Override
	protected
	ResourceLocation getLootTable( ) { return isSmallSlime( ) ? LOOT_TABLE : LootTableList.EMPTY; }
	
	/** Called to modify the mob's attributes based on the variant. */
	@Override
	protected
	void applyTypeAttributes( )
	{
		slimeExperienceValue += 2;
	}
	
	/** Called to modify the mob's attributes based on the variant. Health, damage, and speed must be modified here for slimes. */
	@Override
	protected
	void adjustTypeAttributesForSize( int size )
	{
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 2.0 * size );
	}
	
	// Called every tick while this entity is alive.
	@Override
	public
	void onLivingUpdate( )
	{
		if( isEntityAlive( ) ) {
			Entity target = getAttackTarget( );
			if( !world.isRemote && target != null && getDistanceSq( target ) < 9.0F + (getSlimeSize( ) - 1.0F) * 2.0F ) {
				if( fuseTime == 0 ) {
					playSound( SoundEvents.ENTITY_CREEPER_PRIMED, 1.0F, 0.5F );
				}
				else if( fuseTime >= 30 ) {
					boolean griefing = ForgeEventFactory.getMobGriefingEvent( world, this );
					dead = true;
					world.newExplosion( this, posX, posY, posZ, getSlimeSize( ) + 0.5F, true, griefing );
					setDead( );
				}
				fuseTime++;
				getSpecialData( ).setRenderScale( getSpecialData( ).getRenderScale( ) + 0.013F );
				motionX = motionZ = 0.0;
				onGround = false;
			}
			else if( fuseTime > 0 ) {
				fuseTime--;
				getSpecialData( ).setRenderScale( getSpecialData( ).getRenderScale( ) - 0.013F );
				motionX = motionZ = 0.0;
				onGround = false;
			}
		}
		
		super.onLivingUpdate( );
	}
	
	// Saves this entity to NBT.
	@Override
	public
	void writeEntityToNBT( NBTTagCompound tag )
	{
		super.writeEntityToNBT( tag );
		NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
		
		saveTag.setByte( TAG_FUSE_TIME, (byte) fuseTime );
	}
	
	// Reads this entity from NBT.
	@Override
	public
	void readEntityFromNBT( NBTTagCompound tag )
	{
		super.readEntityFromNBT( tag );
		NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
		
		if( saveTag.hasKey( TAG_FUSE_TIME, SpecialMobData.NBT_TYPE_PRIMITIVE ) ) {
			fuseTime = saveTag.getByte( TAG_FUSE_TIME );
		}
	}
}
