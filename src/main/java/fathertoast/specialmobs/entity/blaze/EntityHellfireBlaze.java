package fathertoast.specialmobs.entity.blaze;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public
class EntityHellfireBlaze extends Entity_SpecialBlaze
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xdddddd );
		return info;
	}
	
	private static final String TAG_EXPLOSION_POWER = "ExplosionPower";
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "hellfire" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addCommonDrop( "common", "Gunpowder", Items.GUNPOWDER );
	}
	
	// The base explosion strength of this blaze's fireballs.
	private int explosionPower = 2;
	
	public
	EntityHellfireBlaze( World world ) { super( world ); }
	
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
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 10.0 );
	}
	
	@Override
	protected
	void initTypeAI( )
	{
		getSpecialData( ).rangedAttackSpread = 0.0F;
		setRangedAI( 1, 0, 60, 100, 40.0F );
	}
	
	// Called to attack the target entity with a fireball.
	@Override
	public
	void attackEntityWithRangedAttack( EntityLivingBase target, float distanceFactor )
	{
		world.playEvent( null, 1018, new BlockPos( this ), 0 );
		
		double dX = target.posX - posX;
		double dY = target.getEntityBoundingBox( ).minY + target.height / 2.0F - (posY + height / 2.0F);
		double dZ = target.posZ - posZ;
		
		float accelVariance = MathHelper.sqrt( MathHelper.sqrt( getDistanceSq( target ) ) ) * getSpecialData( ).rangedAttackSpread / 28.0F;
		
		EntityLargeFireball fireball = new EntityLargeFireball(
			world, this,
			dX + rand.nextGaussian( ) * accelVariance,
			dY,
			dZ + rand.nextGaussian( ) * accelVariance
		);
		fireball.explosionPower = explosionPower;
		fireball.posY = posY + height / 2.0F + 0.5;
		world.spawnEntity( fireball );
	}
	
	// Saves this entity to NBT.
	@Override
	public
	void writeEntityToNBT( NBTTagCompound tag )
	{
		super.writeEntityToNBT( tag );
		NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
		
		saveTag.setInteger( TAG_EXPLOSION_POWER, explosionPower );
	}
	
	// Reads this entity from NBT.
	@Override
	public
	void readEntityFromNBT( NBTTagCompound tag )
	{
		super.readEntityFromNBT( tag );
		NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
		
		if( saveTag.hasKey( TAG_EXPLOSION_POWER, SpecialMobData.NBT_TYPE_PRIMITIVE ) ) {
			explosionPower = saveTag.getInteger( TAG_EXPLOSION_POWER );
		}
	}
}
