package fathertoast.specialmobs.common.item;

import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.network.NetworkHelper;
import fathertoast.specialmobs.common.util.EntityUtil;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

public class IncorporealFireChargeItem extends Item {

    public IncorporealFireChargeItem() {
        super(new Item.Properties().stacksTo(1).tab(ItemGroup.TAB_MISC).rarity(Rarity.UNCOMMON));
    }

    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Incorporeal Fire Charge",
                "", "", "", "", "", "" );//TODO
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (world.isClientSide) {
            Entity entity = EntityUtil.getClientMouseOver(player);

            if (entity instanceof LivingEntity) {
                NetworkHelper.spawnIncorporealFireball(player, (LivingEntity) entity);
            }
            world.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
        }
        return ActionResult.sidedSuccess(player.getItemInHand(hand), world.isClientSide);
    }
}
