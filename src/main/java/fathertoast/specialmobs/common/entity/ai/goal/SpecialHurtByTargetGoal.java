package fathertoast.specialmobs.common.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Replaces the vanilla 'hurt by target' goal with one that properly handles alerting among variants.
 * <p>
 * The only two changes are: Allows the parent class to be specified; and Exclusions apply to all subclasses
 */
public class SpecialHurtByTargetGoal extends HurtByTargetGoal {
    
    private final Class<? extends Mob> parentClass;
    
    private Class<?>[] toIgnoreAlert;
    
    public SpecialHurtByTargetGoal( PathfinderMob entity, Class<? extends Mob> parent, Class<?>... ignoreEntities ) {
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
        final AABB boundingBox = AABB.unitCubeFromLowerCorner( mob.position() ).inflate( range, 10.0, range );
        final List<? extends Mob> nearbyEntities = mob.level().getEntitiesOfClass( parentClass, boundingBox ); // Insert parent class
        
        // This is the exact same logic as the vanilla super method, just a lot more understandable
        for( Mob entity : nearbyEntities ) {
            if( mob != entity && entity.getTarget() == null &&
                    (!(mob instanceof TamableAnimal) || ((TamableAnimal) mob).getOwner() == ((TamableAnimal) entity).getOwner()) &&
                    !entity.isAlliedTo( target ) && shouldAlert( entity ) ) {
                alertOther( entity, target );
            }
        }
    }
    
    private boolean shouldAlert( Mob entity ) {
        if( toIgnoreAlert == null ) return true;
        
        for( Class<?> ignoreClass : toIgnoreAlert ) {
            if( ignoreClass.isAssignableFrom( entity.getClass() ) ) return false; // Allow subclasses
        }
        return true;
    }
}