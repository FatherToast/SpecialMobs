package toast.specialMobs.entity.blaze;

import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntitySmolderBlaze extends Entity_SpecialBlaze
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "blaze/smolder.png")
    };

    public EntitySmolderBlaze(World world) {
        super(world);
        this.getSpecialData().setTextures(EntitySmolderBlaze.TEXTURES);
    }

    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        this.getSpecialData().armor += 10;
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (hit) {
	        for (int i = this.rand.nextInt(3 + looting); i-- > 0;) {
	            this.dropItem(Items.coal, 1);
	        }
        }
    }
}