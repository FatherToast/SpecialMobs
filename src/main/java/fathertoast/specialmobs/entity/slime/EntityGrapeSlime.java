package fathertoast.specialmobs.entity.slime;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public
class EntityGrapeSlime extends Entity_SpecialSlime
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xb333b3 );
		info.weightExceptions = BestiaryInfo.DEFAULT_THEME_MOUNTAIN;
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "grape" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addGuaranteedDrop( "base", "Slime balls", Items.SLIME_BALL, 1 );
		
		loot.addUncommonDrop( "uncommon", "Slime color", new ItemStack( Items.DYE, 1, EnumDyeColor.PURPLE.getDyeDamage( ) ) );
	}
	
	public
	EntityGrapeSlime( World world )
	{
		super( world );
		getSpecialData( ).setFallDamageMultiplier( 0.0F );
	}
	
	@Override
	protected
	EntitySlime getSplitSlime( ) { return new EntityGrapeSlime( world ); }
	
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
		slimeExperienceValue += 1;
	}
	
	/** Called to modify the mob's attributes based on the variant. Health, damage, and speed must be modified here for slimes. */
	@Override
	protected
	void adjustTypeAttributesForSize( int size )
	{
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 4.0 * size );
		getSpecialData( ).multAttribute( SharedMonsterAttributes.MOVEMENT_SPEED, 1.2 );
	}
	
	@Override
	protected
	void jump( )
	{
		EntityLivingBase target = getAttackTarget( );
		if( target != null ) {
			double distanceSq = getDistanceSq( target );
			if( distanceSq > 36.0 && distanceSq < 144.0 ) {
				double vX = target.posX - posX;
				double vZ = target.posZ - posZ;
				double vH = Math.sqrt( vX * vX + vZ * vZ );
				motionX = vX / vH * 1.1 + motionX * 0.2;
				motionY = 0.42 * 2.6;
				motionZ = vZ / vH * 1.1 + motionZ * 0.2;
				isAirBorne = true;
				return;
			}
		}
		super.jump( );
	}
}
