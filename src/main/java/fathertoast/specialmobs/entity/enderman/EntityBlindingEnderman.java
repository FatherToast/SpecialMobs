package fathertoast.specialmobs.entity.enderman;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public
class EntityBlindingEnderman extends Entity_SpecialEnderman
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xffffff );
		info.weightExceptions = BestiaryInfo.DEFAULT_THEME_FOREST;
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "blinding" ) ),
		new ResourceLocation( GET_TEXTURE_PATH( "blinding_eyes" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addCommonDrop( "common", "Ink sac", new ItemStack( Items.DYE, 1, EnumDyeColor.BLACK.getDyeDamage( ) ), 1 );
	}
	
	public
	EntityBlindingEnderman( World world ) { super( world ); }
	
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
		
		getSpecialData( ).addPotionImmunity( MobEffects.BLINDNESS );
	}
	
	// Called every tick while this entity is alive.
	@Override
	public
	void onLivingUpdate( )
	{
		EntityLivingBase target = getAttackTarget( );
		if( target != null && getDistanceSq( target ) < 144.0 ) {
			target.addPotionEffect( new PotionEffect( MobEffects.BLINDNESS, 50, 0 ) );
			target.removePotionEffect( MobEffects.NIGHT_VISION );
		}
		super.onLivingUpdate( );
	}
}
