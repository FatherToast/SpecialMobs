package fathertoast.specialmobs.common.entity.projectile;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.world.World;

public class SpecialFishHookEntity extends DamagingProjectileEntity {
    
    protected SpecialFishHookEntity( EntityType<? extends SpecialFishHookEntity> entityType, World world ) {
        super( entityType, world );
    }
    
    public SpecialFishHookEntity( EntityType<? extends SpecialFishHookEntity> entityType, double x, double y, double z,
                                  double dX, double dY, double dZ, World world ) {
        super( entityType, x, y, z, dX, dY, dZ, world );
    }
    
    public SpecialFishHookEntity( EntityType<? extends SpecialFishHookEntity> entityType, LivingEntity shooter,
                                  double dX, double dY, double dZ, World world ) {
        super( entityType, shooter, dX, dY, dZ, world );
    }
}