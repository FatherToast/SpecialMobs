package fathertoast.specialmobs.entity.pigzombie;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public
class EntityVampirePigZombie extends Entity_SpecialPigZombie
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x000000 );
		info.weight = BestiaryInfo.BASE_WEIGHT_UNCOMMON;
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "vampire" ) ),
		null,
		new ResourceLocation( GET_TEXTURE_PATH( "vampire_overlay" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addSemicommonDrop( "semicommon", "Quartz", Items.QUARTZ );
		
		ItemStack stake = new ItemStack( Items.WOODEN_SWORD );
		stake.addEnchantment( Enchantments.SMITE, Enchantments.SMITE.getMaxLevel( ) * 2 );
		loot.addRareDrop( "rare", "Wooden stake", stake );
	}
	
	public
	EntityVampirePigZombie( World world ) { super( world ); }
	
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
		experienceValue += 4;
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 10.0F );
	}
	
	@Override
	protected
	void initTypeAI( )
	{
		disableRangedAI( );
	}
	
	// Overridden to modify attack effects.
	@Override
	protected
	void onTypeAttack( Entity target )
	{
		if( target instanceof EntityLivingBase ) {
			((EntityLivingBase) target).setHealth( ((EntityLivingBase) target).getHealth( ) - 2.0F );
			heal( 2.0F );
		}
	}
	
	@Nonnull
	@Override
	protected
	EntityArrow getArrow( float distanceFactor )
	{
		EntityArrow arrow = super.getArrow( distanceFactor );
		
		if( arrow instanceof EntityTippedArrow ) {
			((EntityTippedArrow) arrow).addEffect( new PotionEffect( MobEffects.INSTANT_DAMAGE, 1, 1 ) );
		}
		
		return arrow;
	}
	
	@Override
	public
	EnumCreatureAttribute getCreatureAttribute( )
	{
		return EnumCreatureAttribute.UNDEAD;
	}
	
	// Called when the entity is attacked.
	@Override
	public
	boolean attackEntityFrom( DamageSource damageSource, float damage )
	{
		if( MobHelper.isDamageSourceIneffectiveAgainstVampires( damageSource ) ) {
			damage = Math.min( 1.0F, damage );
		}
		return super.attackEntityFrom( damageSource, damage );
	}
}
