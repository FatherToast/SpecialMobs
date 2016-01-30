package toast.specialMobs.entity.slime;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs._SpecialMobs;

public class EntityGrapeSlime extends Entity_SpecialSlime
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "slime/grape.png")
    };

    public EntityGrapeSlime(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityGrapeSlime.TEXTURES);
        this.getSpecialData().isImmuneToFalling = true;
    }

    /// Gets the additional experience this slime type gives.
    @Override
	protected int getTypeXp() {
    	return 2;
    }

    /// Called when this slime jumps, returns true if it successfully jumps.
    @Override
	protected boolean jumpByType(EntityPlayer target) {
        if (target != null) {
            float distance = (float) this.getDistanceSqToEntity(target);
            if (distance > 36.0F && distance < 144.0F) {
                double vX = target.posX - this.posX;
                double vZ = target.posZ - this.posZ;
                double vH = Math.sqrt(vX * vX + vZ * vZ);
                this.motionX = vX / vH * 1.11 + this.motionX * 0.21;
                this.motionY = 0.41 * 2.61;
                this.motionZ = vZ / vH * 1.11 + this.motionZ * 0.21;
                this.onGround = false;
                return true;
            }
        }
    	return super.jumpByType(target);
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (this.getSlimeSize() == 1) {
	        for (int i = this.rand.nextInt(2) + 1; i-- > 0;) {
	            this.dropItem(Items.slime_ball, 1);
	        }
	        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
	            this.entityDropItem(new ItemStack(Items.dye, 1, 5), 0.0F);
	        }
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        if (this.getSlimeSize() == 1) {
	        ItemStack itemStack = new ItemStack(Items.potionitem, 1, 8193);
	        EffectHelper.setItemName(itemStack, "Potion of Jumping", 0xf);
	        EffectHelper.addPotionEffect(itemStack, Potion.jump, 600, 9);
	        this.entityDropItem(itemStack, 0.0F);
        }
    }
}