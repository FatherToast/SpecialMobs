package toast.specialMobs.entity.ghast;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs.MobHelper;
import toast.specialMobs._SpecialMobs;

public class EntityUnholyGhast extends EntityMeleeGhast
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "ghast/unholy.png"),
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "ghast/unholy_shooting.png")
    };

    public EntityUnholyGhast(World world) {
        super(world);
        this.setSize(2.0F, 2.0F);
        this.getSpecialData().setTextures(EntityUnholyGhast.TEXTURES);
        this.getSpecialData().resetRenderScale(0.5F);
        this.experienceValue += 4;
    }

    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.attackDamage, 2.0);
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 1.2);
    }

    /// Get this entity's creature type.
    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEAD;
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
                    if (EnchantmentHelper.getEnchantmentLevel(Enchantment.smite.effectId, heldItem) > 0)
                        return true;
                    /// Tinker's Construct compatibility.
                    if (heldItem.hasTagCompound()) {
                        NBTTagCompound tinkerTag = heldItem.getTagCompound().getCompoundTag("InfiTool");
                        if (tinkerTag.hasKey("ModSmite") && tinkerTag.getIntArray("ModSmite")[0] > 0)
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
        ItemStack drop = new ItemStack(Items.golden_sword);
        EffectHelper.setItemName(drop, "Excalibur", 0xd);
        drop.addEnchantment(Enchantment.smite, 10);
        drop.addEnchantment(Enchantment.fireAspect, this.rand.nextInt(2) + 1);
        drop.addEnchantment(Enchantment.unbreaking, 3);
        this.entityDropItem(drop, 0.0F);
    }
}