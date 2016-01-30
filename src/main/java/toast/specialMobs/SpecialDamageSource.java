package toast.specialMobs;

import net.minecraft.entity.Entity;
import net.minecraft.util.EntityDamageSourceIndirect;

/**
 * An ordinary damage source, only with all methods made public.
 * Why were they not in the first place?
 */
public class SpecialDamageSource extends EntityDamageSourceIndirect
{
    // This damage source's hunger damage.
    private float hungerDamage = 0.3F;

    public SpecialDamageSource(String damageType, Entity directEntity, Entity indirectEntity){
        super(damageType, directEntity, indirectEntity);
    }

    // Sets the stamina damage.
    public SpecialDamageSource setHungerDamage(float val) {
        this.hungerDamage = val;
        return this;
    }

    // How much stamina is consumed by this damage source.
    @Override
    public float getHungerDamage() {
        return this.hungerDamage;
    }

    // Allows this damage source to damage invulnerable entities.
    @Override
    public SpecialDamageSource setDamageAllowedInCreativeMode() {
        super.setDamageAllowedInCreativeMode();
        return this;
    }

    // Causes the damage type to be considered fire damage.
    @Override
    public SpecialDamageSource setFireDamage() {
        super.setFireDamage();
        return this;
    }

    // Define the damage type as projectile based.
    @Override
    public SpecialDamageSource setProjectile() {
        super.setProjectile();
        return this;
    }

    // Define the damage type as explosion based.
    @Override
    public SpecialDamageSource setExplosion() {
        super.setExplosion();
        return this;
    }

    // Define the damage type as magic based.
    @Override
    public SpecialDamageSource setMagicDamage() {
        super.setMagicDamage();
        return this;
    }

    // Define the damage type to bypass armor.
    @Override
    public SpecialDamageSource setDamageBypassesArmor() {
        super.setDamageBypassesArmor();
        return this;
    }

    // Set whether this damage source will have its damage amount scaled based on the current difficulty.
    @Override
    public SpecialDamageSource setDifficultyScaled() {
        super.setDifficultyScaled();
        return this;
    }
}