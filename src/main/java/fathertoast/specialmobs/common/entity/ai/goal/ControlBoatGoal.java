package fathertoast.specialmobs.common.entity.ai.goal;

import fathertoast.specialmobs.common.entity.misc.MobBoat;
import fathertoast.specialmobs.common.entity.skeleton.PirateSkeletonEntity;
import fathertoast.specialmobs.common.network.NetworkHelper;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * A goal that allows mobs to control a {@link MobBoat} to navigate
 * to their current target.<br><br>
 * If the distance to the mob's target is greater
 * or equal to {@link ControlBoatGoal#sqrStartDist}, the goal will try to run.
 * If the goal is currently running and the distance to the target is now lesser
 * or equal to {@link ControlBoatGoal#sqrStopDist}, the goal will stop running.
 */
public class ControlBoatGoal extends Goal {


    private final Mob mob;
    /** Required squared distance to target before this goal can run. */
    private final double sqrStartDist;
    /** The squared distance to target for when this goal should stop. */
    private final double sqrStopDist;


    public ControlBoatGoal( Mob mob, double sqrStartDist, double sqrStopDist ) {
        this.mob = mob;
        this.sqrStartDist = sqrStartDist;
        this.sqrStopDist = sqrStopDist;
        setFlags( EnumSet.of( Flag.MOVE ) );
    }


    @Override
    public boolean canUse() {
        return mob.isAlive() && mob.getTarget() != null && mob.distanceToSqr( mob.getTarget() ) >= sqrStartDist && mob.getVehicle() instanceof MobBoat;
    }

    @Override
    public boolean canContinueToUse() {
        return mob.isAlive() && mob.getTarget() != null && mob.distanceToSqr( mob.getTarget() ) > sqrStopDist && mob.getVehicle() instanceof MobBoat;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void stop() {
        // Make sure the boat actually stops so
        // the mob doesn't sail into infinity on the client
        if ( mob.getVehicle() instanceof MobBoat boat ) {
            boat.setInput( false, false, false, false );
            NetworkHelper.sendUpdateBoatInputs( boat, false, false, false, false );
        }
    }

    @Override
    public void tick() {
        if ( mob.getTarget() == null || mob.getVehicle() == null ) return;

        MobBoat boat = ( MobBoat ) mob.getVehicle();

        if ( boat.isUnderWater() ) {
            // ABANDON SHIP!!
            Vec3 dismountLoc = boat.getDismountLocationForPassenger( mob );
            mob.dismountTo( dismountLoc.x, dismountLoc.y,dismountLoc.z );
            return;
        }
        // Control boat
        Vec3 boatPosition = boat.position();
        Vec3 boatViewVec = boat.getViewVector(0.0F).normalize();
        Vec3 targetVec = mob.getTarget().position().subtract(boatPosition).normalize();

        double deviation = boatViewVec.x * targetVec.z - boatViewVec.z * targetVec.x;

        boolean left = ( deviation < 0.2 );
        boolean right = ( deviation > -0.2 ) ;

        boat.setInput( left, right, true, false );
        NetworkHelper.sendUpdateBoatInputs( boat, true, false, left, right );
    }
}
