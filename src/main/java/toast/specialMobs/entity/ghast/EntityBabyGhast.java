package toast.specialMobs.entity.ghast;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.world.World;

public class EntityBabyGhast extends EntityMeleeGhast
{
    public EntityBabyGhast(World world) {
        super(world);
        this.setSize(1.0F, 1.0F);
        this.getSpecialData().resetRenderScale(0.25F);
        this.experienceValue = 1;
    }

    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.attackDamage, -2.0);
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 1.5);
    }

    /// Returns the sound this mob makes while it's alive.
    @Override
    protected String getLivingSound() {
        return null;
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        for (int i = this.rand.nextInt(2 + looting); i-- > 0;) {
            this.dropItem(Items.gunpowder, 1);
        }
    }
}