package fathertoast.specialmobs.common.entity.projectile;

import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.core.register.SMEntities;
import fathertoast.specialmobs.common.core.register.SMItems;
import fathertoast.specialmobs.common.entity.ghast.CorporealShiftGhastEntity;
import fathertoast.specialmobs.common.event.PlayerVelocityWatcher;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public class IncorporealFireballEntity extends AbstractHurtingProjectile implements IEntityAdditionalSpawnData, ItemSupplier {

    public int explosionPower = 1;
    private boolean shouldExplode = false;
    
    @Nullable
    private LivingEntity target;
    
    
    public IncorporealFireballEntity( EntityType<? extends AbstractHurtingProjectile> entityType, Level level ) {
        super( entityType, level );
    }
    
    public IncorporealFireballEntity( Level level, CorporealShiftGhastEntity ghast, double x, double y, double z ) {
        super( SMEntities.INCORPOREAL_FIREBALL.get(), ghast, x, y, z, level );
        explosionPower = ghast.getExplosionPower();
        target = ghast.getTarget();
    }
    
    public IncorporealFireballEntity(Level level, @Nullable Player owner, @Nullable LivingEntity target, double x, double y, double z ) {
        this( SMEntities.INCORPOREAL_FIREBALL.get(), level );
        setPos( x, y, z );
        this.target = target;

        moveTo( x, y, z, getYRot(), getXRot() );
        reapplyPosition();
        double d = Mth.sqrt( (float) (x * x + y * y + z * z) );

        if ( d != 0.0D ) {
            xPower = x / d * 0.1D;
            yPower = y / d * 0.1D;
            zPower = z / d * 0.1D;
        }

        if ( owner != null ) {
            setOwner( owner );
            setRot( owner.getYRot(), owner.getXRot() );
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

            if ( !level.isClientSide ) discard();
            return;
        }
        // Follow target
        Vec3 vec3 = new Vec3( target.getX() - this.getX(), (target.getY() + (target.getEyeHeight() / 2)) - this.getY(), target.getZ() - this.getZ() );
        setDeltaMovement( vec3.normalize().scale( 0.5 ) );

        // Boof
        if ( !level.isClientSide && shouldExplode ) explode();
    }
    
    private void explode() {
        boolean mobGrief = ForgeEventFactory.getMobGriefingEvent( level, getOwner() );
        Explosion.BlockInteraction mode = mobGrief ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.NONE;

        level.explode( null, this.getX(), this.getY(), this.getZ(), (float) explosionPower, mobGrief, mode );
        target = null;
        discard();
    }
    
    @Override
    protected boolean shouldBurn() {
        return false;
    }
    
    @Override
    protected void onHit( HitResult hitResult ) {
        super.onHit( hitResult );
    }
    
    @Override
    protected void onHitEntity( EntityHitResult hitResult ) {
        super.onHitEntity( hitResult );
        
        if( !this.level.isClientSide ) {
            Entity target = hitResult.getEntity();

            if( target instanceof Player player ) {
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
            discard();
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
    
    @Override
    public ItemStack getItem() {
        return new ItemStack( SMItems.INCORPOREAL_FIREBALL.get() );
    }
    
    @Override
    public void addAdditionalSaveData( CompoundTag compoundTag ) {
        super.addAdditionalSaveData( compoundTag );
        compoundTag.putInt( "ExplosionPower", explosionPower );
        compoundTag.putInt( "TargetId", target == null ? -1 : target.getId() );
    }
    
    @Override
    public void readAdditionalSaveData( CompoundTag compoundTag ) {
        super.readAdditionalSaveData( compoundTag );
        if( compoundTag.contains( "ExplosionPower", Tag.TAG_ANY_NUMERIC ) ) {
            explosionPower = compoundTag.getInt( "ExplosionPower" );
        }
        
        if( compoundTag.contains( "TargetId", Tag.TAG_ANY_NUMERIC ) ) {
            Entity entity = level.getEntity( compoundTag.getInt( "TargetId" ) );
            
            if( entity instanceof LivingEntity ) {
                target = (LivingEntity) entity;
            }
        }
    }
    
    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket( this );
    }

    @Override
    public void writeSpawnData( FriendlyByteBuf buffer ) {
        final Entity owner = getOwner();
        buffer.writeInt( owner == null ? 0 : owner.getId() );
        buffer.writeInt( target == null ? 0 : target.getId() );
    }

    @Override
    public void readSpawnData( FriendlyByteBuf additionalData ) {
        final int ownerId = additionalData.readInt();
        final int targetId = additionalData.readInt();

        setOwner( level.getEntity( ownerId ) );

        if ( level.getEntity( targetId ) instanceof LivingEntity ) {
            target = (LivingEntity) level.getEntity( targetId );
        }
    }
}