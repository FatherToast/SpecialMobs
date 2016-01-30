package toast.specialMobs.entity.pigzombie;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs.EnchantmentSpecial;
import toast.specialMobs._SpecialMobs;

public class EntityBrutishPigZombie extends Entity_SpecialPigZombie
{
    public static final ResourceLocation[] TEXTURES1 = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "pigzombie/brutish.png")
    };

    public EntityBrutishPigZombie(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityBrutishPigZombie.TEXTURES1);
        this.experienceValue += 2;
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 20.0);
        this.getSpecialData().armor += 10;

        if (this.getHeldItem() == null) {
            this.setCurrentItemOrArmor(0, new ItemStack(Items.wooden_sword));
        }
        ItemStack itemStack = this.getHeldItem();
        if (itemStack != null) {
            if (itemStack.getItem() instanceof ItemBow) {
                if (EnchantmentSpecial.painBow != null) {
                    EffectHelper.overrideEnchantment(itemStack, EnchantmentSpecial.painBow, this.rand.nextInt(EnchantmentSpecial.painBow.getMaxLevel()) + 1);
                }
            }
            else if (EnchantmentSpecial.painSword != null) {
                EffectHelper.overrideEnchantment(itemStack, EnchantmentSpecial.painSword, this.rand.nextInt(EnchantmentSpecial.painSword.getMaxLevel()) + 1);
            }
        }
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        for (int i = this.rand.nextInt(2 + looting); i-- > 0;) {
            this.dropItem(Items.flint, 1);
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        this.dropItem(Items.iron_ingot, 1);
    }
}