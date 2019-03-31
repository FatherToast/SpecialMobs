package fathertoast.specialmobs.ai;

import fathertoast.specialmobs.entity.projectile.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A mob must implement this interface to shoot EntitySpecialFishHook.
 * This allows get and set methods for the fish hook so that the entity and hook can
 * keep track of each other.
 */
public
interface IAngler
{
	/**
	 * Sets this angler's fish hook.
	 *
	 * @param hook The angler's new fish hook.
	 */
	void setFishHook( EntitySpecialFishHook hook );
	
	/**
	 * Gets this angler's fish hook.
	 *
	 * @return The angler's current fish hook, null if the angler does not have one out.
	 */
	EntitySpecialFishHook getFishHook( );
	
	/**
	 * @return Whether the angler's line is out.
	 */
	boolean isLineOut( );
	
	/**
	 * The item property getter override that allows the fishing rod item animation for any entity implementing IAngler.
	 */
	class ItemFishingRodPropertyGetter implements IItemPropertyGetter
	{
		@SideOnly( Side.CLIENT )
		@Override
		public
		float apply( @Nonnull ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity )
		{
			if( entity == null ) {
				return 0.0F;
			}
			else {
				boolean inMainhand = entity.getHeldItemMainhand( ) == stack;
				boolean inOffhand  = entity.getHeldItemOffhand( ) == stack && !(entity.getHeldItemMainhand( ).getItem( ) instanceof ItemFishingRod);
				
				return (inMainhand || inOffhand) && (
					entity instanceof EntityPlayer && ((EntityPlayer) entity).fishEntity != null ||
					entity instanceof IAngler && ((IAngler) entity).isLineOut( )
				) ? 1.0F : 0.0F;
			}
		}
	}
}
