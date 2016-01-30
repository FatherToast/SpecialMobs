package toast.specialMobs;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.LanguageRegistry;

public class EnchantmentSpecial extends Enchantment
{
    // The enchantment variables for this mod. Each can be null if disabled.
    public static final Enchantment painBow, painSword, plagueBow, plagueSword, poisonBow, poisonSword;
    static {
        int id;
        id = Properties.getInt(Properties.ENCHANTS, "pain_bow");
        painBow = id < 0 ? null : new EnchantmentSpecial(id, "Agony", "painBow", 3, EnumEnchantmentType.bow);
        id = Properties.getInt(Properties.ENCHANTS, "pain_sword");
        painSword = id < 0 ? null : new EnchantmentSpecial(id, "Pain", "painSword", 3, EnumEnchantmentType.weapon);

        id = Properties.getInt(Properties.ENCHANTS, "plague_bow");
        plagueBow = id < 0 ? null : new EnchantmentSpecial(id, "Scourge", "plagueBow", 3, EnumEnchantmentType.bow);
        id = Properties.getInt(Properties.ENCHANTS, "plague_sword");
        plagueSword = id < 0 ? null : new EnchantmentSpecial(id, "Plague", "plagueSword", 3, EnumEnchantmentType.weapon);

        id = Properties.getInt(Properties.ENCHANTS, "poison_bow");
        poisonBow = id < 0 ? null : new EnchantmentSpecial(id, "Venom", "poisonBow", 2, EnumEnchantmentType.bow);
        id = Properties.getInt(Properties.ENCHANTS, "poison_sword");
        poisonSword = id < 0 ? null : new EnchantmentSpecial(id, "Poison Aspect", "poisonSword", 1, EnumEnchantmentType.weapon);
    }

    // Called to initialize the enchantments in this mod.
    public static void init() {
        // Currently, do nothing.
    }

    // This enchantment's max level.
    private final int MAX_LEVEL;

    public EnchantmentSpecial(int id, String name, String loc, int max, EnumEnchantmentType type) {
        super(id, 0, type);
        this.MAX_LEVEL = max;
        this.setName("SpecialMobs." + loc);
        LanguageRegistry.instance().addStringLocalization("enchantment.SpecialMobs." + loc, name);
        Enchantment.addToBookList(this);
    }

    // Returns the maximum level that the enchantment can have.
    @Override
    public int getMaxLevel() {
        return this.MAX_LEVEL;
    }

    // This applies specifically to applying at the enchanting table.
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack itemStack) {
        return false;
    }

    // Is this enchantment allowed to be enchanted on books via Enchantment Table.
    @Override
    public boolean isAllowedOnBooks() {
        return false;
    }
}