package fathertoast.specialmobs.common.event;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.function.Supplier;

public class GameEvents {



    @SubscribeEvent( priority = EventPriority.NORMAL )
    public void onLivingHurt( LivingHurtEvent event ) {

    }

    @SubscribeEvent( priority = EventPriority.NORMAL )
    public void onLivingFall( LivingFallEvent event ) {

    }

    /**
     * Make the relevant vanilla replacements have the same visibility
     * multipliers that their true vanilla counterpart gets from equipped heads/skulls.
     */
    @SubscribeEvent ( priority = EventPriority.HIGHEST )
    public void onLivingVisibility( LivingEvent.LivingVisibilityEvent event ) {
        if ( event.getLookingEntity() == null ) return;

        ItemStack headStack = event.getEntity().getItemBySlot( EquipmentSlot.HEAD );
        EntityType<?> lookingEntity = event.getLookingEntity().getType();

        if ( lookingEntity == MobFamily.CREEPER.vanillaReplacement.entityType.get() && headStack.is( Items.CREEPER_HEAD )
                || lookingEntity == MobFamily.ZOMBIE.vanillaReplacement.entityType.get() && headStack.is( Items.ZOMBIE_HEAD )
                || lookingEntity == MobFamily.SKELETON.vanillaReplacement.entityType.get() && headStack.is( Items.SKELETON_SKULL ) ) {

            event.modifyVisibility(0.5D);
        }
    }
}