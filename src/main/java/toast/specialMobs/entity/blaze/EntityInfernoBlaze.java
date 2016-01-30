package toast.specialMobs.entity.blaze;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityInfernoBlaze extends Entity_SpecialBlaze
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "blaze/inferno.png")
    };

    public EntityInfernoBlaze(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityInfernoBlaze.TEXTURES);
        this.experienceValue += 4;
    }

    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 10.0);
        this.setRangedAI(12, 2, 80, 140, 20.0F);
        this.getSpecialData().arrowSpread *= 2.0F;
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (hit) {
	        for (int i = this.rand.nextInt(2 + looting); i-- > 0;) {
	            this.dropItem(Items.fire_charge, 1);
	        }
            for (int i = this.rand.nextInt(3 + looting); i-- > 0;) {
                this.dropItem(Items.blaze_powder, 1);
            }
        }
    }
}