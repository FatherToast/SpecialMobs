package toast.specialMobs.entity.silverfish;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityToughSilverfish extends Entity_SpecialSilverfish
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "silverfish/tough.png")
    };

    public EntityToughSilverfish(World world) {
        super(world);
        this.setSize(0.6F, 0.9F);
        this.getSpecialData().setTextures(EntityToughSilverfish.TEXTURES);
        this.getSpecialData().resetRenderScale(1.5F);
        this.experienceValue += 2;
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 16.0);
        this.getSpecialData().addAttribute(SharedMonsterAttributes.attackDamage, 2.0);
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 0.7);
        this.getSpecialData().armor += 20;
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        for (int i = this.rand.nextInt(2 + looting); i-- > 0;) {
            this.dropItem(Items.flint, 1);
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        this.dropItem(Items.iron_ingot, 1);
    }
}