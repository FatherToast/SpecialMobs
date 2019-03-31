package fathertoast.specialmobs.entity.creeper;

import fathertoast.specialmobs.bestiary.*;
import fathertoast.specialmobs.loot.*;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public
class EntityGravelCreeper extends Entity_SpecialCreeper
{
	@SuppressWarnings( "unused" )
	public static
	BestiaryInfo GET_BESTIARY_INFO( )
	{
		BestiaryInfo info = new BestiaryInfo( 0x908884 );
		return info;
	}
	
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation( GET_TEXTURE_PATH( "gravel" ) )
	};
	
	public static ResourceLocation LOOT_TABLE;
	
	@SuppressWarnings( "unused" )
	public static
	void BUILD_LOOT_TABLE( LootTableBuilder loot )
	{
		ADD_BASE_LOOT( loot );
		loot.addCommonDrop( "common", "Gravel", Blocks.GRAVEL );
		loot.addUncommonDrop( "uncommon", "Flint", Items.FLINT );
	}
	
	public
	EntityGravelCreeper( World world ) { super( world ); }
	
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
	
	// The explosion caused by this creeper.
	@Override
	public
	void explodeByType( boolean powered, boolean griefing )
	{
		float power = (float) explosionRadius / 2.0F * (powered ? 2.0F : 1.0F);
		world.createExplosion( this, posX, posY, posZ, power, griefing );
		
		power += 4.0F;
		int count = (int) Math.ceil( power * power * 3.5F );
		
		EntityFallingBlock gravel;
		float              speed;
		float              pitch, yaw;
		for( int i = 0; i < count; i++ ) {
			gravel = new EntityFallingBlock( world, posX, posY + height / 2.0F, posZ, Blocks.GRAVEL.getDefaultState( ) );
			gravel.fallTime = 1;
			gravel.shouldDropItem = false;
			gravel.setHurtEntities( true );
			gravel.fallDistance = 3.0F;
			
			speed = (power * 0.7F + rand.nextFloat( ) * power) / 20.0F;
			pitch = rand.nextFloat( ) * (float) Math.PI;
			yaw = rand.nextFloat( ) * 2.0F * (float) Math.PI;
			gravel.motionX = MathHelper.cos( yaw ) * speed;
			gravel.motionY = MathHelper.sin( pitch ) * (power + rand.nextFloat( ) * power) / 18.0F;
			gravel.motionZ = MathHelper.sin( yaw ) * speed;
			world.spawnEntity( gravel );
		}
	}
	
	// Called when the entity is attacked.
	@Override
	public
	boolean attackEntityFrom( DamageSource source, float amount )
	{
		if( source != null && (
			DamageSource.FALLING_BLOCK.getDamageType( ).equals( source.getDamageType( ) ) ||
			DamageSource.ANVIL.getDamageType( ).equals( source.getDamageType( ) ))
		) {
			return true;
		}
		return super.attackEntityFrom( source, amount );
	}
}
