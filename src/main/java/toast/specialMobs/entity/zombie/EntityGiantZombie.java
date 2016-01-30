package toast.specialMobs.entity.zombie;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.world.World;

public class EntityGiantZombie extends Entity_SpecialZombie
{
    public EntityGiantZombie(World world) {
        super(world);
        this.stepHeight = 1.0F;
        this.setSize(0.9F, 2.7F);
        this.func_146069_a(1.0F); // Set size scale
        this.getSpecialData().resetRenderScale(1.5F);
        this.experienceValue += 2;
    }

    /// Override to set the attack AI to use.
    @Override
    protected void initTypeAI() {
        this.setRangedAI(1.0, 26, 80, 17.0F);
        this.setMeleeAI(1.2);
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 20.0);
        this.getSpecialData().addAttribute(SharedMonsterAttributes.attackDamage, 2.0);
        this.getSpecialData().arrowDamage += 2.0F;
    }

    /// If true, this entity is a baby.
    @Override
    public boolean isChild() {
        return false;
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        for (int i = this.rand.nextInt(2) + 1; i-- > 0;) {
            this.dropItem(Items.rotten_flesh, 1);
        }
    }
}