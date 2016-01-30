package toast.specialMobs.entity.slime;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs._SpecialMobs;

public class EntityWatermelonSlime extends Entity_SpecialSlime
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "slime/watermelon.png")
    };

    public EntityWatermelonSlime(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityWatermelonSlime.TEXTURES);
        this.getSpecialData().armor = 10;
    }

    /// Gets the additional experience this slime type gives.
    @Override
	protected int getTypeXp() {
    	return 2;
    }

    /// Overridden to modify inherited attribites, except for health.
    @Override
	protected void adjustTypeAttributes() {
    	this.getSpecialData().addAttribute(SharedMonsterAttributes.attackDamage, 2.0);
    }
    /// Overridden to modify inherited max health.
    @Override
	protected void adjustHealthAttribute() {
    	this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 8.0);
    }

    /// Called when this slime jumps, returns true if it successfully jumps.
    @Override
	protected boolean jumpByType(EntityPlayer target) {
        if (target != null) {
            float distance = (float) this.getDistanceSqToEntity(target);
            if (distance < 16.0F) {
                double vX = target.posX - this.posX;
                double vZ = target.posZ - this.posZ;
                double vH = Math.sqrt(vX * vX + vZ * vZ);
                this.motionX = vX / vH * 1.41 + this.motionX * 0.2;
                this.motionY = 0.45;
                this.motionZ = vZ / vH * 1.41 + this.motionZ * 0.2;
                this.onGround = false;
                this.slimeJumpDelay /= 3;
                return true;
            }
        }
    	return super.jumpByType(target);
    }

    /// Overridden to modify attack effects.
    @Override
	protected void onTypeAttack(Entity target) {
    	target.addVelocity(-MathHelper.sin(this.rotationYaw * (float) Math.PI / 180.0F) * 0.8F, 0.1, MathHelper.cos(this.rotationYaw * (float) Math.PI / 180.0F) * 0.8F);
        this.motionX *= -0.4;
        this.motionZ *= -0.4;
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (this.getSlimeSize() == 1) {
	        for (int i = this.rand.nextInt(3 + looting); i-- > 0;) {
	            this.dropItem(Items.melon, 1);
	        }
	        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
	            this.entityDropItem(new ItemStack(Items.dye, 1, this.rand.nextBoolean() ? 9 : 10), 0.0F);
	        }
	        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
	            this.dropItem(Items.speckled_melon, 1);
	        }
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        if (this.getSlimeSize() == 1) {
	        ItemStack itemStack = new ItemStack(Items.potionitem, 1, 8206);
	        EffectHelper.setItemName(itemStack, "Potion of Resistance", 0xf);
	        EffectHelper.addPotionEffect(itemStack, Potion.resistance, 3600 / (superRare + 1), superRare);
	        this.entityDropItem(itemStack, 0.0F);
        }
    }
}