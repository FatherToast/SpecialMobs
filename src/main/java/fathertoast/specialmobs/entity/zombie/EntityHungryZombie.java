package fathertoast.specialmobs.entity.zombie;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

public
class EntityHungryZombie extends Entity_SpecialZombie
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xab1518 );
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "hungry" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addCommonDrop( "common", "Bones", Items.BONE );
		loot.addUncommonDrop( "uncommon", "Food", Items.BEEF, Items.CHICKEN, Items.MUTTON, Items.PORKCHOP, Items.RABBIT, Items.COOKIE );
	}
	
	public
	EntityHungryZombie( World world )
	{
		super( world );
		getSpecialData( ).setRegenerationTime( 30 );
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
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 10.0 );
		getSpecialData( ).multAttribute( SharedMonsterAttributes.MOVEMENT_SPEED, 1.3 );
	}
	
	@Override
	protected
	void initTypeAI( )
	{
		disableRangedAI( );
	}
	
	@Override
	protected
	void setEquipmentBasedOnDifficulty( DifficultyInstance difficulty )
	{
		super.setEquipmentBasedOnDifficulty( difficulty );
		
		if( getHeldItemMainhand( ).getItem( ) instanceof ItemBow ) {
			setItemStackToSlot( EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY );
		}
		setCanPickUpLoot( false );
	}
	
	// Overridden to modify attack effects.
	@Override
	public
	void onTypeAttack( Entity target )
	{
		float healAmount = 2.0F;
		if( target instanceof EntityPlayer && ForgeEventFactory.getMobGriefingEvent( world, this ) ) {
			ItemStack food = MobHelper.stealRandomFood( (EntityPlayer) target );
			if( !food.isEmpty( ) ) {
				healAmount += Math.max( ((ItemFood) food.getItem( )).getHealAmount( food ), 0.0F );
				playSound( SoundEvents.ENTITY_PLAYER_BURP, 0.5F, rand.nextFloat( ) * 0.1F + 0.9F );
			}
		}
		else {
			healAmount += 2.0F;
		}
		heal( healAmount );
	}
}
