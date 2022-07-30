package fathertoast.specialmobs.common.entity.projectile;

import fathertoast.specialmobs.common.core.register.SMEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.world.World;

public class SpecialFishHookEntity extends DamagingProjectileEntity {
    
    public SpecialFishHookEntity( EntityType<? extends SpecialFishHookEntity> entityType, World world ) {
        super( entityType, world );
    }
    
    public SpecialFishHookEntity( double x, double y, double z,
                                  double dX, double dY, double dZ, World world ) {
        super( SMEntities.FISHING_BOBBER.get(), x, y, z, dX, dY, dZ, world );
    }
    
    public SpecialFishHookEntity( LivingEntity shooter,
                                  double dX, double dY, double dZ, World world ) {
        super( SMEntities.FISHING_BOBBER.get(), shooter, dX, dY, dZ, world );
    }
}