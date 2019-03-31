package fathertoast.specialmobs.entity.spider;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public
class EntityWebSpider extends Entity_SpecialSpider
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xe7e7e7 );
		info.weight = BestiaryInfo.BASE_WEIGHT_UNCOMMON;
		info.weightExceptions = BestiaryInfo.DEFAULT_THEME_FOREST;
		return info;
	}
	
	private static final String TAG_WEB_COUNT = "WebCount";
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "web" ) ),
		new ResourceLocation( GET_TEXTURE_PATH( "web_eyes" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addUncommonDrop( "uncommon", "Cobwebs", Blocks.WEB );
	}
	
	// The number of webs this spider can sling.
	private int webCount;
	
	public
	EntityWebSpider( World world ) { super( world ); }
	
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
		webCount = rand.nextInt( 5 ) + 2;
		
		experienceValue += 1;
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 4.0 );
		getSpecialData( ).multAttribute( SharedMonsterAttributes.MOVEMENT_SPEED, 1.2 );
	}
	
	// Overridden to modify attack effects.
	@Override
	public
	void onTypeAttack( Entity target )
	{
		if( !world.isRemote && webCount > 0 && !(target instanceof EntitySpider) && target instanceof EntityLivingBase ) {
			BlockPos pos = new BlockPos( target );
			
			boolean placed = tryPlaceWeb( pos );
			if( !placed && target.height > 1.0F ) {
				tryPlaceWeb( pos.add( 0, 1, 0 ) );
			}
		}
	}
	
	private
	boolean tryPlaceWeb( BlockPos pos )
	{
		IBlockState block = world.getBlockState( pos );
		if( block.getBlock( ).isAir( block, world, pos ) || block.getBlock( ).isReplaceable( world, pos ) ) {
			world.setBlockState( pos, Blocks.WEB.getDefaultState( ), 2 );
			webCount--;
			return true;
		}
		return false;
	}
	
	// Saves this entity to NBT.
	@Override
	public
	void writeEntityToNBT( NBTTagCompound tag )
	{
		super.writeEntityToNBT( tag );
		NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
		
		saveTag.setByte( TAG_WEB_COUNT, (byte) webCount );
	}
	
	// Reads this entity from NBT.
	@Override
	public
	void readEntityFromNBT( NBTTagCompound tag )
	{
		super.readEntityFromNBT( tag );
		NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
		
		if( saveTag.hasKey( TAG_WEB_COUNT, SpecialMobData.NBT_TYPE_PRIMITIVE ) ) {
			webCount = saveTag.getByte( TAG_WEB_COUNT );
		}
	}
}
