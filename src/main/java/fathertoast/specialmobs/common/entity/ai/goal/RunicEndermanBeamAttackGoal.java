package fathertoast.specialmobs.common.entity.ai.goal;

import fathertoast.specialmobs.common.entity.enderman.RunicEndermanEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;

public class RunicEndermanBeamAttackGoal extends Goal {

    private final RunicEndermanEntity runicEnderman;
    private final int maxAttackTime;
    private final double baseDamage;

    private int attackTime;

    public RunicEndermanBeamAttackGoal(RunicEndermanEntity runicEnderman, int maxAttackTime, double baseDamage ) {
        this.runicEnderman = runicEnderman;
        this.maxAttackTime = maxAttackTime;
        this.baseDamage = baseDamage;
    }

    @Override
    public boolean canUse() {
        if (runicEnderman.isVehicle())
            return false;

        LivingEntity target = runicEnderman.getTarget();

        return target != null
                && target.isAlive()
                && runicEnderman.getSensing().canSee(target)
                && (runicEnderman.tickCount % 8 == 0 && runicEnderman.getRandom().nextInt(10) == 0);
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = runicEnderman.getTarget();

        return attackTime > 0 && target != null && target.isAlive() && runicEnderman.getSensing().canSee(target);
    }

    @Override
    public void start() {
        runicEnderman.setBeamTargetId(runicEnderman.getTarget().getId());
        attackTime = maxAttackTime;
    }

    @Override
    public void stop() {
        runicEnderman.clearBeamTargetId();
        attackTime = 0;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void tick() {
        --attackTime;

        if (attackTime <= 0) {
            LivingEntity target = runicEnderman.getTarget();
            double xRatio = MathHelper.sin(runicEnderman.yRot * ((float)Math.PI / 180F));
            double zRatio = -MathHelper.cos(runicEnderman.yRot * ((float)Math.PI / 180F));
            target.knockback(5, xRatio, zRatio);
            target.hurt(DamageSource.mobAttack(runicEnderman), (float) baseDamage);
        }
    }
}
