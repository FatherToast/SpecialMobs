package fathertoast.specialmobs.client;

import fathertoast.specialmobs.common.entity.ai.IAngler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Item property getter override that allows the fishing rod item animation to function for any entity implementing IAngler.
 */
@OnlyIn( Dist.CLIENT )
public class FishingRodItemPropertyGetter implements IItemPropertyGetter {
    
    @Override
    public float call( @Nonnull ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity ) {
        if( entity == null ) return 0.0F;
        
        boolean inMainHand = entity.getMainHandItem() == stack;
        boolean inOffhand = entity.getOffhandItem() == stack && !(entity.getMainHandItem().getItem() instanceof FishingRodItem);
        
        return (inMainHand || inOffhand) && (
                entity instanceof PlayerEntity && ((PlayerEntity) entity).fishing != null ||
                        entity instanceof IAngler && ((IAngler) entity).isLineOut() // Line added to vanilla logic
        ) ? 1.0F : 0.0F;
    }
}