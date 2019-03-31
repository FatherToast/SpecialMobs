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
class EntitySniperWitherSkeleton extends Entity_SpecialWitherSkeleton
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x486720 );
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "sniper" ) ),
		null,
		new ResourceLocation( GET_TEXTURE_PATH( "sniper_overlay" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addGuaranteedDrop( "base", "Arrows", Items.ARROW, 1 );
		loot.addClusterDrop( "uncommon", "Spectral arrows", Items.SPECTRAL_ARROW, 4 );
	}
	
	public
	EntitySniperWitherSkeleton( World world ) { super( world ); }
	
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
		experienceValue += 1;
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.FOLLOW_RANGE, 16.0F );
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ATTACK_DAMAGE, 2.0F );
	}
	
	@Override
	protected
	void initTypeAI( )
	{
		getSpecialData( ).rangedAttackDamage += 2.0F;
		getSpecialData( ).rangedAttackSpread *= 0.05F;
		setRangedAI( 0.3, 30, 25.0F );
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
