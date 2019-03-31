package fathertoast.specialmobs.entity.creeper;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public
class EntitySplittingCreeper extends Entity_SpecialCreeper
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x5f9d22 );
		info.weight = BestiaryInfo.BASE_WEIGHT_UNCOMMON;
		return info;
	}
	
	private static final String TAG_BABIES = "ExtraBabies";
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "splitting" ) ),
		new ResourceLocation( GET_TEXTURE_PATH( "splitting_eyes" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		
		ResourceLocation name     = EntityList.getKey( EntityCreeper.class );
		ItemStack        spawnEgg = new ItemStack( Items.SPAWN_EGG );
		ItemMonsterPlacer.applyEntityIdToItemStack( spawnEgg, name );
		loot.addUncommonDrop( "uncommon", "Spawn egg", spawnEgg );
	}
	
	// The number of extra mini creepers spawned on explosion.
	private int extraBabies;
	
	public
	EntitySplittingCreeper( World world ) { super( world ); }
	
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
		extraBabies = rand.nextInt( 4 );
		
		experienceValue += 2;
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 20.0 );
	}
	
	@Override
	protected
	void initTypeAI( )
	{
		setExplodesWhenShot( true );
	}
	
	// The explosion caused by this creeper.
	@Override
	public
	void explodeByType( boolean powered, boolean griefing )
	{
		float power = (float) explosionRadius * (powered ? 2.0F : 1.0F);
		world.createExplosion( this, posX, posY, posZ, power - 1.0F, false );
		
		int babiesToSpawn = extraBabies + (int) (power * power) / 2;
		
		BlockPos          pos = new BlockPos( this );
		EntityMiniCreeper baby;
		for( int i = 0; i < babiesToSpawn; i++ ) {
			baby = new EntityMiniCreeper( world );
			baby.copyLocationAndAnglesFrom( this );
			baby.setAttackTarget( getAttackTarget( ) );
			baby.onInitialSpawn( world.getDifficultyForLocation( pos ), null );
			
			baby.motionX = (rand.nextDouble( ) - 0.5) * power / 3.0;
			baby.motionY = 0.3 + 0.3 * rand.nextDouble( ); // Used to cause floor clip bug; remove if it happens again
			baby.motionZ = (rand.nextDouble( ) - 0.5) * power / 3.0;
			baby.onGround = false;
			
			if( powered && Entity_SpecialCreeper.POWERED != null ) {
				baby.getDataManager( ).set( Entity_SpecialCreeper.POWERED, true );
			}
			world.spawnEntity( baby );
		}
		playSound( SoundEvents.ENTITY_EGG_THROW, 1.0F, 2.0F / (rand.nextFloat( ) * 0.4F + 0.8F) );
	}
	
	// Saves this entity to NBT.
	@Override
	public
	void writeEntityToNBT( NBTTagCompound tag )
	{
		super.writeEntityToNBT( tag );
		NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
		
		saveTag.setByte( TAG_BABIES, (byte) extraBabies );
	}
	
	// Reads this entity from NBT.
	@Override
	public
	void readEntityFromNBT( NBTTagCompound tag )
	{
		super.readEntityFromNBT( tag );
		NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
		
		if( saveTag.hasKey( TAG_BABIES, SpecialMobData.NBT_TYPE_PRIMITIVE ) ) {
			extraBabies = saveTag.getByte( TAG_BABIES );
		}
	}
}
