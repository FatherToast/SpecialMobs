package fathertoast.specialmobs.entity.blaze;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.Collections;

public
class EntityConflagrationBlaze extends Entity_SpecialBlaze
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xfff87e );
		return info;
	}
	
	private static final String TAG_FEEDING_LEVEL = "FeedLevel";
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "conflagration" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addCommonDrop( "common", "Fire charges", Items.FIRE_CHARGE );
		
		ItemStack potion = PotionUtils.appendEffects( new ItemStack( Items.POTIONITEM ), Collections.singletonList(
			new PotionEffect( MobEffects.FIRE_RESISTANCE, 160 )
		) );
		loot.addRareDrop( "rare", "Fire resist potion", potion );
	}
	
	// The feeding level of this conflagration blaze.
	private byte feedingLevel = 0;
	
	public
	EntityConflagrationBlaze( World world ) { super( world ); }
	
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
	}
	
	// Called when the entity is attacked.
	@Override
	public
	boolean attackEntityFrom( DamageSource damageSource, float damage )
	{
		if( !damageSource.isFireDamage( ) && !damageSource.isExplosion( ) && !damageSource.isMagicDamage( ) && !DamageSource.DROWN.damageType.equals( damageSource.damageType ) && !(damageSource.getImmediateSource( ) instanceof EntitySnowball) ) {
			damage = Math.min( 1.0F, damage );
			if( !world.isRemote && feedingLevel < 7 ) {
				feedingLevel++;
				
				SpecialMobData data = getSpecialData( );
				data.addAttribute( SharedMonsterAttributes.ATTACK_DAMAGE, 1.0 );
				data.rangedAttackDamage += 0.5F;
				data.rangedAttackCooldown -= 4;
				data.rangedAttackMaxCooldown -= 4;
				if( feedingLevel == 7 ) {
					fireballBurstCount++;
				}
			}
		}
		return super.attackEntityFrom( damageSource, damage );
	}
	
	// Saves this entity to NBT.
	@Override
	public
	void writeEntityToNBT( NBTTagCompound tag )
	{
		super.writeEntityToNBT( tag );
		NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
		
		saveTag.setByte( TAG_FEEDING_LEVEL, feedingLevel );
	}
	
	// Reads this entity from NBT.
	@Override
	public
	void readEntityFromNBT( NBTTagCompound tag )
	{
		super.readEntityFromNBT( tag );
		NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
		
		if( saveTag.hasKey( TAG_FEEDING_LEVEL, SpecialMobData.NBT_TYPE_PRIMITIVE ) ) {
			feedingLevel = saveTag.getByte( TAG_FEEDING_LEVEL );
		}
	}
}
