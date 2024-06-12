package fathertoast.specialmobs.common.entity.ai.goal;

import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.ai.IAmmoUser;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;
import java.util.function.BiPredicate;

public class ChargeCreeperGoal<T extends Mob & IAmmoUser> extends Goal {
    
    private final BiPredicate<T, ? super Creeper> targetPredicate;
    
    private final T madman;
    private final double movementSpeed;
    private final double targetRange;
    
    /** The creeper to target for power-up injection */
    private Creeper creeper;
    
    private int pathUpdateCooldown;
    private Vec3 pathTarget = Vec3.ZERO;
    private boolean canUseWhileMounted = false;
    
    public ChargeCreeperGoal( T madman, double movementSpeed, double targetRange, BiPredicate<T, ? super Creeper> targetPredicate ) {
        this.madman = madman;
        this.movementSpeed = movementSpeed;
        this.targetRange = targetRange;
        this.targetPredicate = targetPredicate;
        this.setFlags( EnumSet.of( Flag.MOVE ) );
    }
    
    /** Builder that enables the entity to leap while mounted. */
    @SuppressWarnings( "unused" )
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
        Level level = madman.level();
        List<Creeper> nearbyCreepers = level.getEntitiesOfClass( Creeper.class, madman.getBoundingBox().inflate( targetRange ) );
        
        if( !nearbyCreepers.isEmpty() ) {
            for( Creeper creeper : nearbyCreepers ) {
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
                madman.level().playSound( null, creeper.getX(), creeper.getY(), creeper.getZ(),
                        SoundEvents.BEE_STING, SoundSource.HOSTILE, 0.9F, 1.0F );
            }
            
            creeper = null;
        }
    }
}