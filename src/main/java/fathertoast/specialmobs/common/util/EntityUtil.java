package fathertoast.specialmobs.common.util;

import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public class EntityUtil {
    
    @Nullable
    public static Entity getClientPickEntity( PlayerEntity player, double pickRange ) {
        if( !player.level.isClientSide ) {
            SpecialMobs.LOG.error( "Tried to fetch player \"mouse-over\" entity from server side. This can't be right?" );
            return null;
        }
        Vector3d eyePos = player.getEyePosition( 1.0F );
        Vector3d viewVec = player.getViewVector( 1.0F );
        Vector3d targetVec = eyePos.add( viewVec.x * pickRange, viewVec.y * pickRange, viewVec.z * pickRange );
        
        AxisAlignedBB AABB = player.getBoundingBox().expandTowards( viewVec.scale( pickRange ) ).inflate( 1.0D, 1.0D, 1.0D );
        EntityRayTraceResult result = ProjectileHelper.getEntityHitResult( player, eyePos, targetVec, AABB,
                ( entity ) -> !entity.isSpectator() && entity.isPickable(), pickRange );
        
        return result != null ? result.getEntity() : null;
    }
}