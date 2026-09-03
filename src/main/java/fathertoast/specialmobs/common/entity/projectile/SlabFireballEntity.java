package fathertoast.specialmobs.common.entity.projectile;

import fathertoast.crust.api.lib.NBTHelper;
import fathertoast.specialmobs.common.core.register.SMEntities;
import fathertoast.specialmobs.common.core.register.SMItems;
import fathertoast.specialmobs.common.util.ExplosionHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class SlabFireballEntity extends Fireball {
    
    public static final String TAG_EXPLOSION_POWER = "ExplosionPower";
    
    public int explosionPower = 1;
    
    
    public SlabFireballEntity( EntityType<? extends SlabFireballEntity> entityType, Level level ) {
        super( entityType, level );
    }
    
    public SlabFireballEntity( Level level, LivingEntity shooter, double dx, double dy, double dz ) {
        this( level, shooter.getX(), shooter.getY(), shooter.getZ(), dx, dy, dz );
    }
    
    public SlabFireballEntity( Level level, double x, double y, double z, double dx, double dy, double dz ) {
        super( SMEntities.SLAB_FIREBALL.get(), x, y, z, dx, dy, dz, level );
    }
    
    @Override
    protected void onHit( HitResult hitResult ) {
        super.onHit( hitResult );
        
        final ExplosionHelper explosion = new ExplosionHelper( this, explosionPower, true, true );
        if( !explosion.initializeExplosion() ) return;
        explosion.finalizeExplosion( true );
        discard();
    }
    
    @SuppressWarnings( "resource" )
    @Override
    protected void onHitEntity( EntityHitResult hitResult ) {
        super.onHitEntity( hitResult );
        
        if( !level().isClientSide ) {
            final Entity entity = hitResult.getEntity();
            final Entity shooter = getOwner();
            
            if( shooter instanceof LivingEntity ) {
                // Check if this is a reflected fireball and the target is a living entity.
                if( shooter instanceof Player && entity instanceof LivingEntity livingEntity ) {
                    // Damage the target for 50% of their max health.
                    // MOB_PROJECTILE damage type is used so we can actually damage ghasts.
                    entity.hurt( damageSources().mobProjectile( this, livingEntity ), livingEntity.getMaxHealth() / 2 );
                }
                else {
                    entity.hurt( damageSources().fireball( this, shooter ), 3.0F );
                }
                doEnchantDamageEffects( (LivingEntity) shooter, entity );
            }
        }
    }
    
    @Override
    public void addAdditionalSaveData( CompoundTag saveTag ) {
        super.addAdditionalSaveData( saveTag );
        saveTag.putByte( TAG_EXPLOSION_POWER, (byte) explosionPower );
    }
    
    @Override
    public void readAdditionalSaveData( CompoundTag saveTag ) {
        super.readAdditionalSaveData( saveTag );
        
        if( NBTHelper.containsNumber( saveTag, TAG_EXPLOSION_POWER ) )
            explosionPower = saveTag.getByte( TAG_EXPLOSION_POWER );
    }
    
    @Override
    public ItemStack getItem() {
        final ItemStack itemStack = getItemRaw();
        return itemStack.isEmpty() ? new ItemStack( SMItems.SLAB_FIREBALL.get() ) : itemStack;
    }
}
