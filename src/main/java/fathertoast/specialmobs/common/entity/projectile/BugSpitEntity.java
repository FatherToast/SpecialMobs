package fathertoast.specialmobs.common.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.world.World;

public abstract class BugSpitEntity extends ThrowableEntity {
    
    public BugSpitEntity( EntityType<? extends BugSpitEntity> entityType, World world ) {
        super( entityType, world );
    }
    
    //TODO
}