package fathertoast.specialmobs.common.entity.ai;

/**
 * Represents a mob that utilizes {@link fathertoast.specialmobs.common.entity.ai.goal.ControlBoatGoal}
 * Mobs using the boat control goal are not required to implement this, but the MobBoat
 * checks if the rider extends this interface to despawn itself when the rider despawns.
 */
public interface IBoatRider {
}
