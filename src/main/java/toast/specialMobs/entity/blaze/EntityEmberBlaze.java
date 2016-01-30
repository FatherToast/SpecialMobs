package toast.specialMobs.entity.blaze;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityEmberBlaze extends Entity_SpecialBlaze
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "blaze/ember.png")
    };

    public EntityEmberBlaze(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityEmberBlaze.TEXTURES);
    }

    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 10.0);
        this.setRangedAI(0, 6, 60, 100, 0.0F);
        this.getSpecialData().armor += 10;
    }

    /// Overridden to modify attack effects.
    @Override
	protected void onTypeAttack(Entity target) {
    	if (target instanceof EntityLivingBase) {
    		((EntityLivingBase) target).setHealth(((EntityLivingBase) target).getHealth() - 2.0F);
    	}
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (hit) {
	        for (int i = this.rand.nextInt(2 + looting); i-- > 0;) {
	            this.dropItem(Items.coal, 1);
	        }
        }
    }
}