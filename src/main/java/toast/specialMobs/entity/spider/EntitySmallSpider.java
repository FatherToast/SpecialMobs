package toast.specialMobs.entity.spider;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.world.World;

public class EntitySmallSpider extends Entity_SpecialSpider
{
    public EntitySmallSpider(World world) {
        super(world);
        this.setSize(0.7F, 0.5F);
        this.getSpecialData().resetRenderScale(0.7F);
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 1.2);
    }
}