package fathertoast.specialmobs.common.entity.ai.goal;

import fathertoast.specialmobs.common.entity.misc.MobBoat;
import fathertoast.specialmobs.common.entity.skeleton.PirateSkeletonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.Fluids;

import java.util.EnumSet;
import java.util.List;

/**
 * Makes the pirate try to mount a nearby boat,
 * or spawn one if possible.
 */
@SuppressWarnings("resource")
public class PirateSpawnBoatGoal extends Goal {

    private final PirateSkeletonEntity pirate;
    private int heightLevel = -1;


    public PirateSpawnBoatGoal(PirateSkeletonEntity pirate ) {
        this.pirate = pirate;
        setFlags( EnumSet.of( Flag.MOVE ) );
    }


    @Override
    public boolean canUse() {
        return pirate.getVehicle() == null && pirate.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse() && heightLevel < 1;
    }

    @Override
    public void start() {
        heightLevel = -1;
    }

    @Override
    public void tick() {
        List<Boat> nearbyBoats = pirate.level().getEntitiesOfClass( Boat.class, pirate.getBoundingBox().inflate( 1.0D ) );

        // Check if there is a boat real close by to mount
        if ( !nearbyBoats.isEmpty() ) {
            for ( Boat boat : nearbyBoats ) {
                if ( !boat.hasControllingPassenger() ) {
                    pirate.startRiding( boat );
                    return;
                }
            }
        }
        // No boat close by, try spawning one and mount it
        if ( pirate.getEntityData().get( PirateSkeletonEntity.SPAWNED_BOAT ) ) {
            return;
        }
        final BlockPos pos = pirate.blockPosition();
        Holder<Biome> biome = pirate.level().getBiome( pos );
        boolean isEligibleBiome = biome.is( BiomeTags.IS_OCEAN ) || biome.is( BiomeTags.IS_RIVER ) || biome.is( BiomeTags.IS_BEACH );
        boolean shouldIReallyPlaceABoatHereItIsNotVerySpaciousOrALotOfWaterHereIShouldConsiderMountingMyBoatSomewhereElse = true;

        for ( BlockPos pos1 : BlockPos.betweenClosed( pos.offset( 2, heightLevel, 2 ), pos.offset( -2, heightLevel, -2 ) ) ) {
            if ( !( pirate.level().getFluidState( pos1 ).is( Fluids.WATER ) && pirate.level().getBlockState( pos1.above() ).isAir() ) )
                shouldIReallyPlaceABoatHereItIsNotVerySpaciousOrALotOfWaterHereIShouldConsiderMountingMyBoatSomewhereElse = false;

        }
        if ( isEligibleBiome && shouldIReallyPlaceABoatHereItIsNotVerySpaciousOrALotOfWaterHereIShouldConsiderMountingMyBoatSomewhereElse ) {
            if ( nearbyBoats.isEmpty() ) {
                MobBoat boat = new MobBoat( pirate.level(), pirate.getX(), pirate.getY() + (heightLevel + 1), pirate.getZ() );
                boat.setVariant( Boat.Type.values()[pirate.level().random.nextInt(Boat.Type.values().length)] );

                pirate.level().addFreshEntity( boat );
                pirate.startRiding( boat );
                pirate.getEntityData().set( PirateSkeletonEntity.SPAWNED_BOAT, true );

                return;
            }
        }
        ++heightLevel;
    }
}