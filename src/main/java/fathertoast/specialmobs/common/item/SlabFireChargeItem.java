package fathertoast.specialmobs.common.item;

import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class SlabFireChargeItem extends FireChargeItem {

    public SlabFireChargeItem() {
        super( new Item.Properties() );
    }

    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Slab Fire Charge",
                "", "", "", "", "", "" );//TODO
    }

    @Override
    public Rarity getRarity( ItemStack itemStack ) {
        return Rarity.UNCOMMON;
    }
}
