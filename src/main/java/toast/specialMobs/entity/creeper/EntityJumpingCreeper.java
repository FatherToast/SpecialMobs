package toast.specialMobs.entity.creeper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs._SpecialMobs;

public class EntityJumpingCreeper extends Entity_SpecialCreeper
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "creeper/jumping.png")
    };

    public EntityJumpingCreeper(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityJumpingCreeper.TEXTURES);
        this.getSpecialData().isImmuneToFalling = true;
        this.experienceValue += 2;
    }

    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 1.3);
    }

    /// Called every tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        Entity target = this.getAttackTarget();
        if (target != null) {
            float distance = this.getDistanceToEntity(target);
            if (distance > 6.0F && distance < 10.0F && this.onGround && this.rand.nextInt(10) == 0) {
                double vX = target.posX - this.posX;
                double vZ = target.posZ - this.posZ;
                double vH = Math.sqrt(vX * vX + vZ * vZ);
                this.motionX = vX / vH * 1.31 + this.motionX * 0.21;
                this.motionY = 0.41 * 2.1;
                this.motionZ = vZ / vH * 1.31 + this.motionZ * 0.21;
            }
        }
        super.onLivingUpdate();
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
            this.dropItem(Items.slime_ball, 1);
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        ItemStack itemStack = new ItemStack(Items.potionitem, 1, 8193);
        EffectHelper.setItemName(itemStack, "Potion of Jumping", 0xf);
        EffectHelper.addPotionEffect(itemStack, Potion.jump, 600, 9);
        this.entityDropItem(itemStack, 0.0F);
    }
}