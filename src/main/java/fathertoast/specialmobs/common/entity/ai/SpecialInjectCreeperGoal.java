package fathertoast.specialmobs.common.entity.ai;

import fathertoast.specialmobs.common.entity.creeper._SpecialCreeperEntity;
import fathertoast.specialmobs.common.entity.zombie.MadScientistZombieEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.List;
import java.util.function.BiPredicate;

public class SpecialInjectCreeperGoal<T extends MadScientistZombieEntity> extends Goal {

    private final BiPredicate<T, ? super CreeperEntity> targetPredicate;

    private final T madman;
    private final double movementSpeed;
    private final double targetRange;

    /** The creeper to target for power-up injection **/
    private CreeperEntity creeper;

    private boolean canUseWhileMounted = false;


    public SpecialInjectCreeperGoal(T madman, double movementSpeed, double targetRange, BiPredicate<T, ? super CreeperEntity> targetPredicate) {
        this.madman = madman;
        this.movementSpeed = movementSpeed;
        this.targetRange = targetRange;
        this.targetPredicate = targetPredicate;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    /** Builder that enables the entity to leap while mounted. */
    public SpecialInjectCreeperGoal<T> canUseWhileMounted() {
        canUseWhileMounted = true;
        return this;
    }

    /** @return Returns true if this AI can be activated. */
    @Override
    public boolean canUse() {
        if( madman.isPassenger() || !canUseWhileMounted && madman.isVehicle() ) return false;
        findCreeper();
        return creeper != null;
    }

    private void findCreeper() {
        World world = madman.level;
        List<CreeperEntity> nearbyCreepers = world.getLoadedEntitiesOfClass(CreeperEntity.class, madman.getBoundingBox().inflate(targetRange), null);

        if (!nearbyCreepers.isEmpty()) {
            for (CreeperEntity creeper : nearbyCreepers) {
                if (targetPredicate.test(madman, creeper)) {
                    this.creeper = creeper;
                    break;
                }
            }
        }
    }

    /** Called when this AI is activated. */
    @Override
    public void start() {
        madman.getNavigation().moveTo(creeper, movementSpeed);
    }

    /** @return Called each update while active and returns true if this AI can remain active. */
    @Override
    public boolean canContinueToUse() {
        return !madman.isOnGround() && !madman.isPassenger() && !madman.isInWaterOrBubble() && !madman.isInLava();
    }

    /** Called each tick while this AI is active. */
    @Override
    public void tick() {
        if (creeper == null || !targetPredicate.test(madman, creeper)) {
            findCreeper();
        }
        else {
            madman.getNavigation().moveTo(creeper, movementSpeed);
            madman.getLookControl().setLookAt(this.creeper.getX(), this.creeper.getEyeY(), this.creeper.getZ());

            if (madman.distanceTo(creeper) < 1.5D) {
                creeper.getEntityData().set(CreeperEntity.DATA_IS_POWERED, true);

                // HEE HEE HEE HAW
                if (creeper instanceof _SpecialCreeperEntity && creeper.level.random.nextDouble() < 0.1F) {
                    ((_SpecialCreeperEntity)creeper).setSupercharged( true );
                }
                madman.level.playSound(null, creeper.getX() + 0.5D, creeper.getY(), creeper.getZ() + 0.5D, SoundEvents.BEE_STING, SoundCategory.HOSTILE, 0.9F, 1.0F);
                creeper = null;
            }
        }
    }
}
