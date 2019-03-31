package fathertoast.specialmobs.entity.witch;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public
class EntityUndeadWitch extends Entity_SpecialWitch
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x799c65 );
		return info;
	}
	
	private static final String TAG_SKELETONS = "SkeletonCount";
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "undead" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addLootTable( "common", "Zombie loot", LootTableList.ENTITIES_ZOMBIE );
		
		ResourceLocation name     = EntityList.getKey( EntitySkeleton.class );
		ItemStack        spawnEgg = new ItemStack( Items.SPAWN_EGG );
		ItemMonsterPlacer.applyEntityIdToItemStack( spawnEgg, name );
		loot.addUncommonDrop( "uncommon", "Spawn egg", spawnEgg );
	}
	
	// The number of skeletons this witch can spawn.
	private int skeletonCount;
	
	public
	EntityUndeadWitch( World world ) { super( world ); }
	
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
		skeletonCount = 3 + rand.nextInt( 4 );
		
		experienceValue += 2;
	}
	
	@Override
	public
	EnumCreatureAttribute getCreatureAttribute( )
	{
		return EnumCreatureAttribute.UNDEAD;
	}
	
	// Overridden to modify potion attacks. Return ItemStack.EMPTY to cancel the potion throw.
	protected
	ItemStack pickThrownPotionByType( ItemStack potion, EntityLivingBase target, float distanceFactor, float distance )
	{
		if( skeletonCount > 0 && rand.nextInt( 4 ) == 0 ) {
			skeletonCount--;
			
			EntitySkeleton skeleton = new EntitySkeleton( world );
			skeleton.copyLocationAndAnglesFrom( this );
			skeleton.setAttackTarget( getAttackTarget( ) );
			skeleton.onInitialSpawn( world.getDifficultyForLocation( new BlockPos( this ) ), null );
			skeleton.setItemStackToSlot( EntityEquipmentSlot.MAINHAND, new ItemStack( Items.IRON_SWORD ) );
			skeleton.setItemStackToSlot( EntityEquipmentSlot.HEAD, new ItemStack( Items.CHAINMAIL_HELMET ) );
			
			double vX = target.posX - posX;
			double vZ = target.posZ - posZ;
			double vH = Math.sqrt( vX * vX + vZ * vZ );
			skeleton.motionX = vX / vH * 0.8 + motionX * 0.2;
			skeleton.motionY = 0.4; // Used to cause floor clip bug; remove if it happens again
			skeleton.motionZ = vZ / vH * 0.8 + motionZ * 0.2;
			skeleton.onGround = false;
			
			world.spawnEntity( skeleton );
			playSound( SoundEvents.ENTITY_BLAZE_SHOOT, 1.0F, 2.0F / (rand.nextFloat( ) * 0.4F + 0.8F) );
			skeleton.spawnExplosionParticle( );
			
			potion = ItemStack.EMPTY;
		}
		else {
			// Only throw harming potions - heals self and minions, while probably damaging the target
			potion = makeSplashPotion( rand.nextFloat( ) < 0.2F ? PotionTypes.STRONG_HARMING : PotionTypes.HARMING );
		}
		
		return potion;
	}
	
	// Called when the witch is looking for a potion to drink.
	@Override
	public
	void tryDrinkPotion( )
	{
		if( potionThrowTimer <= 0 ) {
			if( rand.nextFloat( ) < 0.15F && (isBurning( ) || getLastDamageSource( ) != null && getLastDamageSource( ).isFireDamage( )) && !isPotionActive( MobEffects.FIRE_RESISTANCE ) ) {
				usePotion( makePotion( PotionTypes.FIRE_RESISTANCE ) );
			}
			else if( rand.nextFloat( ) < 0.15F && isInsideOfMaterial( Material.WATER ) && !isPotionActive( MobEffects.WATER_BREATHING ) ) {
				usePotion( makePotion( PotionTypes.WATER_BREATHING ) );
			}
			else if( rand.nextFloat( ) < 0.05F && getHealth( ) < getMaxHealth( ) ) {
				usePotion( makePotion( PotionTypes.HARMING ) ); // We need to drink harming instead
			}
			else if( rand.nextFloat( ) < 0.5F && getAttackTarget( ) != null && !isPotionActive( MobEffects.SPEED ) && getAttackTarget( ).getDistanceSq( this ) > 121.0 ) {
				usePotion( makeSplashPotion( PotionTypes.SWIFTNESS ) );
			}
			else {
				tryDrinkPotionByType( );
			}
		}
	}
	
	// Saves this entity to NBT.
	@Override
	public
	void writeEntityToNBT( NBTTagCompound tag )
	{
		super.writeEntityToNBT( tag );
		NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
		
		saveTag.setByte( TAG_SKELETONS, (byte) skeletonCount );
	}
	
	// Reads this entity from NBT.
	@Override
	public
	void readEntityFromNBT( NBTTagCompound tag )
	{
		super.readEntityFromNBT( tag );
		NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
		
		if( saveTag.hasKey( TAG_SKELETONS, SpecialMobData.NBT_TYPE_PRIMITIVE ) ) {
			skeletonCount = saveTag.getByte( TAG_SKELETONS );
		}
	}
}
