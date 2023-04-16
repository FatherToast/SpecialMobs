package fathertoast.specialmobs.client;

import fathertoast.specialmobs.common.entity.ai.IAngler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Item property getter override that allows the fishing rod item animation to function for any entity implementing IAngler.
 */
@SuppressWarnings("deprecation")
public class FishingRodItemPropertyGetter implements ItemPropertyFunction {
    
    @Override
    public float call(@Nonnull ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed ) {
        if( entity == null ) return 0.0F;
        
        boolean inMainHand = entity.getMainHandItem() == stack;
        boolean inOffhand = entity.getOffhandItem() == stack && !(entity.getMainHandItem().getItem() instanceof FishingRodItem);
        
        return (inMainHand || inOffhand) && (
                entity instanceof Player player && player.fishing != null ||
                        entity instanceof IAngler angler && angler.isLineOut() // Line added to vanilla logic
        ) ? 1.0F : 0.0F;
    }
}