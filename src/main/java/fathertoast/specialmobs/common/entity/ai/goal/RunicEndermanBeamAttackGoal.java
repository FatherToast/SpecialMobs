package fathertoast.specialmobs.common.entity.ai.goal;

import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.enderman.RunicEndermanEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class RunicEndermanBeamAttackGoal extends Goal {
    private final RunicEndermanEntity mob;
    private final DamageSource beamDamageSource;
    
    private LivingEntity targetEntity;
    private Vector3d targetPos;
    private int attackTime;
    
    public RunicEndermanBeamAttackGoal( RunicEndermanEntity entity ) {
        mob = entity;
        beamDamageSource = new EntityDamageSource( DamageSource.MAGIC.getMsgId(), entity );
        setFlags( EnumSet.of( Goal.Flag.MOVE, Flag.LOOK ) );
    }
    
    /** @return Returns true if this AI can be activated. */
    @Override
    public boolean canUse() {
        if( mob.isVehicle() ) return false;
        
        final LivingEntity target = mob.getTarget();
        if( target != null && target.isAlive() && mob.getSensing().canSee( target ) &&
                mob.tickCount % 8 == 0 && mob.getRandom().nextInt( 10 ) == 0 && target.distanceToSqr( mob ) <=
                mob.getSpecialData().getRangedAttackMaxRange() * mob.getSpecialData().getRangedAttackMaxRange() ) {
            
            targetEntity = target;
            updateTargetPos();
            attackTime = 0;
            
            mob.setBeamState( RunicEndermanEntity.BeamState.CHARGING );
            if( !mob.isSilent() ) {
                mob.level.playSound( null, mob.getX(), mob.getEyeY(), mob.getZ(),
                        SoundEvents.ENDERMAN_SCREAM, mob.getSoundSource(),
                        1.0F, 1.0F / (mob.getRandom().nextFloat() * 0.2F + 1.8F) );
            }
            return true;
        }
        return false;
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
        switch( mob.getBeamState() ) {
            case CHARGING:
                chargingTick();
                break;
            case DAMAGING:
                damagingTick();
                break;
        }
    }
    
    /** Called each tick while this AI is active and in the charging phase. */
    private void chargingTick() {
        mob.getLookControl().setLookAt( targetPos.x, targetPos.y, targetPos.z,
                30.0F, mob.getMaxHeadXRot() );
        
        if( attackTime >= mob.getSpecialData().getRangedAttackCooldown() ) {
            mob.setBeamState( RunicEndermanEntity.BeamState.DAMAGING );
        }
    }
    
    /** Called each tick while this AI is active and in the damaging phase. */
    private void damagingTick() {
        if( targetEntity != null ) updateTargetPos();
        
        if( !mob.isSilent() && attackTime % 10 == 0 ) {
            mob.level.playSound( null, mob.getX(), mob.getEyeY(), mob.getZ(),
                    SoundEvents.ENDERMAN_SCREAM, mob.getSoundSource(),
                    1.0F, 1.0F / (mob.getRandom().nextFloat() * 0.4F + 1.0F) );
        }
        
        lookTowardTarget( 1.7F * mob.getSpecialData().getRangedWalkSpeed() ); // Use move speed as a turn rate modifier
        final Vector3d viewVec = mob.getViewVector( 1.0F ).scale( RunicEndermanEntity.BEAM_MAX_RANGE );
        
        final Vector3d beamStartPos = mob.getEyePosition( 1.0F );
        Vector3d beamEndPos = beamStartPos.add( viewVec );
        
        final RayTraceResult blockRayTrace = mob.level.clip(
                new RayTraceContext( beamStartPos, beamEndPos,
                        RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, mob ) );
        if( blockRayTrace.getType() != RayTraceResult.Type.MISS ) {
            beamEndPos = blockRayTrace.getLocation();
        }
        
        final List<Entity> hitEntities = rayTraceEntities( beamStartPos, beamEndPos,
                mob.getBoundingBox().expandTowards( viewVec ).inflate( 1.0 ) );
        for( Entity entity : hitEntities ) {
            if( entity instanceof EndermanEntity || entity instanceof EnderDragonEntity ) {
                if( mob.tickCount % 10 == 0 ) ((MobEntity) entity).heal( 1.0F );
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
        targetPos = new Vector3d( targetEntity.getX(), targetEntity.getY( 0.5 ), targetEntity.getZ() );
    }
    
    /** Sets the target look position. Modifies the target position to limit vertical look speed. */
    private void lookTowardTarget( float speed ) {
        final double dX = targetPos.x - mob.getX();
        final double dY = targetPos.y - mob.getEyeY();
        final double dZ = targetPos.z - mob.getZ();
        final double dH = MathHelper.sqrt( dX * dX + dZ * dZ );
        final float targetXRot = (float) MathHelper.atan2( dY, dH );
        
        final float clampedXRot = (mob.xRot + MathHelper.clamp( MathHelper.degreesDifference( mob.xRot,
                targetXRot * -57.2957763671875F ), -speed, speed )) / -57.2957763671875F;
        final double clampedDY = dH * MathHelper.sin( clampedXRot ) / MathHelper.cos( clampedXRot );
        targetPos = targetPos.add( 0.0, clampedDY - dY, 0.0 );
        
        mob.getLookControl().setLookAt( targetPos.x, targetPos.y, targetPos.z,
                speed, mob.getMaxHeadXRot() );
    }
    
    /** @return A list of all entities between the two vector positions and within the search area, in no particular order. */
    private List<Entity> rayTraceEntities( Vector3d from, Vector3d to, AxisAlignedBB searchArea ) {
        final List<Entity> entitiesHit = new ArrayList<>();
        for( Entity entity : mob.level.getEntities( mob, searchArea, this::canBeamHitTarget ) ) {
            final Optional<Vector3d> hitPos = entity.getBoundingBox().inflate( 0.3 ).clip( from, to );
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