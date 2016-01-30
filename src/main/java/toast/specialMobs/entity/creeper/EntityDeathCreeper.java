package toast.specialMobs.entity.creeper;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityDeathCreeper extends Entity_SpecialCreeper
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "creeper/death.png")
    };

    public EntityDeathCreeper(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityDeathCreeper.TEXTURES);
        this.setExplodesWhenBurning(true);
        this.experienceValue += 1;
    }

    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 1.2);
    }

    /// The explosion caused by this creeper.
    @Override
    public void explodeByType(boolean powered, boolean griefing) {
        float power = powered ? (this.explosionRadius + 2) * 2.0F : (float)(this.explosionRadius + 2);
        this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, power, griefing);
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        for (int i = this.rand.nextInt(3 + looting) + 1; i-- > 0;) {
            this.dropItem(Items.gunpowder, 1);
        }
        if (hit && this.isBurning() && looting > 0) {
            //this.dropItem(Items.record_13 + this.rand.nextInt(Items.record_wait - Items.record_13 + 1), 1);
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        this.dropItem(Item.getItemFromBlock(Blocks.tnt), 1);
    }
}