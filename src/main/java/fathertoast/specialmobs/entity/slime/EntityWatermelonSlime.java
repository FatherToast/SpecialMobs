package fathertoast.specialmobs.entity.slime;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public
class EntityWatermelonSlime extends Entity_SpecialSlime
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xdf7679 );
		info.weight = BestiaryInfo.BASE_WEIGHT_UNCOMMON;
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "watermelon" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addCommonDrop( "common", "Melon slices", Items.MELON );
		loot.addRareDrop( "rare", "Glistening melon", Items.SPECKLED_MELON );
		
		loot.addUncommonDrop(
			"uncommon", "Slime color",
			new ItemStack( Items.DYE, 1, EnumDyeColor.PINK.getDyeDamage( ) ),
			new ItemStack( Items.DYE, 1, EnumDyeColor.LIME.getDyeDamage( ) )
		);
	}
	
	public
	EntityWatermelonSlime( World world )
	{
		super( world );
		getSpecialData( ).setBaseScale( 1.5F );
	}
	
	@Override
	protected
	EntitySlime getSplitSlime( ) { return new EntityWatermelonSlime( world ); }
	
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
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ARMOR, 15.0 );
	}
	
	/** Entity size scale must be modified here for slimes. */
	protected
	float typeSizeMultiplier( ) { return 0.765F; }
	
	/** Called to modify the mob's attributes based on the variant. Health, damage, and speed must be modified here for slimes. */
	@Override
	protected
	void adjustTypeAttributesForSize( int size )
	{
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 2.0 * size + 8.0 );
	}
	
	@Override
	protected
	void jump( )
	{
		EntityLivingBase target = getAttackTarget( );
		if( target != null ) {
			double distanceSq = getDistanceSq( target );
			if( distanceSq < 25.0 ) {
				double vX = target.posX - posX;
				double vZ = target.posZ - posZ;
				double vH = Math.sqrt( vX * vX + vZ * vZ );
				motionX = vX / vH * 1.16 + motionX * 0.2;
				motionY = 0.42;
				motionZ = vZ / vH * 1.16 + motionZ * 0.2;
				isAirBorne = true;
				return;
			}
		}
		super.jump( );
	}
	
	// Overridden to modify attack effects.
	@Override
	protected
	void onTypeAttack( Entity target )
	{
		target.addVelocity(
			-MathHelper.sin( rotationYaw * (float) Math.PI / 180.0F ) * 0.8F,
			0.21,
			MathHelper.cos( rotationYaw * (float) Math.PI / 180.0F ) * 0.8F
		);
		motionX *= -0.2;
		motionZ *= -0.2;
	}
}
