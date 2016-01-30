package toast.specialMobs.entity.skeleton;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntitySniperSkeleton extends Entity_SpecialSkeleton
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "skeleton/sniper.png"),
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "skeleton/sniper_wither.png")
    };

    public EntitySniperSkeleton(World world) {
        super(world);
        this.getSpecialData().setTextures(EntitySniperSkeleton.TEXTURES);
        this.experienceValue += 2;
    }

    /// Override to set the attack AI to use.
    @Override
    protected void initTypeAI() {
        this.setRangedAI(1.0, 26, 80, 23.0F);
        this.setMeleeAI(1.5);
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.followRange, 8.0);
        this.getSpecialData().addAttribute(SharedMonsterAttributes.attackDamage, 2.0);
        this.getSpecialData().arrowDamage += 2.0F;
        this.getSpecialData().arrowSpread -= 4.0F;
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        for (int i = this.rand.nextInt(2 + looting); i-- > 0;) {
            this.dropItem(Items.feather, 1);
        }
    }
}