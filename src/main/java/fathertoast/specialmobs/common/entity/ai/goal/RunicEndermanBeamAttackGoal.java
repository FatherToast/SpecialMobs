package fathertoast.specialmobs.common.entity.ai.goal;

import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.enderman.RunicEndermanEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class RunicEndermanBeamAttackGoal extends Goal {
    private final RunicEndermanEntity mob;
    private final DamageSource beamDamageSource;
    
    private LivingEntity targetEntity;
    private Vec3 targetPos;
    private int attackTime;
    
    public RunicEndermanBeamAttackGoal( RunicEndermanEntity entity ) {
        mob = entity;
        beamDamageSource = entity.damageSources().indirectMagic(entity, null);
        setFlags( EnumSet.of( Goal.Flag.MOVE, Flag.LOOK ) );
    }
    
    /** @return Returns true if this AI can be activated. */
    @Override
    public boolean canUse() {
        if( mob.isVehicle() ) return false;
        
        final LivingEntity target = mob.getTarget();
        if( target != null && target.isAlive() && mob.getSensing().hasLineOfSight( target ) &&
                mob.tickCount % 8 <= 1 && mob.getRandom().nextInt( 10 ) == 0 && isTargetWithinBeamRange( target ) ) {
            
            targetEntity = target;
            updateTargetPos();
            attackTime = 0;
            
            mob.setBeamState( RunicEndermanEntity.BeamState.CHARGING );
            if( !mob.isSilent() ) {
                mob.level().playSound( null, mob.getX(), mob.getEyeY(), mob.getZ(),
                        SoundEvents.ENDERMAN_SCREAM, mob.getSoundSource(),
                        1.0F, 1.0F / (mob.getRandom().nextFloat() * 0.2F + 1.8F) );
            }
            return true;
        }
        return false;
    }

    private boolean isTargetWithinBeamRange( LivingEntity target ) {
        return target.distanceToSqr( mob ) <=
                mob.getSpecialData().getRangedAttackMaxRange() * mob.getSpecialData().getRangedAttackMaxRange();
    }
    
    /** @return Called each update while active and returns true if this AI can remain active. */
    @Override
    public boolean canContinueToUse() {
        return (mob.getBeamState() == RunicEndermanEntity.BeamState.DAMAGING || targetEntity != null && targetEntity.isAlive()) &&
                attackTime < mob.getSpecialData().getRangedAttackMaxCooldown();
    }
    
    /** Called when this AI is deactivated. */
    @Override
    public void stop() {
        targetEntity = null;
        targetPos = null;
        mob.setBeamState( RunicEndermanEntity.BeamState.OFF );
    }
    
    /** Called each tick while this AI is active. */
    @Override
    public void tick() {
        attackTime++;
        switch (mob.getBeamState()) {
            case CHARGING -> chargingTick();
            case DAMAGING -> damagingTick();
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    /** Called each tick while this AI is active and in the charging phase. */
    private void chargingTick() {
        mob.getLookControl().setLookAt( targetPos.x, targetPos.y, targetPos.z,
                10.0F, mob.getMaxHeadXRot() );
        
        if( attackTime >= mob.getSpecialData().getRangedAttackCooldown() ) {
            mob.setBeamState( RunicEndermanEntity.BeamState.DAMAGING );
        }
    }
    
    /** Called each tick while this AI is active and in the damaging phase. */
    private void damagingTick() {
        if( targetEntity != null ) updateTargetPos();
        
        if( !mob.isSilent() && attackTime % 10 == 0 ) {
            mob.level().playSound( null, mob.getX(), mob.getEyeY(), mob.getZ(),
                    SoundEvents.ENDERMAN_SCREAM, mob.getSoundSource(),
                    1.0F, 1.0F / (mob.getRandom().nextFloat() * 0.4F + 1.0F) );
        }
        
        lookTowardTarget( 1.7F * mob.getSpecialData().getRangedWalkSpeed() ); // Use move speed as a turn rate modifier
        final Vec3 viewVec = mob.getViewVector( 1.0F ).scale( RunicEndermanEntity.BEAM_MAX_RANGE );
        
        final Vec3 beamStartPos = mob.getEyePosition( 1.0F );
        Vec3 beamEndPos = beamStartPos.add( viewVec );
        
        final HitResult blockRayTrace = mob.level().clip(
                new ClipContext( beamStartPos, beamEndPos,
                        ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob ) );
        if( blockRayTrace.getType() != HitResult.Type.MISS ) {
            beamEndPos = blockRayTrace.getLocation();
        }
        
        final List<Entity> hitEntities = rayTraceEntities( beamStartPos, beamEndPos,
                mob.getBoundingBox().expandTowards( viewVec ).inflate( 1.0 ) );
        for( Entity entity : hitEntities ) {
            if( entity instanceof EnderMan || entity instanceof EnderDragon) {
                if( mob.tickCount % 10 == 0 ) ((Mob) entity).heal( 1.0F );
            }
            else if( entity.hurt( beamDamageSource, mob.getSpecialData().getRangedAttackDamage() ) &&
                    entity instanceof LivingEntity ) {
                MobHelper.knockback( (LivingEntity) entity, 1.0F, 1.0F,
                        -viewVec.x, -viewVec.z, 1.0 );
            }
        }
    }
    
    /** Updates the target position based on an entity we want to hit. */
    private void updateTargetPos() {
        targetPos = new Vec3( targetEntity.getX(), targetEntity.getY( 0.5 ), targetEntity.getZ() );
    }
    
    /** Sets the target look position. Modifies the target position to limit vertical look speed. */
    private void lookTowardTarget( float speed ) {
        final double dX = targetPos.x - mob.getX();
        final double dY = targetPos.y - mob.getEyeY();
        final double dZ = targetPos.z - mob.getZ();
        final double dH = Mth.sqrt( (float) (dX * dX + dZ * dZ) );
        final float targetXRot = (float) Mth.atan2( dY, dH );
        
        final float clampedXRot = (mob.getXRot() + Mth.clamp( Mth.degreesDifference( mob.getXRot(),
                targetXRot * -57.2957763671875F ), -speed, speed )) / -57.2957763671875F;
        final double clampedDY = dH * Mth.sin( clampedXRot ) / Mth.cos( clampedXRot );
        targetPos = targetPos.add( 0.0, clampedDY - dY, 0.0 );
        
        mob.getLookControl().setLookAt( targetPos.x, targetPos.y, targetPos.z,
                speed, mob.getMaxHeadXRot() );
    }
    
    /** @return A list of all entities between the two vector positions and within the search area, in no particular order. */
    private List<Entity> rayTraceEntities( Vec3 from, Vec3 to, AABB searchArea ) {
        final List<Entity> entitiesHit = new ArrayList<>();
        for( Entity entity : mob.level().getEntities( mob, searchArea, this::canBeamHitTarget ) ) {
            final Optional<Vec3> hitPos = entity.getBoundingBox().inflate( 0.3 ).clip( from, to );
            if( hitPos.isPresent() ) {
                entitiesHit.add( entity );
            }
        }
        return entitiesHit;
    }
    
    /** @return True if the beam can hit the target. */
    private boolean canBeamHitTarget( @Nullable Entity entity ) {
        return entity != null && !entity.isSpectator() && entity.isAlive() && entity.isPickable() &&
                !entity.isPassengerOfSameVehicle( mob );
    }
}