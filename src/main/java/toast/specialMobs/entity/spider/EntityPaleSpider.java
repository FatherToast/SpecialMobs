package toast.specialMobs.entity.spider;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityPaleSpider extends Entity_SpecialSpider
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "spider/pale.png"),
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "spider/pale_eyes.png")
    };

    public EntityPaleSpider(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityPaleSpider.TEXTURES);
        this.experienceValue += 2;
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().armor += 15;
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
            ((EntityLivingBase)target).addPotionEffect(new PotionEffect(Potion.weakness.id, time, 2));
        }
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (hit && (this.rand.nextInt(5) == 0 || this.rand.nextInt(1 + looting) > 0)) {
            this.dropItem(Items.fermented_spider_eye, 1);
        }
    }
}