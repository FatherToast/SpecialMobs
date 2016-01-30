package toast.specialMobs.entity.spider;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.world.World;

public class EntityBabySpider extends Entity_SpecialSpider
{
    public EntityBabySpider(World world) {
        super(world);
        this.setSize(0.6F, 0.4F);
        this.experienceValue = 1;
        this.getSpecialData().resetRenderScale(0.5F);
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, -12.0);
        this.getSpecialData().addAttribute(SharedMonsterAttributes.attackDamage, -1.0);
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 1.3);
        this.getSpecialData().arrowDamage -= 1.0F;
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        for (int i = this.rand.nextInt(2 + looting); i-- > 0;) {
            this.dropItem(Items.string, 1);
        }
    }
}