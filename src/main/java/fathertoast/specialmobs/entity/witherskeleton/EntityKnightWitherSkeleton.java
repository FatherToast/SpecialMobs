package fathertoast.specialmobs.entity.witherskeleton;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public
class EntityKnightWitherSkeleton extends Entity_SpecialWitherSkeleton
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xdddddd );
		return info;
	}
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addCommonDrop( "common", "Iron nuggets", Items.IRON_NUGGET );
	}
	
	public
	EntityKnightWitherSkeleton( World world ) { super( world ); }
	
	@Override
	protected
	ResourceLocation getLootTable( ) { return LOOT_TABLE; }
	
	/** Called to modify the mob's attributes based on the variant. */
	@Override
	protected
	void applyTypeAttributes( )
	{
		experienceValue += 2;
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ATTACK_DAMAGE, 4.0F );
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 10.0F );
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ARMOR, 10.0F );
		getSpecialData( ).multAttribute( SharedMonsterAttributes.MOVEMENT_SPEED, 0.8F );
	}
	
	@Override
	protected
	void initTypeAI( )
	{
		getSpecialData( ).rangedAttackDamage += 4.0F;
	}
	
	@Override
	protected
	void setEquipmentBasedOnDifficulty( DifficultyInstance difficulty )
	{
		super.setEquipmentBasedOnDifficulty( difficulty );
		
		if( rand.nextFloat( ) < 0.95F ) {
			setItemStackToSlot( EntityEquipmentSlot.MAINHAND, new ItemStack( Items.IRON_SWORD ) );
			setItemStackToSlot( EntityEquipmentSlot.OFFHAND, new ItemStack( Items.SHIELD ) );
		}
		else {
			setItemStackToSlot( EntityEquipmentSlot.MAINHAND, new ItemStack( Items.BOW ) );
		}
		setItemStackToSlot( EntityEquipmentSlot.HEAD, new ItemStack( Items.CHAINMAIL_HELMET ) );
		setItemStackToSlot( EntityEquipmentSlot.CHEST, new ItemStack( Items.CHAINMAIL_CHESTPLATE ) );
		setItemStackToSlot( EntityEquipmentSlot.LEGS, new ItemStack( Items.CHAINMAIL_LEGGINGS ) );
		setItemStackToSlot( EntityEquipmentSlot.FEET, new ItemStack( Items.CHAINMAIL_BOOTS ) );
	}
}
