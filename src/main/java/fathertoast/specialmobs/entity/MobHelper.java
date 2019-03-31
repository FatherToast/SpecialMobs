package fathertoast.specialmobs.entity;

import fathertoast.specialmobs.config.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import java.util.Random;

public
class MobHelper
{
	public static final int AI_BIT_NONE     = 0b000000;
	public static final int AI_BIT_MOVEMENT = 0b000001;
	public static final int AI_BIT_FACING   = 0b000010;
	public static final int AI_BIT_SWIMMING = 0b000100;
	
	public
	enum AttributeModOperation
	{
		ADDITION( 0 ), MULTIPLY_BASE( 1 ), MULTIPLY_TOTAL( 2 );
		
		public final int id;
		
		AttributeModOperation( int key ) { id = key; }
	}
	
	// Pool of potions to choose from for plague-type mobs to apply on hit. Duration is a multiplier.
	private static final PotionEffect[] POTIONS_PLAGUE = {
		new PotionEffect( MobEffects.SLOWNESS, 2, 0 ),
		new PotionEffect( MobEffects.MINING_FATIGUE, 2, 1 ),
		new PotionEffect( MobEffects.BLINDNESS, 1, 0 ),
		new PotionEffect( MobEffects.HUNGER, 2, 0 ),
		new PotionEffect( MobEffects.WEAKNESS, 1, 0 ),
		new PotionEffect( MobEffects.POISON, 1, 0 ),
		new PotionEffect( MobEffects.NAUSEA, 2, 0 ) // Keep this option last for easy disable (by config)
	};
	
	// Pool of potions to choose from for witch-spider-type mobs to apply on hit. Duration is a multiplier.
	private static final PotionEffect[] POTIONS_WITCH = {
		new PotionEffect( MobEffects.SLOWNESS, 1, 1 ),
		new PotionEffect( MobEffects.MINING_FATIGUE, 2, 1 ),
		new PotionEffect( MobEffects.RESISTANCE, 1, -3 ),
		new PotionEffect( MobEffects.BLINDNESS, 1, 0 ),
		new PotionEffect( MobEffects.HUNGER, 2, 0 ),
		new PotionEffect( MobEffects.WEAKNESS, 1, 0 ),
		new PotionEffect( MobEffects.WITHER, 1, 0 ),
		new PotionEffect( MobEffects.LEVITATION, 1, 1 ),
		new PotionEffect( MobEffects.UNLUCK, 2, 1 ),
		new PotionEffect( MobEffects.POISON, 1, 0 ) // Keep this option last for easy disable (by cave spiders)
	};
	
	/**
	 * Returns true if the damage source can deal normal damage to vampire-type mobs.
	 *
	 * @param damageSource The damage source to test.
	 *
	 * @return True if the damage is being dealt by a smite-enchanted or wooden weapon.
	 */
	public static
	boolean isDamageSourceIneffectiveAgainstVampires( DamageSource damageSource )
	{
		if( damageSource != null ) {
			if( damageSource.canHarmInCreative( ) ) {
				return false;
			}
			Entity attacker = damageSource.getTrueSource( );
			if( attacker instanceof EntityLivingBase ) {
				ItemStack weapon = ((EntityLivingBase) attacker).getHeldItemMainhand( );
				
				return !Item.ToolMaterial.WOOD.toString( ).equals( getToolMaterialName( weapon ) ) &&
				       EnchantmentHelper.getModifierForCreature( weapon, EnumCreatureAttribute.UNDEAD ) <= 0.0F;
			}
		}
		return true;
	}
	
	// Gets the tool material name used by the item given as by Item.ToolMaterial#toString().
	// Returns an empty string if the item has no discoverable material.
	private static
	String getToolMaterialName( ItemStack item )
	{
		if( !item.isEmpty( ) ) {
			if( item.getItem( ) instanceof ItemSword ) {
				return ((ItemSword) item.getItem( )).getToolMaterialName( );
			}
			else if( item.getItem( ) instanceof ItemTool ) {
				return ((ItemTool) item.getItem( )).getToolMaterialName( );
			}
			else if( item.getItem( ) instanceof ItemHoe ) {
				return ((ItemHoe) item.getItem( )).getMaterialName( );
			}
		}
		return "";
	}
	
	/**
	 * Implementation of EntityLivingBase#attackEntityAsMob( Entity target ) for any mobs that do not inherit it from EntityMob.class.
	 *
	 * @param attacker The mob making the attack.
	 * @param target   The entity being attacked.
	 *
	 * @return True if the attack is successful.
	 */
	public static
	boolean attackEntityAsMob( EntityLivingBase attacker, Entity target )
	{
		float damage    = (float) attacker.getEntityAttribute( SharedMonsterAttributes.ATTACK_DAMAGE ).getAttributeValue( );
		int   knockback = 0;
		
		if( target instanceof EntityLivingBase ) {
			damage += EnchantmentHelper.getModifierForCreature( attacker.getHeldItemMainhand( ), ((EntityLivingBase) target).getCreatureAttribute( ) );
			knockback += EnchantmentHelper.getKnockbackModifier( attacker );
		}
		
		boolean success = target.attackEntityFrom( DamageSource.causeMobDamage( attacker ), damage );
		
		if( success ) {
			if( knockback > 0 ) {
				((EntityLivingBase) target).knockBack( attacker, (float) knockback * 0.5F, (double) MathHelper.sin( attacker.rotationYaw * 0.017453292F ), (double) (-MathHelper.cos( attacker.rotationYaw * 0.017453292F )) );
				attacker.motionX *= 0.6;
				attacker.motionZ *= 0.6;
			}
			
			int fireAspect = EnchantmentHelper.getFireAspectModifier( attacker );
			if( fireAspect > 0 ) {
				target.setFire( fireAspect * 4 );
			}
			
			if( target instanceof EntityPlayer ) {
				ItemStack weapon = attacker.getHeldItemMainhand( );
				
				EntityPlayer entityplayer = (EntityPlayer) target;
				ItemStack    shield       = entityplayer.isHandActive( ) ? entityplayer.getActiveItemStack( ) : ItemStack.EMPTY;
				
				if( !weapon.isEmpty( ) && !shield.isEmpty( ) && weapon.getItem( ).canDisableShield( weapon, shield, entityplayer, attacker ) && shield.getItem( ).isShield( shield, entityplayer ) ) {
					float shieldDisableChance = 0.25F + (float) EnchantmentHelper.getEfficiencyModifier( attacker ) * 0.05F;
					
					if( attacker.getRNG( ).nextFloat( ) < shieldDisableChance ) {
						entityplayer.getCooldownTracker( ).setCooldown( shield.getItem( ), 100 );
						attacker.world.setEntityState( entityplayer, (byte) 30 );
					}
				}
			}
			
			//attacker.applyEnchantments( attacker, target ); // protected method inlined below
			if( target instanceof EntityLivingBase ) {
				EnchantmentHelper.applyThornEnchantments( (EntityLivingBase) target, attacker );
			}
			EnchantmentHelper.applyArthropodEnchantments( attacker, target );
			// end applyEnchantments
		}
		
		return success;
	}
	
	/**
	 * Removes one random food item from the player's inventory and returns it.
	 * Returns an empty stack if there is no food in the player's inventory.
	 *
	 * @param player The player to steal from.
	 *
	 * @return The item removed from the player's inventory.
	 */
	public static
	ItemStack stealRandomFood( EntityPlayer player )
	{
		ItemStack item;
		int       count = 0;
		for( int i = 0; i < player.inventory.getSizeInventory( ); i++ ) {
			item = player.inventory.getStackInSlot( i );
			if( !item.isEmpty( ) && item.getItem( ) instanceof ItemFood ) {
				count++;
			}
		}
		if( count > 0 ) {
			count = player.getRNG( ).nextInt( count );
			for( int i = 0; i < player.inventory.getSizeInventory( ); i++ ) {
				item = player.inventory.getStackInSlot( i );
				if( !item.isEmpty( ) && item.getItem( ) instanceof ItemFood && --count < 0 ) {
					return player.inventory.decrStackSize( i, 1 );
				}
			}
		}
		return ItemStack.EMPTY;
	}
	
	/**
	 * Steals a positive potion effect from a target and applies it to the attacker with a minimum duration of 5 seconds.
	 *
	 * @param attacker The entity stealing an effect.
	 * @param target   The entity to steal an effect from.
	 */
	public static
	void stealPotionEffect( EntityLivingBase attacker, EntityLivingBase target )
	{
		if( !attacker.world.isRemote ) {
			for( PotionEffect potion : target.getActivePotionEffects( ) ) {
				if( potion != null && !potion.getPotion( ).isBadEffect( ) && potion.getAmplifier( ) >= 0 ) {
					
					target.removePotionEffect( potion.getPotion( ) );
					attacker.addPotionEffect( new PotionEffect( potion.getPotion( ), Math.max( potion.getDuration( ), 200 ), potion.getAmplifier( ) ) );
					break;
				}
			}
		}
	}
	
	public static
	int getDebuffDuration( EnumDifficulty difficulty )
	{
		switch( difficulty ) {
			case PEACEFUL:
			case EASY:
				return 60;
			case NORMAL:
				return 140;
			default:
				return 300;
		}
	}
	
	/**
	 * Makes a random potion effect for a plague-type mob to apply on hit.
	 *
	 * @param random The rng to draw from.
	 * @param world  The context.
	 *
	 * @return A newly created potion effect instance we can apply to an entity.
	 */
	public static
	PotionEffect nextPlagueEffect( Random random, World world )
	{
		int duration = MobHelper.getDebuffDuration( world.getDifficulty( ) );
		
		PotionEffect potion = POTIONS_PLAGUE[ random.nextInt( POTIONS_PLAGUE.length - (Config.get( ).GENERAL.DISABLE_NAUSEA ? 1 : 0) ) ];
		return new PotionEffect(
			potion.getPotion( ), duration * potion.getDuration( ), potion.getAmplifier( )
		);
	}
	
	/**
	 * Makes a random potion effect for a witch-spider-type mob to apply on hit,
	 * optionally including poison in the effect pool.
	 * <p>
	 * For example, witch cave spiders do not include poison in the pool because they apply poison already.
	 *
	 * @param random        The rng to draw from.
	 * @param world         The context.
	 * @param includePoison Whether to include poison in the potion pool.
	 *
	 * @return A newly created potion effect instance we can apply to an entity.
	 */
	public static
	PotionEffect nextWitchSpiderEffect( Random random, World world, boolean includePoison )
	{
		int duration = MobHelper.getDebuffDuration( world.getDifficulty( ) );
		
		PotionEffect potion = POTIONS_WITCH[ random.nextInt( POTIONS_WITCH.length - (includePoison ? 0 : 1) ) ];
		return new PotionEffect(
			potion.getPotion( ), duration * potion.getDuration( ), potion.getAmplifier( )
		);
	}
}
