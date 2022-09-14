package fathertoast.specialmobs.common.entity.projectile;

import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.core.register.SMEntities;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class SpecialFishingBobberEntity extends ProjectileEntity implements IEntityAdditionalSpawnData {
    private static final float DRAG_FACTOR = 0.92F;
    private static final float GRAVITY_ACCEL = 0.03F;
    
    private float maxRangeSq = 1024.0F;
    
    public SpecialFishingBobberEntity( EntityType<? extends SpecialFishingBobberEntity> entityType, World world ) { super( entityType, world ); }
    
    public SpecialFishingBobberEntity( LivingEntity angler, LivingEntity target ) {
        super( SMEntities.FISHING_BOBBER.get(), angler.level );
        setOwner( angler );
        
        final Vector3d lookVec = angler.getViewVector( 1.0F ).scale( angler.getBbWidth() );
        setPos( angler.getX() + lookVec.x, angler.getEyeY() - 0.1, angler.getZ() + lookVec.z );
        
        float spread = 18 - 4 * level.getDifficulty().getId();
        if( angler instanceof ISpecialMob ) {
            final ISpecialMob<?> specialShooter = (ISpecialMob<?>) angler;
            
            if( specialShooter.getSpecialData().getRangedAttackMaxRange() >= 0.0F ) {
                maxRangeSq = 2.0F * specialShooter.getSpecialData().getRangedAttackMaxRange();
                maxRangeSq *= maxRangeSq;
            }
            else {
                SpecialMobs.LOG.warn( "Entity is shooting projectile, but has no max range stat! {}", angler );
            }
            
            if( specialShooter.getSpecialData().getRangedAttackSpread() >= 0.0F ) {
                spread *= specialShooter.getSpecialData().getRangedAttackSpread();
            }
            else {
                SpecialMobs.LOG.warn( "Entity is shooting projectile, but has no ranged spread stat! {}", angler );
            }
        }
        
        final double dX = target.getX() - getX();
        final double dY = target.getY( 0.3333 ) - getY();
        final double dZ = target.getZ() - getZ();
        final double dH = MathHelper.sqrt( dX * dX + dZ * dZ );
        shoot( dX, dY + dH * 0.2, dZ, 1.3F, spread );
    }
    
    /** Called from the Entity.class constructor to define data watcher variables. */
    @Override
    protected void defineSynchedData() { }
    
    @Override
    public IPacket<?> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket( this ); }
    
    /**
     * Called by the server when constructing the spawn packet.
     * Data should be added to the provided stream.
     *
     * @param buffer The packet data stream
     */
    @Override
    public void writeSpawnData( PacketBuffer buffer ) {
        final Entity owner = getOwner();
        buffer.writeInt( owner == null ? 0 : owner.getId() );
    }
    
    /**
     * Called by the client when it receives a Entity spawn packet.
     * Data should be read out of the stream in the same way as it was written.
     *
     * @param additionalData The packet data stream
     */
    @Override
    public void readSpawnData( PacketBuffer additionalData ) {
        final int ownerId = additionalData.readInt();
        setOwner( level.getEntity( ownerId ) );
    }
    
    /** @return The bounding box to use for frustum culling. */
    @OnlyIn( Dist.CLIENT )
    @Override
    public AxisAlignedBB getBoundingBoxForCulling() {
        // Include the whole fishing line as part of the render area
        final AxisAlignedBB boundingBox = super.getBoundingBoxForCulling();
        final Entity owner = getOwner();
        return owner == null ? boundingBox : boundingBox.minmax( owner.getBoundingBoxForCulling() );
    }
    
    /** @return The owner/shooter of this projectile. Null if the owner is not set or invalid. */
    @Nullable
    public LivingEntity getLivingOwner() {
        final Entity owner = getOwner();
        return owner instanceof LivingEntity ? (LivingEntity) owner : null;
    }
    
    /** Called each tick to update this entity. */
    @Override
    public void tick() {
        super.tick();
        
        final LivingEntity angler = getLivingOwner();
        if( !level.isClientSide() && (angler == null || distanceToSqr( angler ) > maxRangeSq) ) {
            remove();
            return;
        }
        
        final RayTraceResult rayTrace = ProjectileHelper.getHitResult( this, this::canHitEntity );
        if( rayTrace.getType() != RayTraceResult.Type.MISS && !ForgeEventFactory.onProjectileImpact( this, rayTrace ) ) {
            onHit( rayTrace );
        }
        
        if( !isNoGravity() ) {
            setDeltaMovement( getDeltaMovement()
                    .add( 0.0, isInWaterOrBubble() ? 2 * GRAVITY_ACCEL : -GRAVITY_ACCEL, 0.0 ) );
        }
        
        move( MoverType.SELF, getDeltaMovement() );
        updateRotation();
        
        setDeltaMovement( getDeltaMovement().scale( DRAG_FACTOR ) );
        reapplyPosition();
    }
    
    /** Called when this projectile hits anything. */
    @Override
    protected void onHit( RayTraceResult hit ) {
        super.onHit( hit );
        
        if( !level.isClientSide() ) remove();
    }
    
    /** Called when this projectile hits an entity. */
    @Override
    protected void onHitEntity( EntityRayTraceResult hit ) {
        super.onHitEntity( hit );
        
        playSound( SoundEvents.FISHING_BOBBER_RETRIEVE, 1.0F, 0.4F / (random.nextFloat() * 0.4F + 0.8F) );
        MobHelper.pull( getOwner(), hit.getEntity(), 0.32 );
    }
    
    //    /** Called when this projectile hits a block. */
    //    @Override
    //    protected void onHitBlock( BlockRayTraceResult hit ) {
    //        super.onHitBlock( hit );
    //    }
    
    /** Saves data to this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void addAdditionalSaveData( CompoundNBT tag ) {
        super.addAdditionalSaveData( tag );
        
        tag.putFloat( References.TAG_MAX_RANGE, maxRangeSq );
    }
    
    /** Loads data from this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void readAdditionalSaveData( CompoundNBT tag ) {
        super.readAdditionalSaveData( tag );
        
        if( tag.contains( References.TAG_MAX_RANGE, References.NBT_TYPE_NUMERICAL ) )
            maxRangeSq = tag.getFloat( References.TAG_MAX_RANGE );
    }
}