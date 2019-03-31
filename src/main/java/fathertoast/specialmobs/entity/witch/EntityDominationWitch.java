package fathertoast.specialmobs.entity.witch;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Collections;

public
class EntityDominationWitch extends Entity_SpecialWitch
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0xfff87e );
		info.weight = BestiaryInfo.BASE_WEIGHT_UNCOMMON;
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "domination" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addCommonDrop( "common", "XP bottle", Items.EXPERIENCE_BOTTLE );
	}
	
	private static final Collection< PotionEffect > POTION_LEVITATION = Collections.singletonList( new PotionEffect( MobEffects.LEVITATION, 140, 1 ) );
	
	// Ticks before this witch can use its pull ability.
	private int pullDelay;
	
	public
	EntityDominationWitch( World world ) { super( world ); }
	
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
		
		getSpecialData( ).multAttribute( SharedMonsterAttributes.MOVEMENT_SPEED, 0.8 );
	}
	
	// Overridden to modify potion attacks. Return ItemStack.EMPTY to cancel the potion throw.
	protected
	ItemStack pickThrownPotionByType( ItemStack potion, EntityLivingBase target, float distanceFactor, float distance )
	{
		if( !target.isPotionActive( MobEffects.WEAKNESS ) ) {
			potion = makeSplashPotion( PotionTypes.WEAKNESS );
		}
		else if( distance >= 5.0F && target.onGround && !target.isPotionActive( MobEffects.LEVITATION ) ) {
			potion = makeSplashPotion( POTION_LEVITATION );
		}
		
		return potion;
	}
	
	// Called every tick while this entity is alive.
	@Override
	public
	void onLivingUpdate( )
	{
		EntityLivingBase target = getAttackTarget( );
		if( !world.isRemote && isEntityAlive( ) && pullDelay-- <= 0 && target != null && rand.nextInt( 20 ) == 0 ) {
			
			// Pull the player toward this entity if they are vulnerable
			double distanceSq = target.getDistanceSq( this );
			if( distanceSq > 100.0 && distanceSq < 196.0 &&
			    (target.isPotionActive( MobEffects.WEAKNESS ) || target.isPotionActive( MobEffects.LEVITATION )) &&
			    canEntityBeSeen( target )
			) {
				pullDelay = 100;
				
				Vec3d pullVec = new Vec3d(
					posX - target.posX,
					posY - target.posY,
					posZ - target.posZ
				);
				double distance = pullVec.lengthVector( );
				pullVec = pullVec.scale( 0.32 );
				
				target.motionX = pullVec.x;
				target.motionY = pullVec.y + Math.sqrt( distance ) * 0.1;
				target.motionZ = pullVec.z;
				target.onGround = false;
				
				if( target instanceof EntityPlayerMP ) {
					try {
						((EntityPlayerMP) target).connection.sendPacket( new SPacketEntityVelocity( target ) );
					}
					catch( Exception ex ) {
						ex.printStackTrace( );
					}
				}
			}
		}
		super.onLivingUpdate( );
	}
}
