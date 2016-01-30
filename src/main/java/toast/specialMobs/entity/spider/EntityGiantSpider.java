package toast.specialMobs.entity.spider;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.world.World;

public class EntityGiantSpider extends Entity_SpecialSpider
{
    public EntityGiantSpider(World world) {
        super(world);
        this.setSize(1.9F, 1.3F);
        this.getSpecialData().resetRenderScale(1.5F);
        this.experienceValue += 2;
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 16.0);
        this.getSpecialData().addAttribute(SharedMonsterAttributes.attackDamage, 2.0);
        this.getSpecialData().arrowDamage += 1.0F;
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        for (int i = this.rand.nextInt(2) + 1; i-- > 0;) {
            this.dropItem(Items.string, 1);
        }
    }
}