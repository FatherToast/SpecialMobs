package toast.specialMobs.entity.creeper;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.MobHelper;
import toast.specialMobs._SpecialMobs;

public class EntityGravelCreeper extends Entity_SpecialCreeper {
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] { new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "creeper/gravel.png") };

    public EntityGravelCreeper(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityGravelCreeper.TEXTURES);
        this.experienceValue += 1;
    }

    /// The explosion caused by this creeper.
    @Override
    public void explodeByType(boolean powered, boolean griefing) {
        float power = (powered ? this.explosionRadius * 2.0F : this.explosionRadius) / 2.0F;
        this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, power, griefing);
        if (griefing) {
        	MobHelper.gravelExplode(this, power);
        }
    }

    /// Called when the entity is attacked.
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float damage) {
        if (DamageSource.fallingBlock.equals(damageSource)) {
            damage = Math.min(0.5F, damage);
        }
        return super.attackEntityFrom(damageSource, damage);
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        for (int i = this.rand.nextInt(3 + looting); i-- > 0;) {
            this.dropItem(Item.getItemFromBlock(Blocks.gravel), 1);
        }
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
            this.dropItem(Items.flint, 1);
        }
    }
}