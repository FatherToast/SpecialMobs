package fathertoast.specialmobs.entity.zombie;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.entity.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

import javax.annotation.Nonnull;

public
class EntityHuskZombie extends Entity_SpecialZombie
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xe6cc94 );
		info.weight = BestiaryInfo.BASE_WEIGHT_UNCOMMON;
		info.weightExceptions = BestiaryInfo.DEFAULT_THEME_FIRE;
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( "textures/entity/zombie/husk.png" )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		loot.addLootTable( "main", "Base loot", LootTableList.ENTITIES_HUSK );
	}
	
	public
	EntityHuskZombie( World world )
	{
		super( world );
		getSpecialData( ).setBaseScale( 1.0625F );
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
	}
	
	@Override
	protected
	boolean shouldBurnInDay( ) { return false; }
	
	@Override
	protected
	SoundEvent getAmbientSound( ) { return SoundEvents.ENTITY_HUSK_AMBIENT; }
	
	@Override
	protected
	SoundEvent getHurtSound( DamageSource damageSource ) { return SoundEvents.ENTITY_HUSK_HURT; }
	
	@Override
	protected
	SoundEvent getDeathSound( ) { return SoundEvents.ENTITY_HUSK_DEATH; }
	
	@Override
	protected
	SoundEvent getStepSound( ) { return SoundEvents.ENTITY_HUSK_STEP; }
	
	// Overridden to modify attack effects.
	@Override
	protected
	void onTypeAttack( Entity target )
	{
		if( target instanceof EntityLivingBase ) {
			int duration = MobHelper.getDebuffDuration( world.getDifficulty( ) );
			
			((EntityLivingBase) target).addPotionEffect( new PotionEffect( MobEffects.HUNGER, duration ) );
		}
	}
	
	@Nonnull
	@Override
	protected
	EntityArrow getArrow( float distanceFactor )
	{
		EntityArrow arrow = super.getArrow( distanceFactor );
		
		if( arrow instanceof EntityTippedArrow ) {
			int duration = MobHelper.getDebuffDuration( world.getDifficulty( ) );
			
			((EntityTippedArrow) arrow).addEffect( new PotionEffect( MobEffects.HUNGER, duration ) );
		}
		
		return arrow;
	}
}
