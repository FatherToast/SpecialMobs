package toast.specialMobs.entity.enderman;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityIcyEnderman extends Entity_SpecialEnderman
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "enderman/icy.png"),
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "enderman/icy_eyes.png")
    };

    public EntityIcyEnderman(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityIcyEnderman.TEXTURES);
        this.getSpecialData().isImmuneToBurning = true;
        this.experienceValue += 1;
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().armor += 10;
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
                    time = 2;
                    break;
                case NORMAL:
                    time = 5;
                    break;
                default:
                    time = 12;
            }
            time *= 20;
            ((EntityLivingBase)target).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, time, 5));
        }
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
            for (int i = this.rand.nextInt(8 + looting) + 1; i-- > 0;) {
                this.dropItem(Items.snowball, 1);
            }
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        this.dropItem(Item.getItemFromBlock(Blocks.ice), 1);
    }
}