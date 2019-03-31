package fathertoast.specialmobs.entity.witch;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.entity.spider.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public
class EntityWildsWitch extends Entity_SpecialWitch
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xa80e0e );
		info.weightExceptions = BestiaryInfo.DEFAULT_THEME_FOREST;
		return info;
	}
	
	private static final String TAG_SPIDER_MOUNTS = "SpiderMountCount";
	private static final String TAG_SWARM_COUNT   = "SpiderSwarmCount";
	private static final String TAG_SWARM_SIZE    = "SpiderSwarmSize";
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "wilds" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		
		ResourceLocation name     = EntityList.getKey( EntitySpider.class );
		ItemStack        spawnEgg = new ItemStack( Items.SPAWN_EGG );
		ItemMonsterPlacer.applyEntityIdToItemStack( spawnEgg, name );
		loot.addUncommonDrop( "uncommon", "Spawn egg", spawnEgg );
	}
	
	// The number of spider mounts this witch can spawn.
	private int spiderMounts;
	// The number of spider swarm attacks this witch can cast.
	private int spiderSwarms;
	// The number of baby spiders to spawn in each spider swarm attack.
	private int spiderSwarmSize;
	
	public
	EntityWildsWitch( World world )
	{
		super( world );
		getSpecialData( ).setImmuneToWebs( true );
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
		spiderMounts = 1 + rand.nextInt( 3 );
		spiderSwarms = 2 + rand.nextInt( 3 );
		
		experienceValue += 1;
		
		getSpecialData( ).multAttribute( SharedMonsterAttributes.MOVEMENT_SPEED, 0.7 );
	}
	
	// Called every tick while this entity is alive.
	@Override
	public
	void onLivingUpdate( )
	{
		if( getRidingEntity( ) instanceof EntityLiving && getAttackTarget( ) != null && rand.nextInt( 10 ) == 0 ) {
			((EntityLiving) getRidingEntity( )).setAttackTarget( getAttackTarget( ) );
		}
		super.onLivingUpdate( );
	}
	
	// Overridden to modify potion attacks. Return ItemStack.EMPTY to cancel the potion throw.
	protected
	ItemStack pickThrownPotionByType( ItemStack potion, EntityLivingBase target, float distanceFactor, float distance )
	{
		if( spiderSwarms > 0 && rand.nextInt( 4 ) == 0 ) {
			spiderSwarms--;
			
			BlockPos pos = new BlockPos( this );
			
			EntityBabySpider baby;
			for( int i = 0; i < spiderSwarmSize; i++ ) {
				baby = new EntityBabySpider( world );
				baby.copyLocationAndAnglesFrom( this );
				baby.setAttackTarget( getAttackTarget( ) );
				baby.onInitialSpawn( world.getDifficultyForLocation( pos ), null );
				
				baby.motionX = (rand.nextDouble( ) - 0.5) * 0.3;
				baby.motionY = rand.nextDouble( ) * 0.5; // Used to cause floor clip bug; remove if it happens again
				baby.motionZ = (rand.nextDouble( ) - 0.5) * 0.3;
				world.spawnEntity( baby );
			}
			spawnExplosionParticle( );
			playSound( SoundEvents.ENTITY_EGG_THROW, 1.0F, 2.0F / (rand.nextFloat( ) * 0.4F + 0.8F) );
			
			potion = ItemStack.EMPTY;
		}
		else if( !target.isPotionActive( MobEffects.POISON ) ) {
			potion = makeSplashPotion( PotionTypes.STRONG_POISON );
		}
		else {
			// Save the spiders
			PotionType currentType = PotionUtils.getPotionFromItem( potion );
			if( currentType == PotionTypes.HARMING || currentType == PotionTypes.STRONG_HARMING ) {
				potion = makeSplashPotion( PotionTypes.STRONG_POISON );
			}
		}
		
		return potion;
	}
	
	// Called when the witch is looking for a potion to drink.
	@Override
	public
	void tryDrinkPotion( )
	{
		if( potionThrowTimer <= 0 ) {
			EntityLivingBase mount;
			if( getRidingEntity( ) instanceof EntityLivingBase ) {
				mount = (EntityLivingBase) getRidingEntity( );
			}
			else {
				mount = null;
			}
			
			if( mount != null && rand.nextFloat( ) < 0.15F && (mount.isBurning( ) || mount.getLastDamageSource( ) != null && mount.getLastDamageSource( ).isFireDamage( )) && !mount.isPotionActive( MobEffects.FIRE_RESISTANCE ) ) {
				usePotion( makeSplashPotion( PotionTypes.FIRE_RESISTANCE ) );
			}
			else if( rand.nextFloat( ) < 0.15F && (isBurning( ) || getLastDamageSource( ) != null && getLastDamageSource( ).isFireDamage( )) && !isPotionActive( MobEffects.FIRE_RESISTANCE ) ) {
				usePotion( makePotion( PotionTypes.FIRE_RESISTANCE ) );
			}
			else if( mount != null && rand.nextFloat( ) < 0.15F && mount.isInsideOfMaterial( Material.WATER ) && !mount.isPotionActive( MobEffects.WATER_BREATHING ) ) {
				usePotion( makeSplashPotion( PotionTypes.WATER_BREATHING ) );
			}
			else if( rand.nextFloat( ) < 0.15F && isInsideOfMaterial( Material.WATER ) && !isPotionActive( MobEffects.WATER_BREATHING ) ) {
				usePotion( makePotion( PotionTypes.WATER_BREATHING ) );
			}
			else if( mount != null && !mount.isEntityUndead( ) && rand.nextFloat( ) < 0.05F && mount.getHealth( ) < mount.getMaxHealth( ) ) {
				usePotion( makeSplashPotion( PotionTypes.HEALING ) );
			}
			else if( rand.nextFloat( ) < 0.05F && getHealth( ) < getMaxHealth( ) ) {
				usePotion( makePotion( PotionTypes.HEALING ) );
			}
			else if( rand.nextFloat( ) < 0.5F && getAttackTarget( ) != null && !isPotionActive( MobEffects.SPEED ) && getAttackTarget( ).getDistanceSq( this ) > 121.0 ) {
				usePotion( makeSplashPotion( PotionTypes.SWIFTNESS ) );
			}
			else {
				tryDrinkPotionByType( );
			}
		}
	}
	
	// Overridden to add additional potions this witch can drink. Sometimes the main method is overridden instead.
	protected
	void tryDrinkPotionByType( )
	{
		if( spiderMounts > 0 && rand.nextFloat( ) < 0.15F && getRidingEntity( ) == null && getAttackTarget( ) != null && getAttackTarget( ).getDistanceSq( this ) > 100.0 ) {
			Entity_SpecialSpider mount = new Entity_SpecialSpider( world );
			mount.copyLocationAndAnglesFrom( this );
			
			// Make sure the spider doesn't spawn in too small of a space; cancel if it is
			if( world.collidesWithAnyBlock( mount.getEntityBoundingBox( ) ) ) {
				return;
			}
			spiderMounts--;
			potionThrowTimer = 40;
			
			mount.setAttackTarget( getAttackTarget( ) );
			mount.onInitialSpawn( world.getDifficultyForLocation( new BlockPos( this ) ), null );
			world.spawnEntity( mount );
			spawnExplosionParticle( );
			playSound( SoundEvents.ENTITY_BLAZE_SHOOT, 1.0F, 2.0F / (rand.nextFloat( ) * 0.4F + 0.8F) );
			
			startRiding( mount, true );
		}
	}
	
	// Saves this entity to NBT.
	@Override
	public
	void writeEntityToNBT( NBTTagCompound tag )
	{
		super.writeEntityToNBT( tag );
		NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
		
		saveTag.setByte( TAG_SPIDER_MOUNTS, (byte) spiderMounts );
		saveTag.setByte( TAG_SWARM_COUNT, (byte) spiderSwarms );
		saveTag.setByte( TAG_SWARM_SIZE, (byte) spiderSwarmSize );
	}
	
	// Reads this entity from NBT.
	@Override
	public
	void readEntityFromNBT( NBTTagCompound tag )
	{
		super.readEntityFromNBT( tag );
		NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
		
		if( saveTag.hasKey( TAG_SPIDER_MOUNTS, SpecialMobData.NBT_TYPE_PRIMITIVE ) ) {
			spiderMounts = saveTag.getByte( TAG_SPIDER_MOUNTS );
		}
		if( saveTag.hasKey( TAG_SWARM_COUNT, SpecialMobData.NBT_TYPE_PRIMITIVE ) ) {
			spiderSwarms = saveTag.getByte( TAG_SWARM_COUNT );
		}
		if( saveTag.hasKey( TAG_SWARM_SIZE, SpecialMobData.NBT_TYPE_PRIMITIVE ) ) {
			spiderSwarmSize = saveTag.getByte( TAG_SWARM_SIZE );
		}
	}
}
