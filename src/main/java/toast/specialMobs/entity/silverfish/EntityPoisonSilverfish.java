package toast.specialMobs.entity.silverfish;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityPoisonSilverfish extends Entity_SpecialSilverfish
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "silverfish/poison.png")
    };

    public EntityPoisonSilverfish(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityPoisonSilverfish.TEXTURES);
        this.getSpecialData().immuneToPotions.add(Potion.poison.id);
        this.experienceValue += 1;
    }

    /// Overridden to modify attack effects.
    @Override
    protected void onTypeAttack(Entity target) {
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
            ((EntityLivingBase)target).addPotionEffect(new PotionEffect(Potion.poison.id, time, 0));
        }
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
            this.dropItem(Items.spider_eye, 1);
        }

    }
}