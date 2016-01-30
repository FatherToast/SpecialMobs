package toast.specialMobs.entity.creeper;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.world.World;

public class EntityMiniCreeper extends Entity_SpecialCreeper
{
    public EntityMiniCreeper(World world) {
        super(world);
        this.setSize(0.5F, 0.9F);
        this.getSpecialData().resetRenderScale(0.5F);
        this.experienceValue -= 1;
    }

    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 1.3);
    }

    /// The explosion caused by this creeper.
    @Override
    public void explodeByType(boolean powered, boolean griefing) {
        float power = powered ? (float)this.explosionRadius : this.explosionRadius / 2.0F;
        this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, power, griefing);
    }
}