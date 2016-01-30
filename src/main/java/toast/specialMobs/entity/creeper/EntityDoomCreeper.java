package toast.specialMobs.entity.creeper;

import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityDoomCreeper extends Entity_SpecialCreeper
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "creeper/doom.png")
    };

    public EntityDoomCreeper(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityDoomCreeper.TEXTURES);
        this.experienceValue += 1;
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        float power = this.getPowered() ? this.explosionRadius * 1.4F : this.explosionRadius * 0.7F;
        this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, power, false);
        super.dropFewItems(hit, looting);
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
            this.dropItem(Items.bone, 1);
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        this.dropItem(Items.ghast_tear, 1);
    }
}