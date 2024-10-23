package fathertoast.specialmobs.common.entity.ai.goal;

import fathertoast.specialmobs.common.entity.misc.MobBoat;
import fathertoast.specialmobs.common.entity.skeleton.PirateSkeletonEntity;
import fathertoast.specialmobs.common.network.NetworkHelper;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public class PirateControlBoatGoal extends Goal {

    private final PirateSkeletonEntity pirate;


    public PirateControlBoatGoal( PirateSkeletonEntity pirate ) {
        this.pirate = pirate;
    }


    @Override
    public boolean canUse() {
        return pirate.isAlive() && pirate.getTarget() != null && pirate.distanceToSqr( pirate.getTarget() ) > 512.0D && pirate.getVehicle() instanceof MobBoat;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void stop() {
        if ( pirate.getVehicle() instanceof MobBoat boat ) {
            NetworkHelper.sendUpdateBoatInputs( boat, false, false, false, false );
        }
    }

    @Override
    public void tick() {
        MobBoat boat = ( MobBoat ) pirate.getVehicle();

        if ( boat.isUnderWater() ) {
            // ABANDON SHIP!!
            Vec3 dismountLoc = boat.getDismountLocationForPassenger( pirate );
            pirate.dismountTo( dismountLoc.x, dismountLoc.y,dismountLoc.z );
            return;
        }
        boolean right = false;
        boolean left = false;

        if ( pirate.getXRot() > pirate.xRotO ) {
            right = true;
        }
        else if ( pirate.getXRot() < pirate.xRotO ) {
            left = true;
        }
        NetworkHelper.sendUpdateBoatInputs( boat, true, false, left, right );
    }
}
