package fathertoast.specialmobs.common.item;

import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class SyringeItem extends Item {

    public SyringeItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).defaultDurability(10).setNoRepair().tab(ItemGroup.TAB_MISC));
    }

    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Syringe",
                "", "", "", "", "", "" );//TODO
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack usedItem = player.getItemInHand(hand);

        if (!world.isClientSide) {
            if (player.getCooldowns().isOnCooldown(usedItem.getItem())) {
                return ActionResult.pass(usedItem);
            }
            else {
                player.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 300, 2));
                player.addEffect(new EffectInstance(Effects.DOLPHINS_GRACE, 300));
                player.addEffect(new EffectInstance(Effects.CONFUSION, 400));
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEE_STING, SoundCategory.PLAYERS, 0.9F, 1.0F);
                usedItem.hurtAndBreak(1, player, (entity) -> {
                    entity.broadcastBreakEvent(hand);
                });
                player.getCooldowns().addCooldown(usedItem.getItem(), 1200);
                return ActionResult.success(usedItem);
            }
        }
        return ActionResult.fail(usedItem);
    }

    // Not enchantable
    @Override
    public boolean isEnchantable(ItemStack itemStack) {
        return false;
    }
}
