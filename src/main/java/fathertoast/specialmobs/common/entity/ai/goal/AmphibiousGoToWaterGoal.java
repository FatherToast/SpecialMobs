package fathertoast.specialmobs.common.entity.ai.goal;

import net.minecraft.block.Blocks;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Random;

/**
 * The drowned "go to water" goal repurposed for use on other mobs.
 * <p>
 * {@link net.minecraft.entity.monster.DrownedEntity.GoToWaterGoal}
 */
public class AmphibiousGoToWaterGoal extends Goal {
    
    private final MobEntity mob;
    private final double speedModifier;
    
    private boolean disableAtNight = true;
    
    private double wantedX;
    private double wantedY;
    private double wantedZ;
    
    public AmphibiousGoToWaterGoal( MobEntity entity, double speed ) {
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
        if( disableAtNight && !mob.level.isDay() || mob.isInWater() ) return false;
        
        final Vector3d targetPos = findWaterPos();
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
    private Vector3d findWaterPos() {
        final Random random = mob.getRandom();
        final BlockPos origin = mob.blockPosition();
        
        for( int i = 0; i < 10; i++ ) {
            final BlockPos target = origin.offset(
                    random.nextInt( 20 ) - 10,
                    2 - random.nextInt( 8 ),
                    random.nextInt( 20 ) - 10 );
            if( mob.level.getBlockState( target ).is( Blocks.WATER ) ) {
                return Vector3d.atBottomCenterOf( target );
            }
        }
        return null;
    }
}