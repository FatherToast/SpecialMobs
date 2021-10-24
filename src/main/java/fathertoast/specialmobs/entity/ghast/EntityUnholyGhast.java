package fathertoast.specialmobs.entity.ghast;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public
class EntityUnholyGhast extends Entity_SpecialGhast
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x7ac754 );
		info.weight = BestiaryInfo.BASE_WEIGHT_UNCOMMON;
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "unholy" ) ),
		null,
		new ResourceLocation( GET_TEXTURE_PATH( "unholy_shooting" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addCommonDrop( "common", "Bones", Items.BONE );
		loot.addSemicommonDrop( "semicommon", "Quartz", Items.QUARTZ );
	}
	
	public
	EntityUnholyGhast( World world )
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
		experienceValue += 4;
		
		getSpecialData( ).addAttribute( SharedMonsterAttributes.MAX_HEALTH, 10.0 );
		getSpecialData( ).addAttribute( SharedMonsterAttributes.ATTACK_DAMAGE, 2.0 );
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
		target.attackEntityFrom(DamageSource.causeMobDamage(this).setDamageBypassesArmor(), 1.0F);
		heal( 1.0F );
	}
	
	@Override
	public
	EnumCreatureAttribute getCreatureAttribute( )
	{
		return EnumCreatureAttribute.UNDEAD;
	}
	
	// Called when the entity is attacked.
	@Override
	public
	boolean attackEntityFrom( DamageSource damageSource, float damage )
	{
		if( MobHelper.isDamageSourceIneffectiveAgainstVampires( damageSource ) ) {
			damage = Math.min( 1.0F, damage );
		}
		return super.attackEntityFrom( damageSource, damage );
	}
}
