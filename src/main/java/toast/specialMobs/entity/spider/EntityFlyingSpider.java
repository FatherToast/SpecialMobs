package toast.specialMobs.entity.spider;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityFlyingSpider extends Entity_SpecialSpider
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "spider/flying.png"),
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "spider/flying_eyes.png")
    };

    public EntityFlyingSpider(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityFlyingSpider.TEXTURES);
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
    protected void attackEntity(Entity target, float distance) {
        if (this.onGround && distance >= 6.0F && distance < 12.0F && this.rand.nextInt(10) == 0) {
            double vX = target.posX - this.posX;
            double vZ = target.posZ - this.posZ;
            double vH = Math.sqrt(vX * vX + vZ * vZ);
            this.motionX = vX / vH * 2.0 + this.motionX * 0.2;
            this.motionZ = vZ / vH * 2.0 + this.motionZ * 0.2;
            this.motionY = 0.4 * 2.0;
        }
        else {
            super.attackEntity(target, distance);
        }
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
            this.dropItem(Items.feather, 1);
        }
    }
}