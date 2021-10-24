package fathertoast.specialmobs.entity.ghast;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public
class EntityFighterGhast extends Entity_SpecialGhast
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x7a1300 );
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "fighter" ) ),
		null,
		new ResourceLocation( GET_TEXTURE_PATH( "fighter_shooting" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addUncommonDrop( "uncommon", "Iron ingot", Items.IRON_INGOT );
	}
	
	public
	EntityFighterGhast( World world )
	{
		super( world );
		setSize( 2.0F, 2.0F );
		getSpecialData( ).setBaseScale( 0.5F );
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
		experienceValue += 1;
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 20.0 );
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ARMOR, 10.0 );
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ATTACK_DAMAGE, 2.0 );
		getSpecialData( ).multAttribute( SharedMonsterAttributes.MOVEMENT_SPEED, 1.3 );
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
		target.attackEntityFrom(DamageSource.causeMobDamage(this).setDamageBypassesArmor(), 2.0F);
	}
}
