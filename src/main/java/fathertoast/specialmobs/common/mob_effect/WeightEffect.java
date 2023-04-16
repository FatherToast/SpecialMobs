package fathertoast.specialmobs.common.mob_effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * This effect applies downward acceleration to the entity and multiplies fall distance by amplifier.
 * Only functions when the entity is at least one block above a solid block so that it doesn't hinder basic movement.
 * <p>
 * The pull effect is equivalent to one downward bubble column per effect level.
 *
 * @see net.minecraft.world.entity.Entity#onInsideBubbleColumn(boolean)
 */
public class WeightEffect extends MobEffect {
    
    public WeightEffect(MobEffectCategory category, int color ) { super( category, color ); }
    
    /** @return True if the duration tick should apply this effect. */
    public boolean isDurationEffectTick( int duration, int amplifier ) { return true; }
    
    /** Applies this effect to the entity. */
    public void applyEffectTick(LivingEntity entity, int amplifier ) {
        // We only want to apply this if the entity is at least one block above solid ground
        if( entity.level == null || entity.isOnGround() ||
                entity.level.getBlockState( entity.blockPosition().below() ).getMaterial().blocksMotion() ||
                entity instanceof Player player && player.getAbilities().flying ) return;
        
        final Vec3 v = entity.getDeltaMovement();
        final double vTick = -0.03 * (amplifier + 1);
        final double vLimit = 10.0 * vTick;
        if( v.y > vLimit ) entity.setDeltaMovement( v.x, Math.max( vLimit, v.y + vTick ), v.z );
    }
}