package fathertoast.specialmobs.entity.witherskeleton;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public
class EntityGatlingWitherSkeleton extends Entity_SpecialWitherSkeleton
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xffff0b );
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( "textures/entity/skeleton/wither_skeleton.png" ),
		null,
		new ResourceLocation( GET_TEXTURE_PATH( "gatling_overlay" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addGuaranteedDrop( "base", "Arrows", Items.ARROW, 1 );
		loot.addCommonDrop( "common", "Gunpowder", Items.GUNPOWDER );
	}
	
	public
	EntityGatlingWitherSkeleton( World world ) { super( world ); }
	
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
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 10.0F );
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ATTACK_DAMAGE, 2.0F );
	}
	
	@Override
	protected
	void initTypeAI( )
	{
		getSpecialData( ).rangedAttackSpread *= 2.0F;
		setRangedAI( 0.3, 1 );
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
}
