package fathertoast.specialmobs.common.entity.ai.goal;

import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.ai.IAmmoUser;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumSet;
import java.util.List;
import java.util.function.BiPredicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ChargeCreeperGoal<T extends MobEntity & IAmmoUser> extends Goal {
    
    private final BiPredicate<T, ? super CreeperEntity> targetPredicate;
    
    private final T madman;
    private final double movementSpeed;
    private final double targetRange;
    
    /** The creeper to target for power-up injection */
    private CreeperEntity creeper;
    
    private int pathUpdateCooldown;
    private Vector3d pathTarget = Vector3d.ZERO;
    private boolean canUseWhileMounted = false;
    
    public ChargeCreeperGoal( T madman, double movementSpeed, double targetRange, BiPredicate<T, ? super CreeperEntity> targetPredicate ) {
        this.madman = madman;
        this.movementSpeed = movementSpeed;
        this.targetRange = targetRange;
        this.targetPredicate = targetPredicate;
        this.setFlags( EnumSet.of( Flag.MOVE ) );
    }
    
    /** Builder that enables the entity to leap while mounted. */
    public ChargeCreeperGoal<T> canUseWhileMounted() {
        canUseWhileMounted = true;
        return this;
    }
    
    /** @return Returns true if this AI can be activated. */
    @Override
    public boolean canUse() {
        if( !madman.hasAmmo() || madman.isPassenger() || !canUseWhileMounted && madman.isVehicle() ) return false;
        
        findCreeper();
        if( creeper == null ) return false;
        
        madman.getNavigation().moveTo( creeper, movementSpeed );
        pathTarget = creeper.position();
        pathUpdateCooldown = 4 + madman.getRandom().nextInt( 7 );
        return madman.getNavigation().getPath() != null;
    }
    
    private void findCreeper() {
        World world = madman.level;
        List<CreeperEntity> nearbyCreepers = world.getLoadedEntitiesOfClass( CreeperEntity.class, madman.getBoundingBox().inflate( targetRange ), null );
        
        if( !nearbyCreepers.isEmpty() ) {
            for( CreeperEntity creeper : nearbyCreepers ) {
                if( targetPredicate.test( madman, creeper ) ) {
                    this.creeper = creeper;
                    break;
                }
            }
        }
    }
    
    /** @return Called each update while active and returns true if this AI can remain active. */
    @Override
    public boolean canContinueToUse() {
        return !madman.isPassenger() && (canUseWhileMounted || madman.isVehicle()) && creeper != null && targetPredicate.test( madman, creeper );
    }
    
    /** Called each tick while this AI is active. */
    @Override
    public void tick() {
        if( creeper == null ) return;
        
        final double distanceSq = madman.distanceToSqr( creeper );
        
        pathUpdateCooldown--;
        if( pathUpdateCooldown <= 0 && (creeper.distanceToSqr( pathTarget ) >= 1.0 || madman.getRandom().nextFloat() < 0.05F) ) {
            pathUpdateCooldown = 4 + madman.getRandom().nextInt( 7 );
            if( distanceSq > 1024.0 ) pathUpdateCooldown += 10;
            else if( distanceSq > 256.0 ) pathUpdateCooldown += 5;
            
            if( !madman.getNavigation().moveTo( creeper, movementSpeed ) ) pathUpdateCooldown += 15;
            pathTarget = creeper.position();
        }
        
        madman.getLookControl().setLookAt( creeper, 30.0F, 30.0F );
        if( distanceSq < 2.5 ) {
            if( madman.hasAmmo() ) {
                madman.consumeAmmo();
                MobHelper.charge( creeper );
                madman.level.playSound( null, creeper.getX(), creeper.getY(), creeper.getZ(),
                        SoundEvents.BEE_STING, SoundCategory.HOSTILE, 0.9F, 1.0F );
            }
            
            creeper = null;
        }
    }
}