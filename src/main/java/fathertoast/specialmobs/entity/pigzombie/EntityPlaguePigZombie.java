package fathertoast.specialmobs.entity.pigzombie;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public
class EntityPlaguePigZombie extends Entity_SpecialPigZombie
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x8aa838 );
		info.weightExceptions = BestiaryInfo.DEFAULT_THEME_FOREST;
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "plague" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addUncommonDrop(
			"uncommon", "Gross stuff",
			Items.POISONOUS_POTATO, Items.SPIDER_EYE, Items.FERMENTED_SPIDER_EYE,
			Item.getItemFromBlock( Blocks.RED_MUSHROOM ), Item.getItemFromBlock( Blocks.BROWN_MUSHROOM )
		);
	}
	
	public
	EntityPlaguePigZombie( World world ) { super( world ); }
	
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
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 10.0F );
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ARMOR, 10.0F );
	}
	
	// Overridden to modify attack effects.
	@Override
	protected
	void onTypeAttack( Entity target )
	{
		if( target instanceof EntityLivingBase ) {
			((EntityLivingBase) target).addPotionEffect( MobHelper.nextPlagueEffect( rand, world ) );
		}
	}
	
	@Nonnull
	@Override
	protected
	EntityArrow getArrow( float distanceFactor )
	{
		EntityArrow arrow = super.getArrow( distanceFactor );
		
		if( arrow instanceof EntityTippedArrow ) {
			((EntityTippedArrow) arrow).addEffect( MobHelper.nextPlagueEffect( rand, world ) );
		}
		
		return arrow;
	}
}
