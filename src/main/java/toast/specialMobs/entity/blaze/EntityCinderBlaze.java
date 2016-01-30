package toast.specialMobs.entity.blaze;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.world.World;

public class EntityCinderBlaze extends Entity_SpecialBlaze
{
    public EntityCinderBlaze(World world) {
        super(world);
        this.setSize(0.5F, 0.9F);
        this.getSpecialData().resetRenderScale(0.5F);
    }

    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.attackDamage, -2.0);
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 1.3);
        this.setRangedAI(0, 6, 60, 100, 0.0F);
        this.getSpecialData().arrowDamage -= 1.0F;
    }

    /// Overridden to modify attack effects.
    @Override
	protected void onTypeAttack(Entity target) {
    	target.setFire(4);
    }
}