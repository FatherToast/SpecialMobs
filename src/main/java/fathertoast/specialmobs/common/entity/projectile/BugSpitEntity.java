package fathertoast.specialmobs.common.entity.projectile;

import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.core.register.SMEntities;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;

public class BugSpitEntity extends Projectile {
    private static final float DRAG_FACTOR = 0.99F;
    private static final float GRAVITY_ACCEL = 0.06F;
    
    /** The parameter for color tint. */
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId( BugSpitEntity.class, EntityDataSerializers.INT );
    
    private float damageAmount = 2.0F;
    private int knockback;
    
    public BugSpitEntity( EntityType<? extends BugSpitEntity> entityType, Level level ) { super( entityType, level ); }
    
    public BugSpitEntity( LivingEntity shooter, LivingEntity target ) {
        super( SMEntities.BUG_SPIT.get(), shooter.level() );
        setOwner( shooter );
        
        final Vec3 lookVec = shooter.getViewVector( 1.0F ).scale( shooter.getBbWidth() );
        setPos( shooter.getX() + lookVec.x, shooter.getEyeY() - 0.1, shooter.getZ() + lookVec.z );

        float spread = 14 - 4 * level().getDifficulty().getId();
        if(shooter instanceof final ISpecialMob<?> specialShooter) {
            setDamage( specialShooter.getSpecialData().getRangedAttackDamage() );
            
            if( getDamage() < 0.0F ) {
                SpecialMobs.LOG.warn( "Entity is shooting damaging projectile, but has no ranged damage stat! {}", shooter );
                setDamage( 2.0F );
            }
            if( specialShooter.getSpecialData().getRangedAttackSpread() >= 0.0F ) {
                spread *= specialShooter.getSpecialData().getRangedAttackSpread();
            }
            else {
                SpecialMobs.LOG.warn( "Entity is shooting projectile, but has no ranged spread stat! {}", shooter );
            }
        }
        
        final double dX = target.getX() - getX();
        final double dY = target.getY( 0.3333 ) - getY();
        final double dZ = target.getZ() - getZ();
        final double dH = Mth.sqrt( (float) (dX * dX + dZ * dZ) );
        shoot( dX, dY + dH * 0.2, dZ, 1.2F, spread );
    }

    /** Called from the Entity.class constructor to define data watcher variables. */
    @Override
    protected void defineSynchedData() { entityData.define( COLOR, 0xFFFFFF ); }
    
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket( this ); }
    
    /** Sets the RGB color of this spit attack. */
    public void setColor( int color ) {
        entityData.set( COLOR, color );
    }
    
    /** @return The RGB color of this spit attack. */
    public int getColor() { return entityData.get( COLOR ); }
    
    /** Called when the entity is added to the world. */
    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        
        if( level() != null && level().isClientSide() ) {
            final Vec3 v = getDeltaMovement();
            for( int i = 0; i < 7; i++ ) {
                final double multi = 0.4 + 0.1 * i;
                level().addParticle( ParticleTypes.SPIT, getX(), getY(), getZ(),
                        v.x * multi, v.y * multi, v.z * multi );
            }
        }
    }
    
    /** Sets the damage dealt by this projectile. */
    public void setDamage( float amount ) { damageAmount = amount; }
    
    /** @return The damage dealt by this projectile. */
    public float getDamage() { return damageAmount; }
    
    /** Sets the knockback strength this projectile. */
    public void setKnockback( int amount ) { knockback = amount; }
    
    /** @return The knockback strength of this projectile. */
    public int getKnockback() { return knockback; }
    
    /** Called each tick to update this entity. */
    @Override
    public void tick() {
        super.tick();
        
        // Check collision
        final Vec3 v = getDeltaMovement();
        final HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector( this, this::canHitEntity );
        if( hitResult.getType() != HitResult.Type.MISS && !ForgeEventFactory.onProjectileImpact( this, hitResult ) ) {
            onHit( hitResult );
        }
        
        // Update physics
        final double nextX = getX() + v.x;
        final double nextY = getY() + v.y;
        final double nextZ = getZ() + v.z;
        updateRotation();
        if( level().getBlockStates( getBoundingBox() ).noneMatch( BlockBehaviour.BlockStateBase::isAir ) || isInWaterOrBubble() ) {
            // Oof ded (but not a hit)
            discard();
        }
        else {
            setDeltaMovement( v.scale( DRAG_FACTOR ) );
            if( !isNoGravity() ) setDeltaMovement( getDeltaMovement().add( 0.0, -GRAVITY_ACCEL, 0.0 ) );
            
            setPos( nextX, nextY, nextZ );
        }
    }
    
    /** Called when this projectile hits anything. */
    @Override
    protected void onHit( HitResult hit ) {
        super.onHit( hit );
        if( !level().isClientSide() ) {
            // Should we add any on-hit gfx?
            discard();
        }
    }
    
    /** Called when this projectile hits an entity. */
    @Override
    protected void onHitEntity( EntityHitResult hit ) {
        super.onHitEntity( hit );
        final Entity target = hit.getEntity();
        final Entity owner = getOwner();
        if( owner instanceof LivingEntity &&
                target.hurt( damageSources().mobProjectile( this, (LivingEntity) owner ), getDamage() ) ) {
            if( getKnockback() > 0 && target instanceof LivingEntity ) {
                MobHelper.knockback( this, (LivingEntity) target, getKnockback(), 1.0F );
            }
            
            owner.doEnchantDamageEffects( (LivingEntity) owner, target );
            ((LivingEntity) owner).setLastHurtMob( target );
        }
    }
    
    //    /** Called when this projectile hits a block. */
    //    @Override
    //    protected void onHitBlock( BlockRayTraceResult hit ) {
    //        super.onHitBlock( hit );
    //    }
    
    /** Saves data to this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void addAdditionalSaveData( CompoundTag tag ) {
        super.addAdditionalSaveData( tag );
        
        tag.putFloat( References.TAG_RANGED_DAMAGE, getDamage() );
        tag.putInt( References.TAG_KNOCKBACK, getKnockback() );
    }
    
    /** Loads data from this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void readAdditionalSaveData( CompoundTag tag ) {
        super.readAdditionalSaveData( tag );
        
        if( tag.contains( References.TAG_RANGED_DAMAGE, References.NBT_TYPE_NUMERICAL ) )
            setDamage( tag.getFloat( References.TAG_RANGED_DAMAGE ) );
        if( tag.contains( References.TAG_KNOCKBACK, References.NBT_TYPE_NUMERICAL ) )
            setKnockback( tag.getInt( References.TAG_KNOCKBACK ) );
    }
}