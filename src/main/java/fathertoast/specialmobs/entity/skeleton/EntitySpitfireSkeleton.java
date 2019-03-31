package fathertoast.specialmobs.entity.skeleton;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public
class EntitySpitfireSkeleton extends Entity_SpecialSkeleton
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xdc1a00 );
		info.weight = BestiaryInfo.BASE_WEIGHT_UNCOMMON;
		info.weightExceptions = BestiaryInfo.DEFAULT_THEME_FIRE;
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "fire" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addCommonDrop( "common", "Fire charges", Items.FIRE_CHARGE );
	}
	
	public
	EntitySpitfireSkeleton( World world )
	{
		super( world );
		stepHeight = 1.0F;
		getSpecialData( ).setBaseScale( 1.5F );
		
		getSpecialData( ).setImmuneToFire( true );
		getSpecialData( ).setDamagedByWater( true );
	}
	
	@Override
	protected
	void setCollisionSize( float multiplier ) { setSize( 0.9F * multiplier, 2.7F * multiplier ); }
	
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
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ATTACK_DAMAGE, 2.0 );
	}
	
	@Override
	protected
	void initTypeAI( )
	{
		getSpecialData( ).rangedAttackDamage += 2.0F;
		getSpecialData( ).rangedAttackSpread *= 0.5F;
	}
	
	@Override
	protected
	void setEquipmentBasedOnDifficulty( DifficultyInstance difficulty )
	{
		super.setEquipmentBasedOnDifficulty( difficulty );
		
		if( !(getHeldItemMainhand( ).getItem( ) instanceof ItemBow) ) {
			setItemStackToSlot( EntityEquipmentSlot.MAINHAND, new ItemStack( Items.BOW ) );
		}
	}
	
	// Overridden to modify attack effects.
	@Override
	protected
	void onTypeAttack( Entity target )
	{
		target.setFire( 10 );
	}
	
	/** Attack the specified entity using a ranged attack. */
	@Override
	public
	void attackEntityWithRangedAttack( EntityLivingBase target, float distanceFactor )
	{
		world.playEvent( null, 1018, new BlockPos( this ), 0 );
		
		double dX = target.posX - posX;
		double dY = target.getEntityBoundingBox( ).minY + target.height / 2.0F - (posY + height / 2.0F);
		double dZ = target.posZ - posZ;
		
		float accelVariance = MathHelper.sqrt( MathHelper.sqrt( getDistanceSq( target ) ) ) * getSpecialData( ).rangedAttackSpread / 28.0F;
		
		EntitySmallFireball fireball;
		for( int i = 0; i < 3; i++ ) {
			fireball = new EntitySmallFireball(
				world, this,
				dX + getRNG( ).nextGaussian( ) * accelVariance,
				dY,
				dZ + getRNG( ).nextGaussian( ) * accelVariance
			);
			fireball.posY = posY + height / 2.0F + 0.5;
			world.spawnEntity( fireball );
		}
	}
	
	// Called when the entity is attacked.
	@Override
	public
	boolean attackEntityFrom( DamageSource damageSource, float damage )
	{
		if( damageSource.getImmediateSource( ) instanceof EntitySnowball ) {
			damage = Math.max( 2.0F, damage );
		}
		return super.attackEntityFrom( damageSource, damage );
	}
	
	// Returns true if this mob should be rendered on fire.
	@Override
	public
	boolean isBurning( ) { return isEntityAlive( ) && !isWet( ); }
	
	@Override
	public
	boolean isChild( ) { return false; }
	
	@Override
	public
	void setChild( boolean child ) { }
}
