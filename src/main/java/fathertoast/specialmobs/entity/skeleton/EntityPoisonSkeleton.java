package fathertoast.specialmobs.entity.skeleton;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public
class EntityPoisonSkeleton extends Entity_SpecialSkeleton
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x779c68 );
		info.weightExceptions = BestiaryInfo.DEFAULT_THEME_FOREST;
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "poison" ) ),
		null,
		new ResourceLocation( GET_TEXTURE_PATH( "poison_overlay" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		ItemStack arrow = new ItemStack( Items.TIPPED_ARROW );
		PotionUtils.addPotionToItemStack( arrow, PotionTypes.POISON );
		loot.addUncommonDrop( "uncommon", "Poison arrow", arrow );
	}
	
	public
	EntityPoisonSkeleton( World world )
	{
		super( world );
		getSpecialData( ).addPotionImmunity( MobEffects.POISON );
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
		experienceValue += 1;
	}
	
	// Overridden to modify attack effects.
	@Override
	protected
	void onTypeAttack( Entity target )
	{
		if( target instanceof EntityLivingBase ) {
			int duration = MobHelper.getDebuffDuration( world.getDifficulty( ) );
			
			((EntityLivingBase) target).addPotionEffect( new PotionEffect( MobEffects.POISON, duration ) );
		}
	}
	
	@Nonnull
	@Override
	protected
	EntityArrow getArrow( float distanceFactor )
	{
		EntityArrow arrow = super.getArrow( distanceFactor );
		
		if( arrow instanceof EntityTippedArrow ) {
			int duration = MobHelper.getDebuffDuration( world.getDifficulty( ) );
			
			((EntityTippedArrow) arrow).addEffect( new PotionEffect( MobEffects.POISON, duration ) );
		}
		
		return arrow;
	}
}
