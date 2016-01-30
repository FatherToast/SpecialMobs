package toast.specialMobs.entity.skeleton;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.world.World;

public class EntityGiantSkeleton extends Entity_SpecialSkeleton
{
    public EntityGiantSkeleton(World world) {
        super(world);
        this.stepHeight = 1.0F;
        this.setSize(0.9F, 2.7F);
        this.updateScale();
        this.getSpecialData().resetRenderScale(1.5F);
        this.experienceValue += 2;
    }

    /// Override to set the attack AI to use.
    @Override
    protected void initTypeAI() {
        this.setRangedAI(1.0, 23, 70, 17.0F);
        this.setMeleeAI(1.2);
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 20.0);
        this.getSpecialData().addAttribute(SharedMonsterAttributes.attackDamage, 2.0);
        this.getSpecialData().arrowDamage += 2.0F;
    }

    /// Called every tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.worldObj.isRemote && this.getSkeletonType() == 1) {
            this.setSize(0.95F, 3.24F);
            this.updateScale();
        }
    }

    /// If true, this entity is a baby.
    @Override
    public boolean isChild() {
        return false;
    }

    /// Set this skeleton's type.
    @Override
    public void setSkeletonType(int type) {
        super.setSkeletonType(type);
        if (type == 1) {
            this.setSize(0.95F, 3.24F);
        }
        else {
            this.setSize(0.9F, 2.7F);
        }
        this.updateScale();
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        for (int i = this.rand.nextInt(2) + 1; i-- > 0;) {
            this.dropItem(Items.bone, 1);
        }
    }
}