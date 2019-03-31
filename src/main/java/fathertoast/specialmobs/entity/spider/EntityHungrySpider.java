package fathertoast.specialmobs.entity.spider;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

public
class EntityHungrySpider extends Entity_SpecialSpider
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x799c65 );
		return info;
	}
	
	private static final String TAG_FEEDING_LEVEL = "FeedLevel";
	private static final String TAG_GAINED_HEALTH = "GrowCount";
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "hungry" ) ),
		new ResourceLocation( GET_TEXTURE_PATH( "hungry_eyes" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addCommonDrop( "common", "Bones", Items.BONE );
		loot.addUncommonDrop( "uncommon", "Food", Items.APPLE, Items.BEETROOT, Items.ROTTEN_FLESH, Items.CHICKEN, Items.RABBIT, Items.COOKIE );
	}
	
	// The feeding level of this hungry spider.
	private int feedingLevel;
	// The amount of times this hungry spider has gained health.
	private int gainedHealth;
	
	public
	EntityHungrySpider( World world )
	{
		super( world );
		setSize( 1.9F, 1.3F );
		getSpecialData( ).setBaseScale( 1.5F );
		
		getSpecialData( ).setRegenerationTime( 40 );
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
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 4.0 );
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ATTACK_DAMAGE, -1.0 );
	}
	
	@Override
	protected
	void initTypeAI( )
	{
		getSpecialData( ).rangedAttackMaxRange = 0.0F;
	}
	
	// Overridden to modify attack effects.
	@Override
	public
	void onTypeAttack( Entity target )
	{
		if( target instanceof EntityPlayer && ForgeEventFactory.getMobGriefingEvent( world, this ) ) {
			ItemStack food = MobHelper.stealRandomFood( (EntityPlayer) target );
			if( !food.isEmpty( ) ) {
				if( gainedHealth < 32 ) {
					gainedHealth++;
					float maxHealth = getMaxHealth( );
					getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 2.0 );
					setHealth( getHealth( ) + getMaxHealth( ) - maxHealth );
				}
				if( feedingLevel < 7 ) {
					feedingLevel++;
					getSpecialData( ).addAttribute( SharedMonsterAttributes.ATTACK_DAMAGE, 1.0 );
				}
				
				heal( Math.max( ((ItemFood) food.getItem( )).getHealAmount( food ), 1.0F ) );
				playSound( SoundEvents.ENTITY_PLAYER_BURP, 0.5F, rand.nextFloat( ) * 0.1F + 0.9F );
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
		
		saveTag.setByte( TAG_FEEDING_LEVEL, (byte) feedingLevel );
		saveTag.setByte( TAG_GAINED_HEALTH, (byte) gainedHealth );
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
		if( saveTag.hasKey( TAG_GAINED_HEALTH, SpecialMobData.NBT_TYPE_PRIMITIVE ) ) {
			gainedHealth = saveTag.getByte( TAG_GAINED_HEALTH );
		}
	}
}
