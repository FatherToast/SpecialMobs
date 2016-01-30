package toast.specialMobs.entity.spider;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs._SpecialMobs;

public class EntityDesertSpider extends Entity_SpecialSpider
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "spider/desert.png"),
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "spider/desert_eyes.png")
    };

    public EntityDesertSpider(World world) {
        super(world);
        this.setSize(1.0F, 0.8F);
        this.getSpecialData().setTextures(EntityDesertSpider.TEXTURES);
        this.getSpecialData().resetRenderScale(0.8F);
        this.experienceValue += 2;
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 4.0);
    }

    /// Overridden to modify attack effects.
    @Override
    public void onTypeAttack(Entity target) {
        if (target instanceof EntityLivingBase) {
            int time;
            switch (target.worldObj.difficultySetting) {
                case PEACEFUL:
                    return;
                case EASY:
                    time = 3;
                    break;
                case NORMAL:
                    time = 7;
                    break;
                default:
                    time = 15;
            }
            time *= 20;
            EffectHelper.stackEffect((EntityLivingBase)target, Potion.confusion, time, 1, 5);
            EffectHelper.stackEffect((EntityLivingBase)target, Potion.moveSlowdown, time, 1, 5);
            ((EntityLivingBase)target).addPotionEffect(new PotionEffect(Potion.blindness.id, time, 0));
            ((EntityLivingBase)target).addPotionEffect(new PotionEffect(Potion.resistance.id, time, -2));
        }
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        for (int i = this.rand.nextInt(2 + looting); i-- > 0;) {
            this.dropItem(Items.leather, 1);
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        ItemStack itemStack = new ItemStack(Items.potionitem, 1, 16387);
        EffectHelper.setItemName(itemStack, "Splash Potion of Desert Venom", 0xf);
        EffectHelper.addPotionEffect(itemStack, Potion.confusion, 900, 1);
        EffectHelper.addPotionEffect(itemStack, Potion.moveSlowdown, 900, 1);
        EffectHelper.addPotionEffect(itemStack, Potion.blindness, 900, 0);
        this.entityDropItem(itemStack, 0.0F);
    }
}