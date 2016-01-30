package toast.specialMobs.entity.zombie;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs.EnchantmentSpecial;
import toast.specialMobs._SpecialMobs;

public class EntityPlagueZombie extends Entity_SpecialZombie
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "zombie/plague.png"),
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "zombie/plague_villager.png")
    };

    public EntityPlagueZombie(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityPlagueZombie.TEXTURES);
        this.experienceValue += 2;
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 1.3);

        ItemStack itemStack = this.getHeldItem();
        if (itemStack != null) {
            if (itemStack.getItem() instanceof ItemBow) {
                if (EnchantmentSpecial.plagueBow != null) {
                    EffectHelper.overrideEnchantment(itemStack, EnchantmentSpecial.plagueBow, this.rand.nextInt(EnchantmentSpecial.plagueBow.getMaxLevel()) + 1);
                }
            }
            else if (EnchantmentSpecial.plagueSword != null) {
                EffectHelper.overrideEnchantment(itemStack, EnchantmentSpecial.plagueSword, this.rand.nextInt(EnchantmentSpecial.plagueSword.getMaxLevel()) + 1);
            }
        }
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        for (int i = this.rand.nextInt(2 + looting); i-- > 0;) {
            this.dropItem(Item.getItemFromBlock(this.rand.nextBoolean() ? Blocks.brown_mushroom : Blocks.red_mushroom), 1);
        }
    }
}