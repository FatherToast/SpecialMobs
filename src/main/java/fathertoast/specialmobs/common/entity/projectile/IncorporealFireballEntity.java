package fathertoast.specialmobs.common.entity.projectile;

import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.core.register.SMEntities;
import fathertoast.specialmobs.common.core.register.SMItems;
import fathertoast.specialmobs.common.entity.ghast.CorporealShiftGhastEntity;
import fathertoast.specialmobs.common.event.PlayerVelocityWatcher;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class IncorporealFireballEntity extends AbstractFireballEntity implements IEntityAdditionalSpawnData {
    
    public int explosionPower = 1;
    private boolean shouldExplode = false;
    
    @Nullable
    private LivingEntity target;
    
    
    public IncorporealFireballEntity( EntityType<? extends AbstractFireballEntity> entityType, World world ) {
        super( entityType, world );
    }
    
    public IncorporealFireballEntity( World world, CorporealShiftGhastEntity ghast, double x, double y, double z ) {
        super( SMEntities.INCORPOREAL_FIREBALL.get(), ghast, x, y, z, world );
        explosionPower = ghast.getExplosionPower();
        target = ghast.getTarget();
    }
    
    public IncorporealFireballEntity( World world, @Nullable PlayerEntity owner, @Nullable LivingEntity target, double x, double y, double z ) {
        this( SMEntities.INCORPOREAL_FIREBALL.get(), world );
        setPos( x, y, z );
        this.target = target;

        moveTo( x, y, z, yRot, xRot );
        reapplyPosition();
        double d = MathHelper.sqrt( x * x + y * y + z * z );

        if ( d != 0.0D ) {
            xPower = x / d * 0.1D;
            yPower = y / d * 0.1D;
            zPower = z / d * 0.1D;
        }

        if ( owner != null ) {
            setOwner( owner );
            setRot( owner.yRot, owner.xRot );
        }
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Incorporeal Fireball",
                "", "", "", "", "", "" );//TODO
    }
    
    @Override
    public void tick() {
        super.tick();

        // Fizzle out and die when the target is dead or lost,
        // or else the fireball goes bonkers.
        if( target == null || !target.isAlive() ) {
            playSound( SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F );

            if ( !level.isClientSide ) remove();
            return;
        }
        // Follow target
        Vector3d vector3d = new Vector3d( target.getX() - this.getX(), (target.getY() + (target.getEyeHeight() / 2)) - this.getY(), target.getZ() - this.getZ() );
        setDeltaMovement( vector3d.normalize().scale( 0.5 ) );

        // Boof
        if ( !level.isClientSide && shouldExplode ) explode();
    }
    
    private void explode() {
        boolean mobGrief = ForgeEventFactory.getMobGriefingEvent( level, getOwner() );
        Explosion.Mode mode = mobGrief ? Explosion.Mode.DESTROY : Explosion.Mode.NONE;

        level.explode( null, this.getX(), this.getY(), this.getZ(), (float) explosionPower, mobGrief, mode );
        target = null;
        remove();
    }
    
    @Override
    protected boolean shouldBurn() {
        return false;
    }
    
    @Override
    protected void onHit( RayTraceResult traceResult ) {
        super.onHit( traceResult );
    }
    
    @Override
    protected void onHitEntity( EntityRayTraceResult traceResult ) {
        super.onHitEntity( traceResult );
        
        if( !this.level.isClientSide ) {
            Entity target = traceResult.getEntity();

            if( target instanceof PlayerEntity ) {
                PlayerEntity player = (PlayerEntity) target;
                if (PlayerVelocityWatcher.get(player).isMoving()) {
                    explode();
                    return;
                }
            }
            else {
                if( target.getX() != target.xo || target.getY() != target.yo || target.getZ() != target.zo ) {
                    explode();
                    return;
                }
            }
            playSound( SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F );
            remove();
        }
    }
    
    @Override
    public boolean hurt( DamageSource damageSource, float damage ) {
        if( isInvulnerableTo( damageSource ) || damageSource.isFire() ) {
            return false;
        }
        shouldExplode = true;
        return true;
    }
    
    @OnlyIn( Dist.CLIENT )
    public ItemStack getItem() {
        return new ItemStack( SMItems.INCORPOREAL_FIREBALL.get() );
    }
    
    @Override
    public void addAdditionalSaveData( CompoundNBT compoundNBT ) {
        super.addAdditionalSaveData( compoundNBT );
        compoundNBT.putInt( "ExplosionPower", explosionPower );
        compoundNBT.putInt( "TargetId", target == null ? -1 : target.getId() );
    }
    
    @Override
    public void readAdditionalSaveData( CompoundNBT compoundNBT ) {
        super.readAdditionalSaveData( compoundNBT );
        if( compoundNBT.contains( "ExplosionPower", Constants.NBT.TAG_ANY_NUMERIC ) ) {
            explosionPower = compoundNBT.getInt( "ExplosionPower" );
        }
        
        if( compoundNBT.contains( "TargetId", Constants.NBT.TAG_ANY_NUMERIC ) ) {
            Entity entity = level.getEntity( compoundNBT.getInt( "TargetId" ) );
            
            if( entity instanceof LivingEntity ) {
                target = (LivingEntity) entity;
            }
        }
    }
    
    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket( this );
    }

    @Override
    public void writeSpawnData( PacketBuffer buffer ) {
        final Entity owner = getOwner();
        buffer.writeInt( owner == null ? 0 : owner.getId() );
        buffer.writeInt( target == null ? 0 : target.getId() );
    }

    @Override
    public void readSpawnData( PacketBuffer additionalData ) {
        final int ownerId = additionalData.readInt();
        final int targetId = additionalData.readInt();

        setOwner( level.getEntity( ownerId ) );

        if ( level.getEntity( targetId ) instanceof LivingEntity ) {
            target = (LivingEntity) level.getEntity( targetId );
        }
    }
}