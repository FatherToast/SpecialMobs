package toast.specialMobs.entity.pigzombie;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs.EnchantmentSpecial;
import toast.specialMobs.MobHelper;
import toast.specialMobs._SpecialMobs;

public class EntityVampirePigZombie extends Entity_SpecialPigZombie
{
    public static final ResourceLocation[] TEXTURES1 = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "pigzombie/vampire.png")
    };

    public EntityVampirePigZombie(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityVampirePigZombie.TEXTURES1);
        this.experienceValue += 4;
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.attackDamage, 4.0);
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 1.2);
        this.getSpecialData().arrowDamage += 2.0F;

        if (this.getHeldItem() != null && !(this.getHeldItem().getItem() instanceof ItemBow)) {
            this.setCurrentItemOrArmor(0, null);
        }
        this.setCanPickUpLoot(false);
    }

    /// Damages this entity from the damageSource by the given amount. Returns true if this entity is damaged.
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float damage) {
    	float damageLimit = MobHelper.isCritical(damageSource) ? 1.0F : 0.5F;
        if (damage > damageLimit && !this.isDamageSourceEffective(damageSource)) {
            damage = damageLimit;
        }
        return super.attackEntityFrom(damageSource, damage);
    }

    /// Returns true if the given damage source can harm this ghast.
    public boolean isDamageSourceEffective(DamageSource damageSource) {
        if (damageSource != null) {
            if (damageSource.canHarmInCreative())
                return true;
            Entity attacker = damageSource.getEntity();
            if (attacker instanceof EntityLivingBase) {
                ItemStack heldItem = ((EntityLivingBase)attacker).getHeldItem();
                if (heldItem != null) {
                    if (heldItem.getItem() instanceof ItemSword && ((ItemSword)heldItem.getItem()).getToolMaterialName().equals(Item.ToolMaterial.WOOD.toString()) ||heldItem.getItem() instanceof ItemTool && ((ItemTool)heldItem.getItem()).getToolMaterialName().equals(Item.ToolMaterial.WOOD.toString()) || heldItem.getItem() == Items.wooden_hoe)
                        return true;
                    /// Tinker's Construct compatibility.
                    if (heldItem.hasTagCompound()) {
                        NBTTagCompound tinkerTag = heldItem.getTagCompound().getCompoundTag("InfiTool");
                        if (tinkerTag.hasKey("Head") && tinkerTag.getInteger("Head") == 0) /// Has a wooden head.
                            return true;
                    }
                }
            }
        }
        return false;
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        for (int i = this.rand.nextInt(3 + looting); i-- > 0;) {
            this.dropItem(Items.gold_ingot, 1);
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        ItemStack drop = new ItemStack(Items.wooden_sword);
        EffectHelper.setItemName(drop, "Van Helsing's Wooden Stake", 0xd);
        if (EnchantmentSpecial.painSword != null) {
            drop.addEnchantment(EnchantmentSpecial.painSword, 5);
        }
        else {
            drop.addEnchantment(Enchantment.sharpness, 5);
        }
        drop.addEnchantment(Enchantment.unbreaking, 3);
        this.entityDropItem(drop, 0.0F);
    }
}