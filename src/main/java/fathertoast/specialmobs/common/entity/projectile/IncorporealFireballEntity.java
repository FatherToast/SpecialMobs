package fathertoast.specialmobs.common.entity.projectile;

import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.core.register.SMEntities;
import fathertoast.specialmobs.common.core.register.SMItems;
import fathertoast.specialmobs.common.entity.ghast.CorporealShiftGhastEntity;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class IncorporealFireballEntity extends AbstractFireballEntity {

    public int explosionPower = 1;
    private boolean shouldExplode = false;
    
    @Nullable
    private LivingEntity target;
    
    
    public IncorporealFireballEntity(EntityType<? extends AbstractFireballEntity> entityType, World world ) {
        super( entityType, world );
    }
    
    public IncorporealFireballEntity(World world, CorporealShiftGhastEntity ghast, double x, double y, double z ) {
        super( SMEntities.CORPOREAL_FIREBALL.get(), ghast, x, y, z, world );
        target = ghast.getTarget();
    }
    
    public IncorporealFireballEntity(World world, PlayerEntity owner, LivingEntity target, double x, double y, double z ) {
        super( SMEntities.CORPOREAL_FIREBALL.get(), owner, x, y, z, world );
        this.target = target;
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Incorporeal Fireball",
                "", "", "", "", "", "" );//TODO
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if( !level.isClientSide ) {
            // Fizzle out and die when the target is dead or lost,
            // or else the fireball goes bonkers.
            if( target == null || !target.isAlive() ) {
                playSound( SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F );
                remove();
                return;
            }
            // Follow target
            Vector3d vector3d = new Vector3d( target.getX() - this.getX(), (target.getY() + (target.getEyeHeight() / 2)) - this.getY(), target.getZ() - this.getZ() );
            setDeltaMovement( vector3d.normalize().scale( 0.5 ) );
        }
        
        if( !level.isClientSide && shouldExplode )
            explode();
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

            boolean fizzle;

            if ( target instanceof PlayerEntity ) {
                // TODO - Implement player-specific checks
                fizzle = true;
            }
            else {
                if (target.getX() != target.xo || target.getY() != target.yo || target.getZ() != target.zo) {
                    explode();
                    return;
                }
                fizzle = true;
            }
            if (fizzle) {
                playSound( SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F );
                remove();
            }
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

    @OnlyIn(Dist.CLIENT)
    public ItemStack getItem() {
        return new ItemStack(SMItems.INCORPOREAL_FIREBALL.get());
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
}