package toast.specialMobs.entity.slime;

import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityStrawberrySlime extends Entity_SpecialSlime
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "slime/strawberry.png")
    };

    public EntityStrawberrySlime(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityStrawberrySlime.TEXTURES);
        this.getSpecialData().isImmuneToFire = this.isImmuneToFire = true;
        this.getSpecialData().isDamagedByWater = true;
    }

    /// Gets the additional experience this slime type gives.
    @Override
	protected int getTypeXp() {
    	return 1;
    }

    /// Overridden to modify attack effects.
    @Override
	protected void onTypeAttack(Entity target) {
        target.setFire(this.getSlimeSize() * 4);
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (this.getSlimeSize() == 1) {
	        for (int i = this.rand.nextInt(2 + looting); i-- > 0;) {
	            this.dropItem(Items.fire_charge, 1);
	        }
	        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
	            this.entityDropItem(new ItemStack(Items.dye, 1, 1), 0.0F);
	        }
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        if (this.getSlimeSize() == 1) {
			this.entityDropItem(new ItemStack(Items.potionitem, 1, superRare > 0 ? 8259 : 8195), 0.0F);
		}
    }
}