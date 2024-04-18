package fathertoast.specialmobs.common.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * The drowned "go to water" goal repurposed for use on other mobs.
 * <p>
 * {@link net.minecraft.world.entity.monster.Drowned.DrownedGoToWaterGoal}
 */
@SuppressWarnings("JavadocReference")
public class AmphibiousGoToWaterGoal extends Goal {
    
    private final Mob mob;
    private final double speedModifier;
    
    private boolean disableAtNight = true;
    
    private double wantedX;
    private double wantedY;
    private double wantedZ;
    
    public AmphibiousGoToWaterGoal( Mob entity, double speed ) {
        mob = entity;
        speedModifier = speed;
        setFlags( EnumSet.of( Goal.Flag.MOVE ) );
    }
    
    /** Builder that allows this goal to run during the night. */
    public AmphibiousGoToWaterGoal alwaysEnabled() {
        disableAtNight = false;
        return this;
    }
    
    /** @return Returns true if this AI can be activated. */
    @Override
    public boolean canUse() {
        if( disableAtNight && !mob.level().isDay() || mob.isInWater() ) return false;
        
        final Vec3 targetPos = findWaterPos();
        if( targetPos == null ) return false;
        
        wantedX = targetPos.x;
        wantedY = targetPos.y;
        wantedZ = targetPos.z;
        return true;
    }
    
    /** @return Called each update while active and returns true if this AI can remain active. */
    @Override
    public boolean canContinueToUse() { return !mob.getNavigation().isDone(); }
    
    /** Called when this AI is activated. */
    @Override
    public void start() { mob.getNavigation().moveTo( wantedX, wantedY, wantedZ, speedModifier ); }
    
    /** @return A random nearby position of water, or null if none is found after a few tries. */
    @Nullable
    private Vec3 findWaterPos() {
        final RandomSource random = mob.getRandom();
        final BlockPos origin = mob.blockPosition();
        
        for( int i = 0; i < 10; i++ ) {
            final BlockPos target = origin.offset(
                    random.nextInt( 20 ) - 10,
                    2 - random.nextInt( 8 ),
                    random.nextInt( 20 ) - 10 );
            if( mob.level().getBlockState( target ).is( Blocks.WATER ) ) {
                return Vec3.atBottomCenterOf( target );
            }
        }
        return null;
    }
}