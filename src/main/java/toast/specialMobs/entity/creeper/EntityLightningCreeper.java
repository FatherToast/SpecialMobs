package toast.specialMobs.entity.creeper;

import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.MobHelper;
import toast.specialMobs._SpecialMobs;

public class EntityLightningCreeper extends Entity_SpecialCreeper {
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] { new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "creeper/lightning.png") };

    public EntityLightningCreeper(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityLightningCreeper.TEXTURES);
        this.getSpecialData().isImmuneToFire = this.isImmuneToFire = true;
        this.experienceValue += 1;
    }

    /// The explosion caused by this creeper.
    @Override
    public void explodeByType(boolean powered, boolean griefing) {
        float power = powered ? this.explosionRadius * 2.0F : this.explosionRadius / 3.0F;
        this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, power, griefing);
        MobHelper.lightningExplode(this, this.explosionRadius);

        if (powered) {
            int duration = this.rand.nextInt(12000) + 3600;
            if (!this.worldObj.getWorldInfo().isThundering() || this.worldObj.getWorldInfo().getThunderTime() < duration) {
                this.worldObj.getWorldInfo().setThunderTime(duration);
                this.worldObj.getWorldInfo().setThundering(true);
            }
            duration += 1200;
            if (!this.worldObj.getWorldInfo().isRaining() || this.worldObj.getWorldInfo().getRainTime() < duration) {
                this.worldObj.getWorldInfo().setRainTime(duration);
                this.worldObj.getWorldInfo().setRaining(true);
            }
        }
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        for (int i = this.rand.nextInt(2 + looting); i-- > 0;) {
            this.dropItem(Items.redstone, 1);
        }
    }
}