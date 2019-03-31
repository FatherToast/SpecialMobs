package fathertoast.specialmobs.entity.ghast;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public
class EntityQueenGhast extends Entity_SpecialGhast
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xce0aff );
		return info;
	}
	
	private static final String TAG_BABY_SPAWN_COUNT = "BabySpawnCount";
	private static final String TAG_BABY_DEATH_SPAWN = "BabiesOnDeath";
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "queen" ) ),
		null,
		new ResourceLocation( GET_TEXTURE_PATH( "queen_shooting" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addSemicommonDrop( "semicommon", "Gold ingot", Items.GOLD_INGOT );
		
		ResourceLocation name     = EntityList.getKey( EntityGhast.class );
		ItemStack        spawnEgg = new ItemStack( Items.SPAWN_EGG );
		ItemMonsterPlacer.applyEntityIdToItemStack( spawnEgg, name );
		loot.addUncommonDrop( "uncommon", "Spawn egg", spawnEgg );
	}
	
	// The number of babies this ghast can spawn.
	private int babySpawnCount;
	// The number of babies spawned on death.
	private int babyDeathSpawnCount;
	
	public
	EntityQueenGhast( World world )
	{
		super( world );
		setSize( 5.0F, 5.0F );
		getSpecialData( ).setBaseScale( 1.25F );
		
		getSpecialData( ).setRegenerationTime( 20 );
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
		babySpawnCount = rand.nextInt( 7 ) + 4;
		babyDeathSpawnCount = rand.nextInt( 4 ) + 3;
		
		experienceValue += 2;
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 20.0 );
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ATTACK_DAMAGE, 2.0 );
		getSpecialData( ).multAttribute( SharedMonsterAttributes.MOVEMENT_SPEED, 0.6 );
	}
	
	/**
	 * Attack the specified entity using a ranged attack.
	 */
	@Override
	public
	void attackEntityWithRangedAttack( EntityLivingBase target, float distanceFactor )
	{
		if( babySpawnCount > 0 && rand.nextInt( 3 ) == 0 ) {
			babySpawnCount--;
			
			EntityBabyGhast baby = new EntityBabyGhast( world );
			baby.copyLocationAndAnglesFrom( this );
			baby.setAttackTarget( getAttackTarget( ) );
			baby.onInitialSpawn( world.getDifficultyForLocation( new BlockPos( this ) ), null );
			
			double vX = target.posX - posX;
			double vZ = target.posZ - posZ;
			double vH = Math.sqrt( vX * vX + vZ * vZ );
			baby.motionX = vX / vH * 0.8 + motionX * 0.2;
			baby.motionZ = vZ / vH * 0.8 + motionZ * 0.2;
			baby.onGround = false;
			
			world.spawnEntity( baby );
			playSound( SoundEvents.ENTITY_EGG_THROW, 1.0F, 2.0F / (rand.nextFloat( ) * 0.4F + 0.8F) );
			baby.spawnExplosionParticle( );
		}
		else {
			super.attackEntityWithRangedAttack( target, distanceFactor );
		}
	}
	
	@Override
	public
	int getFireballStrength( )
	{
		return Math.round( super.getFireballStrength( ) * 1.5F );
	}
	
	@Override
	public
	void onDeath( DamageSource cause )
	{
		if( !dead && !world.isRemote ) {
			BlockPos pos = new BlockPos( this );
			
			EntityBabyGhast baby;
			for( int i = 0; i < babyDeathSpawnCount; i++ ) {
				baby = new EntityBabyGhast( world );
				baby.copyLocationAndAnglesFrom( this );
				baby.setAttackTarget( getAttackTarget( ) );
				baby.onInitialSpawn( world.getDifficultyForLocation( pos ), null );
				
				baby.motionX = (rand.nextDouble( ) - 0.5) * 0.3;
				baby.motionZ = (rand.nextDouble( ) - 0.5) * 0.3;
				world.spawnEntity( baby );
			}
			spawnExplosionParticle( );
			playSound( SoundEvents.ENTITY_BLAZE_SHOOT, 1.0F, 2.0F / (rand.nextFloat( ) * 0.4F + 0.8F) );
		}
		super.onDeath( cause );
	}
	
	// Saves this entity to NBT.
	@Override
	public
	void writeEntityToNBT( NBTTagCompound tag )
	{
		super.writeEntityToNBT( tag );
		NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
		
		saveTag.setInteger( TAG_BABY_SPAWN_COUNT, babySpawnCount );
		saveTag.setInteger( TAG_BABY_DEATH_SPAWN, babyDeathSpawnCount );
	}
	
	// Reads this entity from NBT.
	@Override
	public
	void readEntityFromNBT( NBTTagCompound tag )
	{
		super.readEntityFromNBT( tag );
		NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
		
		if( saveTag.hasKey( TAG_BABY_SPAWN_COUNT, SpecialMobData.NBT_TYPE_PRIMITIVE ) ) {
			babySpawnCount = saveTag.getInteger( TAG_BABY_SPAWN_COUNT );
		}
		if( saveTag.hasKey( TAG_BABY_DEATH_SPAWN, SpecialMobData.NBT_TYPE_PRIMITIVE ) ) {
			babyDeathSpawnCount = saveTag.getInteger( TAG_BABY_DEATH_SPAWN );
		}
	}
}
