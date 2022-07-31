package fathertoast.specialmobs.common.entity.projectile;

import fathertoast.specialmobs.common.core.register.SMEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.network.IPacket;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class BoneShrapnelEntity extends ThrowableEntity {

    public BoneShrapnelEntity(EntityType<? extends BoneShrapnelEntity> entityType, World world) {
        super(entityType, world);
    }

    public BoneShrapnelEntity(World world, double x, double y, double z) {
        super(SMEntities.BONE_SHRAPNEL.get(), x, y, z, world);
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
