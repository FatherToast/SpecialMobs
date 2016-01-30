package toast.specialMobs.entity.slime;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityBlueberrySlime extends Entity_SpecialSlime
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "slime/blueberry.png")
    };

    public EntityBlueberrySlime(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityBlueberrySlime.TEXTURES);
        this.getSpecialData().isImmuneToBurning = true;
        this.getSpecialData().canBreatheInWater = true;
    }

    /// Gets the additional experience this slime type gives.
    @Override
	protected int getTypeXp() {
    	return 1;
    }

    /// Overridden to modify inherited attribites, except for health.
    @Override
	protected void adjustTypeAttributes() {
    	this.getSpecialData().addAttribute(SharedMonsterAttributes.attackDamage, 2.0);
    }

    @Override
	public boolean handleWaterMovement() {
    	if (this.worldObj.isAnyLiquid(this.boundingBox)) {
			this.fallDistance = 0.0F;
            this.extinguish();
		}
    	return false;
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (this.getSlimeSize() == 1) {
	        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
	            this.entityDropItem(new ItemStack(Items.dye, 1, 12), 0.0F);
	        }
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        if (this.getSlimeSize() == 1) {
			this.entityDropItem(new ItemStack(Items.potionitem, 1, superRare > 0 ? 8269 : 8205), 0.0F);
		}
    }
}