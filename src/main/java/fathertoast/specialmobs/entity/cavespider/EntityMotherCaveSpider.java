package fathertoast.specialmobs.entity.cavespider;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityCaveSpider;
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
class EntityMotherCaveSpider extends Entity_SpecialCaveSpider
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xb300b3 );
		return info;
	}
	
	private static final String TAG_BABIES = "Babies";
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "mother" ) ),
		new ResourceLocation( GET_TEXTURE_PATH( "mother_eyes" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		
		ResourceLocation name     = EntityList.getKey( EntityCaveSpider.class );
		ItemStack        spawnEgg = new ItemStack( Items.SPAWN_EGG );
		ItemMonsterPlacer.applyEntityIdToItemStack( spawnEgg, name );
		loot.addUncommonDrop( "uncommon", "Spawn egg", spawnEgg );
	}
	
	// The number of babies spawned on death.
	private int babies;
	
	public
	EntityMotherCaveSpider( World world ) { super( world );}
	
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
		babies = 3 + rand.nextInt( 4 );
		
		experienceValue += 2;
		
		getSpecialData( ).setRegenerationTime( 30 );
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 16.0 );
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ATTACK_DAMAGE, 2.0 );
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ARMOR, 6.0 );
	}
	
	@Override
	public
	void onDeath( DamageSource cause )
	{
		if( !dead && !world.isRemote ) {
			BlockPos pos = new BlockPos( this );
			
			EntityBabyCaveSpider baby;
			for( int i = 0; i < babies; i++ ) {
				baby = new EntityBabyCaveSpider( world );
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
		
		saveTag.setByte( TAG_BABIES, (byte) babies );
	}
	
	// Reads this entity from NBT.
	@Override
	public
	void readEntityFromNBT( NBTTagCompound tag )
	{
		super.readEntityFromNBT( tag );
		NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
		
		if( saveTag.hasKey( TAG_BABIES, SpecialMobData.NBT_TYPE_PRIMITIVE ) ) {
			babies = saveTag.getByte( TAG_BABIES );
		}
	}
}
