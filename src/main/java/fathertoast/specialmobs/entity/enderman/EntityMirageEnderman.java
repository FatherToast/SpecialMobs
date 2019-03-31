package fathertoast.specialmobs.entity.enderman;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.config.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public
class EntityMirageEnderman extends Entity_SpecialEnderman
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xc2bc84 );
		info.weight = BestiaryInfo.BASE_WEIGHT_UNCOMMON;
		info.weightExceptions = new EnvironmentListConfig(
			new TargetEnvironment.TargetBiomeGroup( "desert", BestiaryInfo.BASE_WEIGHT_COMMON ),
			new TargetEnvironment.TargetBiomeGroup( "savanna", BestiaryInfo.BASE_WEIGHT_COMMON ),
			new TargetEnvironment.TargetBiomeGroup( "mesa", BestiaryInfo.BASE_WEIGHT_COMMON ),
			new TargetEnvironment.TargetBiome( Biomes.BEACH, BestiaryInfo.BASE_WEIGHT_COMMON )
		);
		return info;
	}
	
	private static final String TAG_IS_FAKE = "IsFake";
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "mirage" ) ),
		new ResourceLocation( GET_TEXTURE_PATH( "mirage_eyes" ) )
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
	
	// Whether this mirage enderman is fake.
	public boolean isFake = false;
	
	public
	EntityMirageEnderman( World world ) { super( world ); }
	
	@Override
	public
	ResourceLocation[] getDefaultTextures( ) { return TEXTURES; }
	
	@Override
	protected
	ResourceLocation getLootTable( ) { return isFake ? LootTableList.EMPTY : LOOT_TABLE; }
	
	// Sets this mirage enderman to be fake.
	public
	void setFake( )
	{
		isFake = true;
		getEntityAttribute( SharedMonsterAttributes.ATTACK_DAMAGE ).setBaseValue( 0.0 );
		experienceValue = 0;
	}
	
	/** Called to modify the mob's attributes based on the variant. */
	@Override
	protected
	void applyTypeAttributes( )
	{
		experienceValue += 2;
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 20.0 );
	}
	
	// Overridden to modify attack effects.
	@Override
	public
	void onTypeAttack( Entity target )
	{
		if( !this.isFake ) {
			for( int i = 64; i-- > 0; ) {
				if( teleportRandomly( ) ) {
					break;
				}
			}
		}
	}
	
	// Called every tick while this entity is alive.
	@Override
	public
	void onLivingUpdate( )
	{
		super.onLivingUpdate( );
		if( isFake && ticksExisted > 200 ) {
			setDead( );
		}
	}
	
	// Damages this entity from the damageSource by the given amount. Returns true if this entity is damaged.
	@Override
	public
	boolean attackEntityFrom( DamageSource damageSource, float damage )
	{
		if( isFake ) {
			setHealth( 0.0F );
			return true;
		}
		return super.attackEntityFrom( damageSource, damage );
	}
	
	/**
	 * Teleport the enderman to a random nearby position
	 */
	@Override
	protected
	boolean teleportRandomly( )
	{
		if( isFake ) {
			return false;
		}
		
		double xI = posX;
		double yI = posY;
		double zI = posZ;
		
		if( super.teleportRandomly( ) ) {
			mirage( xI, yI, zI );
			return true;
		}
		return false;
	}
	
	/**
	 * Teleport the enderman to another entity
	 */
	@Override
	protected
	boolean teleportToEntity( Entity target )
	{
		if( isFake ) {
			return false;
		}
		
		double xI = posX;
		double yI = posY;
		double zI = posZ;
		
		if( super.teleportToEntity( target ) ) {
			mirage( xI, yI, zI );
			return true;
		}
		return false;
	}
	
	private
	void mirage( double xI, double yI, double zI )
	{
		if( !isFake && getAttackTarget( ) != null ) {
			EntityMirageEnderman mirage = new EntityMirageEnderman( world );
			mirage.setFake( );
			mirage.copyLocationAndAnglesFrom( this );
			mirage.setAttackTarget( getAttackTarget( ) );
			
			// Return one of the endermen to the initial position (either the real or the fake)
			if( rand.nextInt( 4 ) == 0 ) {
				setPosition( xI, yI, zI );
			}
			else {
				mirage.setPosition( xI, yI, zI );
			}
			
			mirage.setHealth( getHealth( ) );
			world.spawnEntity( mirage );
		}
	}
	
	// Saves this entity to NBT.
	@Override
	public
	void writeEntityToNBT( NBTTagCompound tag )
	{
		super.writeEntityToNBT( tag );
		NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
		
		saveTag.setBoolean( TAG_IS_FAKE, isFake );
	}
	
	// Reads this entity from NBT.
	@Override
	public
	void readEntityFromNBT( NBTTagCompound tag )
	{
		super.readEntityFromNBT( tag );
		NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
		
		if( saveTag.hasKey( TAG_IS_FAKE, SpecialMobData.NBT_TYPE_PRIMITIVE ) ) {
			isFake = saveTag.getBoolean( TAG_IS_FAKE );
		}
	}
}
