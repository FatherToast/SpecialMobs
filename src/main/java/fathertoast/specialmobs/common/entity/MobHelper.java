package fathertoast.specialmobs.common.entity;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Random;

public final class MobHelper {
    
    /** Pool of effects to choose from for plague-type mobs to apply on hit. Duration is a multiplier. */
    private static final EffectInstance[] PLAGUE_EFFECTS = {
            new EffectInstance( Effects.MOVEMENT_SLOWDOWN, 2, 0 ),
            new EffectInstance( Effects.DIG_SLOWDOWN, 2, 1 ),
            new EffectInstance( Effects.BLINDNESS, 1, 0 ),
            new EffectInstance( Effects.HUNGER, 2, 0 ),
            new EffectInstance( Effects.WEAKNESS, 1, 0 ),
            new EffectInstance( Effects.POISON, 1, 0 ),
            new EffectInstance( Effects.CONFUSION, 2, 0 ) // Keep this option last for easy disable (by config)
    };
    
    /** Pool of effects to choose from for witch-spider-type mobs to apply on hit. Duration is a multiplier. */
    private static final EffectInstance[] WITCH_EFFECTS = {
            new EffectInstance( Effects.MOVEMENT_SLOWDOWN, 1, 1 ),
            new EffectInstance( Effects.DIG_SLOWDOWN, 2, 1 ),
            new EffectInstance( Effects.DAMAGE_RESISTANCE, 1, -3 ),
            new EffectInstance( Effects.BLINDNESS, 1, 0 ),
            new EffectInstance( Effects.HUNGER, 2, 0 ),
            new EffectInstance( Effects.WEAKNESS, 1, 0 ),
            new EffectInstance( Effects.WITHER, 1, 0 ),
            new EffectInstance( Effects.LEVITATION, 1, 1 ),
            new EffectInstance( Effects.POISON, 1, 0 ) // Keep this option last for easy disable (by cave spiders)
    };
    
    /** @return True if the damage source can deal normal damage to vampire-type mobs (e.g., wooden or smiting weapons). */
    public static boolean isDamageSourceIneffectiveAgainstVampires( DamageSource source ) {
        if( source != null ) {
            if( source.isBypassMagic() || source.isBypassInvul() ) return false;
            
            final Entity attacker = source.getEntity();
            if( attacker instanceof LivingEntity ) {
                final ItemStack weapon = ((LivingEntity) attacker).getMainHandItem();
                return !isWoodenTool( weapon ) && !hasSmite( weapon );
            }
        }
        return true;
    }
    
    /** @return True if the given item is made of wood. */
    private static boolean isWoodenTool( ItemStack item ) {
        //TODO Consider Tinkers compat - striking component must be wood
        return !item.isEmpty() && item.getItem() instanceof TieredItem && ((TieredItem) item.getItem()).getTier() == ItemTier.WOOD;
    }
    
    /** @return True if the given item deals bonus damage against undead. */
    private static boolean hasSmite( ItemStack item ) {
        //TODO Consider Tinkers compat if this doesn't already work - must have smite modifier
        return EnchantmentHelper.getDamageBonus( item, CreatureAttribute.UNDEAD ) > 0.0F;
    }
    
    /**
     * Reduces the target's life directly. Will not reduce health below 1.
     *
     * @param target The entity to cause life loss on.
     * @param amount The amount of life loss to inflict.
     */
    public static void causeLifeLoss( LivingEntity target, float amount ) {
        final float currentHealth = target.getHealth();
        if( currentHealth > 1.0F ) {
            target.setHealth( Math.max( 1.0F, currentHealth - amount ) );
        }
    }
    
    /**
     * Reduces the target's life directly and heals the attacker by the amount lost.
     * Will not reduce health below 1.
     *
     * @param attacker The entity causing the life steal.
     * @param target   The entity to steal life from.
     * @param amount   The amount of life steal to inflict.
     */
    public static void stealLife( LivingEntity attacker, LivingEntity target, float amount ) {
        final float currentHealth = target.getHealth();
        if( currentHealth > 1.0F ) {
            final float newHealth = Math.max( 1.0F, currentHealth - amount );
            target.setHealth( newHealth );
            attacker.heal( currentHealth - newHealth );
        }
    }
    
    /**
     * Removes one random food item from the player's inventory and returns it.
     * Returns an empty stack if there is no food in the player's inventory.
     *
     * @param player The player to steal from.
     * @return The item removed from the player's inventory.
     */
    public static ItemStack stealRandomFood( PlayerEntity player ) {
        final ArrayList<Integer> foodSlots = new ArrayList<>();
        for( int slot = 0; slot < player.inventory.getContainerSize(); slot++ ) {
            final ItemStack item = player.inventory.getItem( slot );
            if( !item.isEmpty() && item.getItem().getFoodProperties() != null ) foodSlots.add( slot );
        }
        if( !foodSlots.isEmpty() ) {
            return player.inventory.removeItem( foodSlots.get( player.getRandom().nextInt( foodSlots.size() ) ), 1 );
        }
        return ItemStack.EMPTY;
    }
    
    /**
     * Steals a non-harmful potion effect from a target and applies it to the attacker with a minimum duration of 5 seconds.
     *
     * @param attacker The entity stealing an effect.
     * @param target   The entity to steal an effect from.
     */
    public static void stealBuffEffect( LivingEntity attacker, LivingEntity target ) {
        if( !attacker.level.isClientSide() ) {
            for( EffectInstance potion : target.getActiveEffects() ) {
                if( potion != null && potion.getEffect().getCategory() != EffectType.HARMFUL && potion.getAmplifier() >= 0 ) {
                    target.removeEffect( potion.getEffect() );
                    attacker.addEffect( new EffectInstance( potion.getEffect(),
                            Math.max( potion.getDuration(), 200 ), potion.getAmplifier() ) );
                    return;
                }
            }
        }
    }
    
    /** @return The base debuff duration. */
    public static int getDebuffDuration( Difficulty difficulty ) {
        switch( difficulty ) {
            case PEACEFUL:
            case EASY:
                return 60;
            case NORMAL:
                return 140;
            default:
                return 300;
        }
    }
    
    /**
     * Makes a random potion effect for a plague-type mob to apply on hit.
     *
     * @param random The rng to draw from.
     * @param world  The context.
     * @return A newly created potion effect instance we can apply to an entity.
     */
    public static EffectInstance nextPlagueEffect( Random random, World world ) {
        final int duration = MobHelper.getDebuffDuration( world.getDifficulty() );
        
        //EffectInstance potion = POTIONS_PLAGUE[random.nextInt( POTIONS_PLAGUE.length - (Config.get().GENERAL.DISABLE_NAUSEA ? 1 : 0) )]; TODO config
        EffectInstance potion = PLAGUE_EFFECTS[random.nextInt( PLAGUE_EFFECTS.length )];
        return new EffectInstance( potion.getEffect(), duration * potion.getDuration(), potion.getAmplifier() );
    }
    
    /**
     * Makes a random potion effect for a witch-spider-type mob to apply on hit,
     * optionally including poison in the effect pool.
     * <p>
     * For example, witch cave spiders do not include poison in the pool because they apply poison already.
     *
     * @param random        The rng to draw from.
     * @param world         The context.
     * @param includePoison Whether to include poison in the potion pool.
     * @return A newly created potion effect instance we can apply to an entity.
     */
    public static EffectInstance nextWitchSpiderEffect( Random random, World world, boolean includePoison ) {
        final int duration = MobHelper.getDebuffDuration( world.getDifficulty() );
        
        EffectInstance potion = WITCH_EFFECTS[random.nextInt( WITCH_EFFECTS.length - (includePoison ? 0 : 1) )];
        return new EffectInstance( potion.getEffect(), duration * potion.getDuration(), potion.getAmplifier() );
    }
}