package fathertoast.specialmobs.common.entity.projectile;

import fathertoast.specialmobs.common.core.register.SMEntities;
import fathertoast.specialmobs.common.core.register.SMItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.ForgeEventFactory;

public class SlabFireballEntity extends Fireball {

    public int explosionPower = 1;

    public SlabFireballEntity( EntityType<? extends SlabFireballEntity> entityType, Level level ) {
        super( entityType, level );
    }

    public SlabFireballEntity( Level level, LivingEntity shooter, double x, double y, double z ) {
        super( SMEntities.SLAB_FIREBALL.get(), shooter, x, y, z, level );
        setDeltaMovement( getDeltaMovement().multiply( 1.5D, 1.5D, 1.5D ) );
    }

    @Override
    protected void onHit( HitResult hitResult ) {
        super.onHit( hitResult );

        if (!level().isClientSide) {
            boolean flag = ForgeEventFactory.getMobGriefingEvent( level(), getOwner() );
            level().explode( this, getX(), getY(), getZ(), (float) explosionPower, flag, Level.ExplosionInteraction.MOB );
            discard();
        }
    }

    @Override
    protected void onHitEntity( EntityHitResult hitResult ) {
        super.onHitEntity( hitResult );

        if ( !level().isClientSide ) {
            Entity entity = hitResult.getEntity();
            Entity shooter = getOwner();
            entity.hurt( damageSources().fireball( this, shooter ), 3.0F );

            if ( shooter instanceof LivingEntity livingEntity ) {
                doEnchantDamageEffects( livingEntity, entity );
            }
        }
    }

    @Override
    public void addAdditionalSaveData( CompoundTag compoundTag ) {
        super.addAdditionalSaveData( compoundTag );
        compoundTag.putByte( "ExplosionPower", (byte) explosionPower );
    }

    @Override
    public void readAdditionalSaveData( CompoundTag compoundTag ) {
        super.readAdditionalSaveData( compoundTag );

        if ( compoundTag.contains( "ExplosionPower", Tag.TAG_ANY_NUMERIC ) ) {
            explosionPower = compoundTag.getByte( "ExplosionPower" );
        }
    }

    @Override
    public ItemStack getItem() {
        ItemStack itemStack = getItemRaw();
        return itemStack.isEmpty() ? new ItemStack( SMItems.SLAB_FIREBALL.get() ) : itemStack;
    }
}
