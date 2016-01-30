package toast.specialMobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import net.minecraft.block.BlockColored;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public abstract class EffectHelper
{
    // Applies the potion's effect on the entity. If the potion is already active, its duration is increased up to the given duration and its amplifier is increased by the given amplifier + 1.
    public static void stackEffect(EntityLivingBase entity, Potion potion, int duration, int amplifier) {
        if (entity.isPotionActive(potion)) {
            PotionEffect potionEffect = entity.getActivePotionEffect(potion);
            entity.addPotionEffect(new PotionEffect(potion.id, Math.max(duration, potionEffect.getDuration()), potionEffect.getAmplifier() + amplifier + 1));
        }
        else {
            entity.addPotionEffect(new PotionEffect(potion.id, duration, amplifier));
        }
    }

    // Applies the potion's effect on the entity. If the potion is already active, its duration is increased up to the given duration and its amplifier is increased by the given amplifier + 1 up to the given amplifierMax.
    public static void stackEffect(EntityLivingBase entity, Potion potion, int duration, int amplifier, int amplifierMax) {
        if (amplifierMax < 0) {
            EffectHelper.stackEffect(entity, potion, duration, amplifier);
            return;
        }
        if (entity.isPotionActive(potion)) {
            PotionEffect potionEffect = entity.getActivePotionEffect(potion);
            entity.addPotionEffect(new PotionEffect(potion.id, Math.max(duration, potionEffect.getDuration()), Math.min(amplifierMax, potionEffect.getAmplifier() + amplifier + 1)));
        }
        else if (amplifier >= 0) {
            entity.addPotionEffect(new PotionEffect(potion.id, duration, Math.min(amplifier, amplifierMax)));
        }
    }

    // Applies the effects of the plague enchant on the target.
    public static void plagueEffect(EntityLivingBase target, int level) {
        Potion effect = null;
        int time = 10;
        int amplifier = 0;
        int amplifierMax = 0;
        switch (target.getRNG().nextInt(6)) {
            case 0:
                effect = Potion.moveSlowdown;
                time = 8;
                amplifierMax = 5;
                break;
            case 1:
                effect = Potion.confusion;
                amplifierMax = 2;
                break;
            case 2:
                effect = Potion.blindness;
                break;
            case 3:
                effect = Potion.hunger;
                time = 8;
                amplifierMax = 1;
                break;
            case 4:
                effect = Potion.weakness;
                amplifierMax = 6;
                break;
            case 5:
                effect = Potion.poison;
                time = 6;
                amplifierMax = 1;
                break;
        }
        if (effect != null) {
            EffectHelper.stackEffect(target, effect, time * 20, amplifier, amplifierMax);
        }
        if (level > 1) {
            EffectHelper.plagueEffect(target, level - 1);
        }
    }

    // Sets the itemStack's name and removes italics.
    public static void setItemName(ItemStack itemStack, String name) {
        EffectHelper.setItemName(itemStack, name, 0xb);
    }
    public static void setItemName(ItemStack itemStack, String name, int rarityColor) {
        if (itemStack == null || itemStack.hasDisplayName())
            return;
        itemStack.setStackDisplayName("\u00a7" + Integer.toHexString(rarityColor) + name);
    }

    // Adds a line of text to the item stack's infobox.
    public static void addItemText(ItemStack itemStack, String text) {
        if (itemStack.stackTagCompound == null) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        if (!itemStack.stackTagCompound.hasKey("display")) {
            itemStack.stackTagCompound.setTag("display", new NBTTagCompound());
        }
        NBTTagCompound displayTag = itemStack.stackTagCompound.getCompoundTag("display");
        if (!displayTag.hasKey("Lore")) {
            displayTag.setTag("Lore", new NBTTagList());
        }
        NBTTagString textTag = new NBTTagString(text);
        displayTag.getTagList("Lore", textTag.getId()).appendTag(textTag);
    }

    /// Adds a custom attribute modifier to the item stack.
    public static void addModifier(ItemStack itemStack, String name, IAttribute attribute, double value, int operation) {
        if (itemStack.stackTagCompound == null) {
            itemStack.stackTagCompound = new NBTTagCompound();
        }
        if (!itemStack.stackTagCompound.hasKey("AttributeModifiers")) {
            itemStack.stackTagCompound.setTag("AttributeModifiers", new NBTTagList());
        }
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Name", name);
        tag.setString("AttributeName", attribute.getAttributeUnlocalizedName());
        tag.setDouble("Amount", value);
        tag.setInteger("Operation", operation);
        UUID id = UUID.randomUUID();
        tag.setLong("UUIDMost", id.getMostSignificantBits());
        tag.setLong("UUIDLeast", id.getLeastSignificantBits());
        itemStack.stackTagCompound.getTagList("AttributeModifiers", tag.getId()).appendTag(tag);
    }

    /// Adds a custom potion effect to the item.
    public static void addPotionEffect(ItemStack potionStack, Potion potion, int duration, int amplifier) {
        EffectHelper.addPotionEffect(potionStack, potion, duration, amplifier, false);
    }
    public static void addPotionEffect(ItemStack potionStack, Potion potion, int duration, int amplifier, boolean ambient) {
        if (potionStack.stackTagCompound == null) {
            potionStack.stackTagCompound = new NBTTagCompound();
        }
        if (!potionStack.stackTagCompound.hasKey("CustomPotionEffects")) {
            potionStack.stackTagCompound.setTag("CustomPotionEffects", new NBTTagList());
        }
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("Id", (byte) potion.id);
        tag.setInteger("Duration", duration);
        tag.setByte("Amplifier", (byte) amplifier);
        tag.setBoolean("Ambient", ambient);
        potionStack.stackTagCompound.getTagList("CustomPotionEffects", tag.getId()).appendTag(tag);
    }

    /// Applies the enchantment to the item stack at the given level or changes an existing enchantment's level.
    public static void overrideEnchantment(ItemStack itemStack, Enchantment enchantment, int level) {
        if (itemStack.stackTagCompound == null) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        if (!itemStack.stackTagCompound.hasKey("ench")) {
            itemStack.stackTagCompound.setTag("ench", new NBTTagList());
        }
        NBTTagList enchList = (NBTTagList)itemStack.stackTagCompound.getTag("ench");
        NBTTagCompound enchTag;
        for (int i = enchList.tagCount(); i-- > 0;) {
            enchTag = enchList.getCompoundTagAt(i);
            if (enchTag.getShort("id") != enchantment.effectId) {
                continue;
            }
            if (enchTag.getShort("lvl") < level) {
                enchTag.setShort("lvl", (byte)level);
            }
            return;
        }
        enchTag = new NBTTagCompound();
        enchTag.setShort("id", (short)enchantment.effectId);
        enchTag.setShort("lvl", (byte)level);
        enchList.appendTag(enchTag);
    }

    // Converts the item stack's enchantment tag into a shallow arraylist copy for ease of use.
    public static ArrayList<NBTTagCompound> getEnchantments(ItemStack itemStack) {
        ArrayList<NBTTagCompound> enchantments = new ArrayList<NBTTagCompound>(0);
        if (itemStack.stackTagCompound == null || !itemStack.stackTagCompound.hasKey("ench"))
            return enchantments;
        NBTTagList enchList = (NBTTagList)itemStack.stackTagCompound.getTag("ench");
        int length = enchList.tagCount();
        enchantments.ensureCapacity(length);
        for (int i = 0; i < length; i++) {
            enchantments.add((NBTTagCompound)enchList.getCompoundTagAt(i).copy());
        }
        return enchantments;
    }

    // Sets the enchantments for the given item using an arraylist for ease of use.
    public static void setEnchantments(ItemStack itemStack, ArrayList<NBTTagCompound> enchantments) {
        if (itemStack.stackTagCompound == null) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        NBTTagList enchList = new NBTTagList();
        for (NBTTagCompound enchTag : enchantments) {
            enchList.appendTag(enchTag.copy());
        }
        itemStack.stackTagCompound.setTag("ench", enchList);
    }

    // Adds the enchantments to the given item using a 2D array for ease of use.
    public static void addEnchantments(ItemStack itemStack, int[]... enchantments) {
        if (itemStack.stackTagCompound == null) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        if (!itemStack.stackTagCompound.hasKey("ench")) {
            itemStack.stackTagCompound.setTag("ench", new NBTTagList());
        }
        NBTTagList enchList = (NBTTagList)itemStack.stackTagCompound.getTag("ench");
        HashMap<Short, NBTTagCompound> existing = new HashMap<Short, NBTTagCompound>();
        NBTTagCompound enchTag;
        for (int i = enchList.tagCount(); i-- > 0;) {
            enchTag = enchList.getCompoundTagAt(i);
            existing.put(Short.valueOf(enchTag.getShort("id")), enchTag);
        }
        for (int[] enchant : enchantments) {
            if (existing.containsKey(Short.valueOf((short)enchant[0]))) {
                enchTag = existing.get(Short.valueOf((short)enchant[0]));
                if (enchTag.getShort("lvl") < enchant[1]) {
                    enchTag.setShort("lvl", (short)enchant[1]);
                }
            }
            else {
                enchTag = new NBTTagCompound();
                enchTag.setShort("id", (short)enchant[0]);
                enchTag.setShort("lvl", (short)enchant[1]);
                enchList.appendTag(enchTag);
            }
        }
    }

    // Applies the enchantment to the itemStack at the given level. Called by all other enchantItem methods to do the actual enchanting.
    public static void enchantItem(ItemStack itemStack, Enchantment enchantment, int level) {
        itemStack.addEnchantment(enchantment, level);
    }

    // Applies the enchantment with the given enchantment id and level.
    public static void enchantItem(ItemStack itemStack, int enchantmentID, int level) {
        EffectHelper.enchantItem(itemStack, Enchantment.enchantmentsList[enchantmentID], level);
    }

    // Randomly enchants the itemStack based on the level (identical to using an enchantment table).
    public static boolean enchantItem(ItemStack itemStack, int level) {
        return EffectHelper.enchantItem(_SpecialMobs.random, itemStack, level);
    }
    public static boolean enchantItem(Random random, ItemStack itemStack, int level) {
        if (level <= 0 || itemStack == null || !itemStack.isItemEnchantable())
            return false;
        try {
        	EnchantmentHelper.addRandomEnchantment(random, itemStack, level);
        }
        catch (Exception ex) {
        	_SpecialMobs.console("Error applying enchantments! item:" + itemStack.toString());
        	ex.printStackTrace();
        }
        return true;
    }

    // Enchants the item based on the String given. "max" places most possible enchantments at their max levels, "super" places most possible enchantments at level 10, and adding "SilkTouch" to either will place silkTouch on tools or add all enchantments instead of most.
    public static boolean enchantItem(ItemStack itemStack, String type) {
        if (itemStack == null || !itemStack.isItemEnchantable())
            return false;
        boolean isSuper = false;
        boolean silkTouch = false;
        if (type == "max") {
            // Do nothing.
        }
        else if (type == "maxSilkTouch") {
            silkTouch = true;
        }
        else if (type == "super") {
            isSuper = true;
        }
        else if (type == "superSilkTouch") {
            isSuper = silkTouch = true;
        }
        else
            return false;
        Item item = itemStack.getItem();
        if (item instanceof ItemArmor) {
            EffectHelper.enchantItem(itemStack, Enchantment.protection, isSuper ? 10 : Enchantment.protection.getMaxLevel());
            if (((ItemArmor)item).armorType == 0) {
                EffectHelper.enchantItem(itemStack, Enchantment.respiration, isSuper ? 10 : Enchantment.respiration.getMaxLevel());
                if (silkTouch) {
                    EffectHelper.enchantItem(itemStack, Enchantment.aquaAffinity, isSuper ? 10 : Enchantment.aquaAffinity.getMaxLevel());
                }
            }
            else if (((ItemArmor)item).armorType == 3) {
                EffectHelper.enchantItem(itemStack, Enchantment.featherFalling, isSuper ? 10 : Enchantment.featherFalling.getMaxLevel());
            }
        }
        else if (item instanceof ItemSword) {
            EffectHelper.enchantItem(itemStack, Enchantment.sharpness, isSuper ? 10 : Enchantment.sharpness.getMaxLevel());
            EffectHelper.enchantItem(itemStack, Enchantment.knockback, isSuper ? 10 : Enchantment.knockback.getMaxLevel());
            if (silkTouch) {
                EffectHelper.enchantItem(itemStack, Enchantment.looting, isSuper ? 10 : Enchantment.looting.getMaxLevel());
            }
        }
        else if (item instanceof ItemTool) {
            EffectHelper.enchantItem(itemStack, Enchantment.efficiency, isSuper ? 10 : Enchantment.efficiency.getMaxLevel());
            EffectHelper.enchantItem(itemStack, Enchantment.unbreaking, isSuper ? 10 : Enchantment.unbreaking.getMaxLevel());
            if (silkTouch) {
                EffectHelper.enchantItem(itemStack, Enchantment.silkTouch, isSuper ? 10 : Enchantment.silkTouch.getMaxLevel());
            }
            else {
                EffectHelper.enchantItem(itemStack, Enchantment.fortune, isSuper ? 10 : Enchantment.fortune.getMaxLevel());
            }
        }
        else if (item instanceof ItemBow) {
            EffectHelper.enchantItem(itemStack, Enchantment.power, isSuper ? 10 : Enchantment.power.getMaxLevel());
            EffectHelper.enchantItem(itemStack, Enchantment.punch, isSuper ? 10 : Enchantment.punch.getMaxLevel());
            if (silkTouch) {
                EffectHelper.enchantItem(itemStack, Enchantment.infinity, isSuper ? 10 : Enchantment.infinity.getMaxLevel());
            }
        }
        return true;
    }

    // Dyes the given itemStack. Only works on leather armor, returns true if it works.
    public static boolean dye(ItemStack itemStack, String colorName) {
        for (int i = ItemDye.field_150922_c.length; i-- > 0;)
            if (colorName.equalsIgnoreCase(ItemDye.field_150921_b[i]))
                return EffectHelper.dye(itemStack, (byte)i);
        _SpecialMobs.debugException("Tried to dye with an invalid dye name (" + colorName + ")! Valid dye names: black, red, green, brown, blue, purple, cyan, silver, gray, pink, lime, yellow, lightBlue, magenta, orange, white.");
        return false;
    }
    public static boolean dye(ItemStack itemStack, byte colorIndex) {
        if (colorIndex < 0 || colorIndex >= ItemDye.field_150922_c.length) {
            _SpecialMobs.debugException("Tried to dye with an invalid dye index (" + colorIndex + ")!");
            return false;
        }
        float[] rgb = EntitySheep.fleeceColorTable[BlockColored.func_150031_c(colorIndex)];
        return EffectHelper.dye(itemStack, (int)(rgb[0] * 255.0F), (int)(rgb[1] * 255.0F), (int)(rgb[2] * 255.0F));
    }
    public static boolean dye(ItemStack itemStack, int red, int green, int blue) {
        if (red > 255 || green > 255 || blue > 255 || red < 0 || green < 0 || blue < 0) {
            _SpecialMobs.debugException("Tried to dye with an invalid RGB value (" + red + ", " + green + ", " + blue + ")!");
            return false;
        }
        return EffectHelper.dye(itemStack, (red << 16) + (green << 8) + blue);
    }
    public static boolean dye(ItemStack itemStack, int color) {
        if (color < 0 || color > 0xffffff) {
            _SpecialMobs.debugException("Tried to dye with an invalid color value (" + color + ")!");
            return false;
        }
        try {
            ((ItemArmor) itemStack.getItem()).func_82813_b(itemStack, color); /// Dyes the armor if it is leather.
        }
        catch (Exception ex) {
            return false;
        }
        return true;
    }
}