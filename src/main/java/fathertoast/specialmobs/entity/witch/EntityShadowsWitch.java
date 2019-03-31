package fathertoast.specialmobs.entity.witch;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Collection;

public
class EntityShadowsWitch extends Entity_SpecialWitch
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x000000 );
		info.weightExceptions = BestiaryInfo.DEFAULT_THEME_FOREST;
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "shadows" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addCommonDrop( "common", "Ink sac", new ItemStack( Items.DYE, 1, EnumDyeColor.BLACK.getDyeDamage( ) ), 1 );
		
		ItemStack potion = PotionUtils.appendEffects( new ItemStack( Items.POTIONITEM ), Arrays.asList(
			new PotionEffect( MobEffects.BLINDNESS, 400 ),
			new PotionEffect( MobEffects.NIGHT_VISION, 300 )
		) );
		loot.addRareDrop( "rare", "Shadows potion", potion );
	}
	
	private static final Collection< PotionEffect > POTION_SHADOWS = Arrays.asList(
		new PotionEffect( MobEffects.BLINDNESS, 300, 0 ),
		new PotionEffect( MobEffects.WITHER, 200, 0 )
	);
	
	public
	EntityShadowsWitch( World world )
	{
		super( world );
		getSpecialData( ).addPotionImmunity( MobEffects.BLINDNESS, MobEffects.WITHER );
	}
	
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
	}
	
	// Overridden to modify potion attacks. Return ItemStack.EMPTY to cancel the potion throw.
	protected
	ItemStack pickThrownPotionByType( ItemStack potion, EntityLivingBase target, float distanceFactor, float distance )
	{
		if( target.getHealth( ) >= 4.0F && !target.isPotionActive( MobEffects.BLINDNESS ) ) {
			potion = makeSplashPotion( POTION_SHADOWS );
		}
		
		return potion;
	}
	
	// Called every tick while this entity is alive.
	@Override
	public
	void onLivingUpdate( )
	{
		EntityLivingBase target = getAttackTarget( );
		if( !world.isRemote && isEntityAlive( ) && target != null && rand.nextInt( 10 ) == 0 && target.isPotionActive( MobEffects.BLINDNESS ) ) {
			target.removePotionEffect( MobEffects.NIGHT_VISION );
		}
		super.onLivingUpdate( );
	}
}
