package fathertoast.specialmobs.common.entity.ai.goal;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * Replaces the vanilla 'hurt by target' goal with one that properly handles alerting among variants.
 * <p>
 * The only two changes are: Allows the parent class to be specified; and Exclusions apply to all subclasses
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SpecialHurtByTargetGoal extends HurtByTargetGoal {
    
    private final Class<? extends MobEntity> parentClass;
    
    private Class<?>[] toIgnoreAlert;
    
    public SpecialHurtByTargetGoal( CreatureEntity entity, Class<? extends MobEntity> parent, Class<?>... ignoreEntities ) {
        super( entity, ignoreEntities );
        parentClass = parent;
    }
    
    @Override
    public SpecialHurtByTargetGoal setAlertOthers( Class<?>... except ) {
        toIgnoreAlert = except;
        super.setAlertOthers( except );
        return this;
    }
    
    @Override
    protected void alertOthers() {
        final LivingEntity target = mob.getLastHurtByMob();
        if( target == null ) return;
        
        final double range = getFollowDistance();
        final AxisAlignedBB boundingBox = AxisAlignedBB.unitCubeFromLowerCorner( mob.position() ).inflate( range, 10.0, range );
        final List<MobEntity> nearbyEntities = mob.level.getLoadedEntitiesOfClass( parentClass, boundingBox ); // Insert parent class
        
        // This is the exact same logic as the vanilla super method, just a lot more understandable
        for( MobEntity entity : nearbyEntities ) {
            if( mob != entity && entity.getTarget() == null &&
                    (!(mob instanceof TameableEntity) || ((TameableEntity) mob).getOwner() == ((TameableEntity) entity).getOwner()) &&
                    !entity.isAlliedTo( target ) && shouldAlert( entity ) ) {
                alertOther( entity, target );
            }
        }
    }
    
    private boolean shouldAlert( MobEntity entity ) {
        if( toIgnoreAlert == null ) return true;
        
        for( Class<?> ignoreClass : toIgnoreAlert ) {
            if( ignoreClass.isAssignableFrom( entity.getClass() ) ) return false; // Allow subclasses
        }
        return true;
    }
}