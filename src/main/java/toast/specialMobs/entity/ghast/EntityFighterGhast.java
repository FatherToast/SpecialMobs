package toast.specialMobs.entity.ghast;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityFighterGhast extends EntityMeleeGhast
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "ghast/fighter.png"),
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "ghast/fighter_shooting.png")
    };

    public EntityFighterGhast(World world) {
        super(world);
        this.setSize(2.0F, 2.0F);
        this.getSpecialData().setTextures(EntityFighterGhast.TEXTURES);
        this.getSpecialData().resetRenderScale(0.5F);
        this.experienceValue += 2;
    }

    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 20.0);
        this.getSpecialData().addAttribute(SharedMonsterAttributes.attackDamage, 4.0);
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 1.3);
        this.getSpecialData().armor += 6;
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
            this.dropItem(Items.iron_ingot, 1);
        }
    }
}