package toast.specialMobs.entity.cavespider;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs._SpecialMobs;

public class EntityFlyingCaveSpider extends Entity_SpecialCaveSpider
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "cavespider/flying.png"),
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "cavespider/flying_eyes.png")
    };

    public EntityFlyingCaveSpider(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityFlyingCaveSpider.TEXTURES);
        this.getSpecialData().isImmuneToFalling = true;
        this.experienceValue += 2;
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 1.3);
    }

    /// Called each tick this entity's attack target can be seen.
    @Override
    protected void attackEntity(Entity target, float distanceSq) {
        if (this.onGround && distanceSq >= 6.0F && distanceSq < 12.0F && this.rand.nextInt(10) == 0) {
            double vX = target.posX - this.posX;
            double vZ = target.posZ - this.posZ;
            double vH = Math.sqrt(vX * vX + vZ * vZ);
            this.motionX = vX / vH * 2.0 + this.motionX * 0.2;
            this.motionZ = vZ / vH * 2.0 + this.motionZ * 0.2;
            this.motionY = 0.4 * 2.0;
        }
        else {
            super.attackEntity(target, distanceSq);
        }
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        for (int i = this.rand.nextInt(2 + looting); i-- > 0;) {
            this.dropItem(Items.feather, 1);
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        ItemStack itemStack = new ItemStack(Items.potionitem, 1, 8194);
        EffectHelper.setItemName(itemStack, "Potion of Flying", 0xf);
        EffectHelper.addPotionEffect(itemStack, Potion.jump, 600, 3);
        EffectHelper.addPotionEffect(itemStack, Potion.moveSpeed, 600, 1);
        EffectHelper.addPotionEffect(itemStack, Potion.weakness, 600, 3);
        this.entityDropItem(itemStack, 0.0F);
    }
}