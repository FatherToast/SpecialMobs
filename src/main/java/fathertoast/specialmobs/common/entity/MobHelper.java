package fathertoast.specialmobs.common.entity;

import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.core.register.SMTags;
import fathertoast.specialmobs.common.entity.creeper._SpecialCreeperEntity;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.fluids.FluidType;

import javax.annotation.Nullable;
import java.util.ArrayList;

@SuppressWarnings("JavadocReference")
public final class MobHelper {
    
    /** Pool of effects to choose from for plague-type mobs to apply on hit. Duration is a multiplier. */
    private static final MobEffectInstance[] PLAGUE_EFFECTS = {
            new MobEffectInstance( MobEffects.MOVEMENT_SLOWDOWN, 2, 0 ),
            new MobEffectInstance( MobEffects.DIG_SLOWDOWN, 2, 1 ),
            new MobEffectInstance( MobEffects.BLINDNESS, 1, 0 ),
            new MobEffectInstance( MobEffects.HUNGER, 2, 0 ),
            new MobEffectInstance( MobEffects.WEAKNESS, 1, 0 ),
            new MobEffectInstance( MobEffects.POISON, 1, 0 ),
            new MobEffectInstance( MobEffects.CONFUSION, 2, 0 ) // Keep this option last for easy disable (by config)
    };
    
    /** Pool of effects to choose from for witch-spider-type mobs to apply on hit. Duration is a multiplier. */
    private static final MobEffectInstance[] WITCH_EFFECTS = {
            new MobEffectInstance( MobEffects.MOVEMENT_SLOWDOWN, 1, 1 ),
            new MobEffectInstance( MobEffects.DIG_SLOWDOWN, 2, 1 ),
            new MobEffectInstance( MobEffects.DAMAGE_RESISTANCE, 1, -3 ),
            new MobEffectInstance( MobEffects.BLINDNESS, 1, 0 ),
            new MobEffectInstance( MobEffects.HUNGER, 2, 0 ),
            new MobEffectInstance( MobEffects.WEAKNESS, 1, 0 ),
            new MobEffectInstance( MobEffects.WITHER, 1, 0 ),
            new MobEffectInstance( MobEffects.LEVITATION, 1, 1 ),
            new MobEffectInstance( MobEffects.POISON, 1, 0 ) // Keep this option last for easy disable (by cave spiders)
    };

    /** Called on spawn to initialize properties based on the world, difficulty, and the group it spawns with. */
    @Nullable
    public static SpawnGroupData finalizeSpawn(LivingEntity entity, ServerLevelAccessor levelAccessor, DifficultyInstance difficulty,
                                               @Nullable MobSpawnType spawnType, @Nullable SpawnGroupData groupData ) {
        final ItemStack[] startingEquipment = captureEquipment( entity );
        ((ISpecialMob<?>) entity).finalizeSpecialSpawn( levelAccessor, difficulty, spawnType, groupData );
        processSpawnEquipmentChanges( entity, startingEquipment, difficulty );
        return groupData;
    }
    
    /** @return An array of the entity's current equipment so that any changes can be identified. */
    public static ItemStack[] captureEquipment( LivingEntity entity ) {
        final EquipmentSlot[] slots = EquipmentSlot.values();
        final ItemStack[] equipment = new ItemStack[slots.length];
        for( int i = 0; i < slots.length; i++ ) {
            equipment[i] = entity.getItemBySlot( slots[i] );
        }
        return equipment;
    }
    
    /** Performs random enchanting and modification to any new equipment. */
    public static void processSpawnEquipmentChanges( LivingEntity entity, ItemStack[] oldEquipment, DifficultyInstance difficulty ) {
        final float diffMulti = difficulty.getSpecialMultiplier();
        final EquipmentSlot[] slots = EquipmentSlot.values();
        for( int i = 0; i < slots.length; i++ ) {
            final ItemStack newItem = entity.getItemBySlot( slots[i] );
            if( !newItem.isEmpty() && !ItemStack.matches( newItem, oldEquipment[i] ) &&
                    entity.getRandom().nextFloat() < (slots[i].getType() == EquipmentSlot.Type.HAND ? 0.25F : 0.5F) * diffMulti ) {
                
                entity.setItemSlot( slots[i], EnchantmentHelper.enchantItem( entity.getRandom(), newItem,
                        (int) (5.0F + diffMulti * entity.getRandom().nextInt( 18 )), false ) );
            }
        }
    }
    
    /** Charges a creeper, potentially supercharging it. */
    public static void charge( Creeper creeper ) {
        if( creeper instanceof _SpecialCreeperEntity specialCreeper ) {
            specialCreeper.charge();
        }
        else {
            creeper.getEntityData().set( Creeper.DATA_IS_POWERED, true );
        }
    }
    
    /** @return True if the damage source can deal normal damage to vampire-type mobs (e.g., wooden or smiting weapons). */
    public static boolean isDamageSourceIneffectiveAgainstVampires( DamageSource source ) {
        if( source.is( DamageTypeTags.BYPASSES_ARMOR ) || source.is( DamageTypeTags.BYPASSES_INVULNERABILITY ) ) return false;
        
        final Entity attacker = source.getEntity();
        if( attacker instanceof LivingEntity ) {
            final ItemStack weapon = ((LivingEntity) attacker).getMainHandItem();
            return !isWoodenTool( weapon ) && !hasSmite( weapon );
        }
        return true;
    }

    /** @return The amount of extra damage to inflict on vampire-like mobs depending on the damage source. */
    public static float getVampireDamageBonus( DamageSource source ) {
        final Entity attacker = source.getEntity();

        if( attacker instanceof LivingEntity ) {
            final ItemStack weapon = ((LivingEntity) attacker).getMainHandItem();
            return weapon.is(SMTags.GARLIC) ? 6.0F : 0.0F;
        }
        return 0.0F;
    }
    
    /** @return True if the given item is made of wood. */
    private static boolean isWoodenTool( ItemStack item ) {
        if( item.isEmpty() ) return false;
        //TODO Consider Tinkers compat - striking component must be wood
        return item.getItem() instanceof TieredItem && ((TieredItem) item.getItem()).getTier() == Tiers.WOOD ||
                item.getItem() instanceof BowItem || item.getItem() instanceof CrossbowItem;
    }
    
    /** @return True if the given item deals bonus damage against undead. */
    private static boolean hasSmite( ItemStack item ) {
        //TODO Consider Tinkers compat if this doesn't already work - must have smite modifier
        return EnchantmentHelper.getDamageBonus( item, MobType.UNDEAD ) > 0.0F;
    }
    
    /** Pulls the target. */
    public static void pull( @Nullable Entity angler, @Nullable Entity fish, double power ) {
        if( angler instanceof LivingEntity && fish instanceof LivingEntity ) {
            fish.setDeltaMovement( fish.getDeltaMovement().scale( 0.2 ).add(
                    (angler.getX() - fish.getX()) * power,
                    Math.min( (angler.getY() - fish.getY()) * power + Math.sqrt( fish.distanceTo( angler ) ) * 0.1, 2.0 ),
                    (angler.getZ() - fish.getZ()) * power ) );
            fish.hurtMarked = true;
            ((LivingEntity) angler).swing( InteractionHand.MAIN_HAND );
        }
    }
    
    /**
     * Knocks the target away from the source. Power is on the same scale as "level of Knockback enchantment".
     * If the power is negative, this pulls the target toward the source instead.
     */
    public static void knockback( Entity source, LivingEntity target, float power, float upwardMulti ) {
        knockback( source, 0.6, target, power, upwardMulti, 0.5 );
    }
    
    /**
     * Knocks the target away from the source. Power is on the same scale as "level of Knockback enchantment".
     * If the power is negative, this pulls the target toward the source instead.
     * Momentum is the amount of original velocity maintained by the entity after knockback.
     */
    public static void knockback( Entity source, double sourceMomentum, LivingEntity target, float power, float upwardMulti, double momentum ) {
        final float angle = source.getYRot() * (float) Math.PI / 180.0F;
        knockback( target, power, upwardMulti, Mth.sin( angle ), -Mth.cos( angle ), momentum );
        source.setDeltaMovement( source.getDeltaMovement().multiply( sourceMomentum, 1.0, sourceMomentum ) );
    }
    
    /**
     * Knocks the target backward (based on its own facing). Power is on the same scale as "level of Knockback enchantment".
     * If the power is negative, this pushes the target forward instead.
     */
    @SuppressWarnings( "unused" )
    public static void knockback( LivingEntity target, float power, float upwardMulti ) {
        knockback( target, power, upwardMulti, 0.5 );
    }
    
    /**
     * Knocks the target backward (based on its own facing). Power is on the same scale as "level of Knockback enchantment".
     * If the power is negative, this pushes the target forward instead.
     * Momentum is the amount of original velocity maintained by the entity after knockback.
     */
    public static void knockback( LivingEntity target, float power, float upwardMulti, double momentum ) {
        final float angle = target.getYRot() * (float) Math.PI / 180.0F;
        knockback( target, power, upwardMulti, -Mth.sin( angle ), Mth.cos( angle ), momentum );
    }
    
    /**
     * Knocks the target backward (opposite direction from the [x, z] vector provided).
     * Power is on the same scale as "level of Knockback enchantment".
     * If the power is negative, this pushes the target forward instead.
     */
    @SuppressWarnings( "unused" )
    public static void knockback( LivingEntity target, float power, float upwardMulti, double forwardX, double forwardZ ) {
        knockback( target, power, upwardMulti, forwardX, forwardZ, 0.5 );
    }
    
    /**
     * Knocks the target backward (opposite direction from the [x, z] vector provided).
     * Power is on the same scale as "level of Knockback enchantment".
     * If the power is negative, this pushes the target forward instead.
     * Momentum is the amount of original velocity maintained by the entity after knockback.
     */
    public static void knockback( LivingEntity target, float power, float upwardMulti, double forwardX, double forwardZ, double momentum ) {
        final LivingKnockBackEvent event = power < 0.0F ?
                ForgeHooks.onLivingKnockBack( target, -power * 0.5F, -forwardX, -forwardZ ) :
                ForgeHooks.onLivingKnockBack( target, power * 0.5F, forwardX, forwardZ );
        if( event.isCanceled() ) return;
        
        power = (float) (event.getStrength() * (1.0 - target.getAttributeValue( Attributes.KNOCKBACK_RESISTANCE )));
        if( power > 0.0F ) {
            target.hasImpulse = true;
            final Vec3 v = target.getDeltaMovement().scale( momentum );
            final Vec3 vKB = new Vec3( -event.getRatioX(), 0.0, -event.getRatioZ() ).normalize().scale( power );
            final double vY = v.y + power * upwardMulti;
            target.setDeltaMovement(
                    v.x + vKB.x,
                    target.onGround() ? Math.max( 0.2, vY ) : vY,
                    v.z + vKB.z );
            target.hurtMarked = true;
        }
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
    public static ItemStack stealRandomFood( Player player ) {
        final ArrayList<Integer> foodSlots = new ArrayList<>();
        for( int slot = 0; slot < player.getInventory().getContainerSize(); slot++ ) {
            final ItemStack item = player.getInventory().getItem( slot );
            if( !item.isEmpty() && item.getItem().getFoodProperties(item, null) != null ) foodSlots.add( slot );
        }
        if( !foodSlots.isEmpty() ) {
            return player.getInventory().removeItem( foodSlots.get( player.getRandom().nextInt( foodSlots.size() ) ), 1 );
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
        if( !attacker.level().isClientSide() ) {
            for( MobEffectInstance potion : target.getActiveEffects() ) {
                if( potion != null && potion.getEffect().getCategory() != MobEffectCategory.HARMFUL && potion.getAmplifier() >= 0 ) {
                    target.removeEffect( potion.getEffect() );
                    attacker.addEffect( new MobEffectInstance( potion.getEffect(),
                            Math.max( potion.getDuration(), 200 ), potion.getAmplifier() ) );
                    return;
                }
            }
        }
    }
    
    /** Removes night vision from the target. Typically used when blindness is involved due to the awkward interaction. */
    public static void removeNightVision( LivingEntity target ) { target.removeEffect( MobEffects.NIGHT_VISION ); }
    
    /** Applies a random 'plague' potion effect to the target. */
    public static void applyPlagueEffect( LivingEntity target, RandomSource random ) {
        applyEffectFromTemplate( target, PLAGUE_EFFECTS[random.nextInt( PLAGUE_EFFECTS.length -
                (Config.MAIN.GENERAL.enableNausea.get() ? 0 : 1) )] );
    }
    
    /** Applies a random 'witch spider' potion effect to the target, optionally including poison in the effect pool. */
    public static void applyWitchSpiderEffect( LivingEntity target, RandomSource random, boolean includePoison ) {
        applyEffectFromTemplate( target, WITCH_EFFECTS[random.nextInt( WITCH_EFFECTS.length - (includePoison ? 0 : 1) )] );
    }
    
    /** Applies a potion effect to the target with default duration. */
    public static void applyEffect( LivingEntity target, MobEffect effect ) { applyEffect( target, effect, 1, 1.0F ); }
    
    /** Applies a potion effect to the target with default duration and a specified level (amplifier + 1). */
    public static void applyEffect( LivingEntity target, MobEffect effect, int level ) {
        applyEffect( target, effect, level, 1.0F );
    }
    
    /** Applies a potion effect to the target with default duration and a specified level (amplifier + 1). */
    public static void applyEffect( LivingEntity target, MobEffect effect, float durationMulti ) {
        applyEffect( target, effect, 1, durationMulti );
    }
    
    /** Applies a potion effect to the target with default duration and a specified level (amplifier + 1). */
    public static void applyEffect( LivingEntity target, MobEffect effect, int level, float durationMulti ) {
        applyEffect( target, effect, level, effect.isInstantenous() ? 1 :
                (int) (MobHelper.defaultEffectDuration( target.level().getDifficulty() ) * durationMulti) );
    }
    
    /** Applies a potion effect to the target with a specified level (amplifier + 1) and duration. */
    public static void applyDurationEffect( LivingEntity target, MobEffect effect, int duration ) {
        applyEffect( target, effect, 1, duration );
    }
    
    /** Applies a potion effect to the target with a specified level (amplifier + 1) and duration. */
    public static void applyEffect(LivingEntity target, MobEffect effect, int level, int duration ) {
        target.addEffect( new MobEffectInstance( effect, duration, level - 1 ) );
    }
    
    /** Applies a potion effect to the target based on a template effect. The template's duration is used as a multiplier. */
    public static void applyEffectFromTemplate( LivingEntity target, MobEffectInstance template ) {
        applyEffectFromTemplate( target, template, MobHelper.defaultEffectDuration( target.level().getDifficulty() ) );
    }
    
    /** Applies a potion effect to the target based on a template effect. The template's duration is used as a multiplier. */
    public static void applyEffectFromTemplate( LivingEntity target, MobEffectInstance template, int baseDuration ) {
        target.addEffect( new MobEffectInstance( template.getEffect(), template.getEffect().isInstantenous() ? 1 :
                baseDuration * template.getDuration(), template.getAmplifier() ) );
    }
    
    /** Applies a random 'plague' potion effect to the arrow. */
    public static AbstractArrow tipPlagueArrow(AbstractArrow arrow, RandomSource random ) {
        return tipArrowFromTemplate( arrow, PLAGUE_EFFECTS[random.nextInt( PLAGUE_EFFECTS.length -
                (Config.MAIN.GENERAL.enableNausea.get() ? 0 : 1) )] );
    }
    
    //    /** Applies a random 'witch spider' potion effect to the arrow, optionally including poison in the effect pool. */
    //    public static AbstractArrowEntity tipWitchSpiderArrow( AbstractArrowEntity arrow, Random random, boolean includePoison ) {
    //        return tipArrowFromTemplate( arrow, WITCH_EFFECTS[random.nextInt( WITCH_EFFECTS.length - (includePoison ? 0 : 1) )] );
    //    }
    
    /** Applies a potion effect to the arrow with default duration. */
    public static AbstractArrow tipArrow( AbstractArrow arrow, MobEffect effect ) {
        return tipArrow( arrow, effect, 1, 1.0F );
    }
    
    /** Applies a potion effect to the arrow with default duration and a specified level (amplifier + 1). */
    public static AbstractArrow tipArrow( AbstractArrow arrow, MobEffect effect, int level ) {
        return tipArrow( arrow, effect, level, 1.0F );
    }
    
    /** Applies a potion effect to the arrow with default duration and a specified level (amplifier + 1). */
    public static AbstractArrow tipArrow( AbstractArrow arrow, MobEffect effect, float durationMulti ) {
        return tipArrow( arrow, effect, 1, durationMulti );
    }
    
    /** Applies a potion effect to the arrow with default duration and a specified level (amplifier + 1). */
    public static AbstractArrow tipArrow( AbstractArrow arrow, MobEffect effect, int level, float durationMulti ) {
        return tipArrow( arrow, effect, level, effect.isInstantenous() ? 1 :
                (int) (MobHelper.defaultEffectDuration( arrow.level().getDifficulty() ) * durationMulti) );
    }
    
    /** Applies a potion effect to the arrow with a specified level (amplifier + 1) and duration. */
    public static AbstractArrow tipArrow( AbstractArrow arrow, MobEffect effect, int level, int duration ) {
        if( arrow instanceof Arrow arrow1 )
            arrow1.addEffect( new MobEffectInstance( effect, duration, level - 1 ) );
        return arrow;
    }
    
    /** Applies a potion effect to the arrow based on a template effect. The template's duration is used as a multiplier. */
    public static AbstractArrow tipArrowFromTemplate( AbstractArrow arrow, MobEffectInstance template ) {
        return tipArrowFromTemplate( arrow, template, MobHelper.defaultEffectDuration( arrow.level().getDifficulty() ) );
    }
    
    /** Applies a potion effect to the arrow based on a template effect. The template's duration is used as a multiplier. */
    public static AbstractArrow tipArrowFromTemplate( AbstractArrow arrow, MobEffectInstance template, int baseDuration ) {
        if( arrow instanceof Arrow arrow1 )
            arrow1.addEffect( new MobEffectInstance( template.getEffect(), template.getEffect().isInstantenous() ? 1 :
                    baseDuration * template.getDuration(), template.getAmplifier() ) );
        return arrow;
    }
    
    /** @return The base debuff duration. */
    public static int defaultEffectDuration( Difficulty difficulty ) {
        return switch (difficulty) {
            case PEACEFUL, EASY -> 60;
            case NORMAL -> 140;
            default -> 300;
        };
    }
    
    /**
     * Tries to block an incoming damage source on behalf of the blocker, possibly destroying their shield.
     * Note that the blocker will have to call blockUsingShield() and cancel the damage event themselves if this works.
     *
     * @param blocker     The entity that is being attacked.
     * @param source      The damage source.
     * @param needsShield If false, allows the blocker to succeed without a shield.
     * @return True if the block was successful.
     */
    public static boolean tryBlockAttack( LivingEntity blocker, DamageSource source, boolean needsShield ) {
        if( blocker.level().isClientSide() || blocker.isInvulnerableTo( source ) || source.is( DamageTypeTags.BYPASSES_ARMOR ) ) return false;
        
        // Block everything coming from entities at least 6 blocks away, otherwise 33% block chance
        if( blocker.getRandom().nextFloat() >= 0.33F ) {
            final Entity attacker = source.getEntity();
            if( attacker == null || blocker.distanceToSqr( attacker ) < 36.0 ) return false;
        }
        
        // Cannot block piercing arrows
        final Entity entity = source.getDirectEntity();
        if(entity instanceof final AbstractArrow arrow) {
            if( arrow.getPierceLevel() > 0 ) return false;
        }
        
        // Make sure we actually have a shield
        InteractionHand shieldHand = InteractionHand.OFF_HAND;
        ItemStack shield = blocker.getItemInHand( shieldHand );
        if( needsShield && (shield.isEmpty() || !shield.canPerformAction( ToolActions.SHIELD_BLOCK ) ) ) {
            shieldHand = InteractionHand.MAIN_HAND;
            shield = blocker.getItemInHand( shieldHand );
            if( shield.isEmpty() || !shield.canPerformAction( ToolActions.SHIELD_BLOCK ) ) return false;
        }
        
        // Block frontal attacks only
        final Vec3 sourcePos = source.getSourcePosition();
        if( sourcePos != null ) {
            final Vec3 lookVec = blocker.getViewVector( 1.0F );
            Vec3 targetVec = sourcePos.vectorTo( blocker.position() ).normalize();
            targetVec = new Vec3( targetVec.x, 0.0, targetVec.z );
            if( targetVec.dot( lookVec ) < 0.0 ) {
                blocker.level().playSound( null, blocker.getX() + 0.5D, blocker.getY(), blocker.getZ() + 0.5D, SoundEvents.SHIELD_BLOCK, SoundSource.NEUTRAL, 0.9F, 1.0F );
                if( needsShield && entity instanceof Player player ) {
                    maybeDestroyShield( blocker, shield, shieldHand, player.getMainHandItem() );
                }
                if( !source.is( DamageTypeTags.IS_PROJECTILE ) && entity instanceof LivingEntity ) {
                    // Because the vanilla shield knockback is mega-borked
                    ((LivingEntity) entity).knockback( 0.5F,
                            blocker.getX() - entity.getX(), blocker.getZ() - entity.getZ() );
                    entity.hurtMarked = true;
                }
                return true;
            }
        }
        return false;
    }
    
    /** Destroys the blocker's shield based on random chance and conditions. Equivalent to players having their shield set on cooldown. */
    private static void maybeDestroyShield( LivingEntity blocker, ItemStack shield, InteractionHand shieldHand, ItemStack weapon ) {
        if( !weapon.isEmpty() && !shield.isEmpty() && weapon.getItem() instanceof AxeItem && shield.getItem() == Items.SHIELD &&
                blocker.getRandom().nextFloat() < 0.25F - EnchantmentHelper.getBlockEfficiency( blocker ) * 0.05F ) {
            blocker.level().playSound( null, blocker.getX() + 0.5D, blocker.getY(), blocker.getZ() + 0.5D, SoundEvents.SHIELD_BREAK, SoundSource.NEUTRAL, 0.9F, 1.0F );
            blocker.broadcastBreakEvent( shieldHand );
            blocker.setItemInHand( shieldHand, ItemStack.EMPTY );
        }
    }
    
    /**
     * Floats the entity upward if they are in a given fluid type. Used by entities that walk on fluids so their
     * AI doesn't break if they wind up inside that fluid.
     * <p>
     * Should be called in the entity's {@link Entity#tick()} loop right after super.
     *
     * @param entity       The entity to float.
     * @param acceleration How fast the entity floats upward.
     * @param fluidType    The FluidType of the fluid to float in.
     */
    public static void floatInFluid( Entity entity, double acceleration, FluidType fluidType ) {
        if( entity.tickCount > 1 && entity.getFluidTypeHeight(fluidType) > 0.0 ) {
            if( !entity.getEyeInFluidType().isAir() && CollisionContext.of( entity ).isAbove( LiquidBlock.STABLE_SHAPE, entity.blockPosition(), true ) &&
                    entity.level().getFluidState( entity.blockPosition().above() ).getFluidType() == fluidType ) {
                entity.setOnGround( true );
            }
            else {
                entity.setDeltaMovement( entity.getDeltaMovement().scale( 0.5 ).add( 0.0, acceleration, 0.0 ) );
            }
        }
    }
    
    /**
     * Manually provides the frost walker enchantment's effects without any equipment requirements.
     * <p>
     * Should be called in the entity's {@link LivingEntity#onChangedBlock(BlockPos)}} method right after super.
     *
     * @param entity The entity.
     * @param pos    The block pos argument from #onChangedBlock.
     */
    public static void updateFrostWalker(LivingEntity entity, BlockPos pos ) { updateFrostWalker( entity, pos, 1 ); }
    
    /**
     * Manually provides the frost walker enchantment's effects without any equipment requirements.
     * <p>
     * Should be called in the entity's {@link LivingEntity#onChangedBlock(BlockPos)} method right after super.
     *
     * @param entity The entity.
     * @param pos    The block pos argument from #onChangedBlock.
     * @param level  The level of enchantment. Platform radius is 2 + level.
     */
    public static void updateFrostWalker( LivingEntity entity, BlockPos pos, int level ) {
        final boolean actualOnGround = entity.onGround();
        entity.setOnGround( true ); // Spoof the frost walker enchant requirement to be on the ground
        FrostWalkerEnchantment.onEntityMoved( entity, entity.level(), pos, level );
        entity.setOnGround( actualOnGround );
    }
    
    /**
     * Pops the entity upward if they are on top of the water, so that their frost walker ability can take effect.
     * <p>
     * Should be called in the entity's {@link Entity#tick()} loop after both super and the call to floatInFluid.
     *
     * @param entity The entity to pop.
     */
    public static void hopOnFluid( Entity entity ) {
        if( entity.tickCount > 1 && entity.level().random.nextInt( 10 ) == 0 ) {
            if( CollisionContext.of( entity ).isAbove( LiquidBlock.STABLE_SHAPE, entity.blockPosition(), true ) &&
                    entity.level().getFluidState( entity.blockPosition() ).is( FluidTags.WATER )
                    && !entity.level().getFluidState( entity.blockPosition().above() ).is( FluidTags.WATER ) ) {

                // Break water plants and other waterlogged things, otherwise frost walker will not work
                final BlockState block = entity.level().getBlockState( entity.blockPosition() );

                if( !block.isAir() && !block.isSolid() && !block.getFluidState().isEmpty() ) {
                    final BlockEntity blockEntity = block.hasBlockEntity() ? entity.level().getExistingBlockEntity( entity.blockPosition() ) : null;
                    Block.dropResources( block, entity.level(), entity.blockPosition(), blockEntity );
                    entity.level().setBlock( entity.blockPosition(), Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL );
                }
                entity.setDeltaMovement( entity.getDeltaMovement().scale( 0.5 ).add( 0.0, 0.4, 0.0 ) );
            }
        }
    }

    /**
     * Pops the entity upward if they are right next to a solid block when floating in a fluid.
     * <p>
     * Should be called in the entity's {@link Entity#tick()} loop after both super and the call to floatInFluid.
     *
     * @param entity The entity to try and hop back on land
     */
    public static void hopOntoShore( Entity entity, TagKey<Fluid> fluidTag ) {
        if( entity.tickCount > 1 && entity.level().random.nextInt( 10 ) == 0 ) {
            if( CollisionContext.of( entity ).isAbove( LiquidBlock.STABLE_SHAPE, entity.blockPosition(), true ) &&
                    !entity.level().getFluidState( entity.blockPosition().above() ).is( fluidTag ) ) {

                for ( Direction dir : Direction.Plane.HORIZONTAL ) {
                    BlockState neighborState = entity.level().getBlockState( entity.blockPosition().relative( dir ) );

                    if ( neighborState.isSolid() && entity.getDirection() == dir ) {
                        entity.setDeltaMovement( entity.getDeltaMovement().scale( 0.5 ).add( 0.0, 0.4, 0.0 ) );
                    }
                }
            }
        }
    }
    
    /** @return Attempts to place a block, firing the appropriate Forge event. Returns true if successful. */
    public static boolean placeBlock( Entity entity, BlockPos pos, BlockState block ) {
        return placeBlock( entity, pos, block, References.SetBlockFlags.DEFAULTS );
    }
    
    /** @return Attempts to place a block, firing the appropriate Forge event. Returns true if successful. */
    public static boolean placeBlock( Entity entity, BlockPos pos, Direction direction, BlockState block ) {
        return placeBlock( entity, pos, direction, block, References.SetBlockFlags.DEFAULTS );
    }
    
    /** @return Attempts to place a block, firing the appropriate Forge event. Returns true if successful. */
    public static boolean placeBlock( Entity entity, BlockPos pos, BlockState block, int updateFlags ) {
        return placeBlock( entity, pos, Direction.UP, block, updateFlags );
    }
    
    /** @return Attempts to place a block, firing the appropriate Forge event. Returns true if successful. */
    public static boolean placeBlock( Entity entity, BlockPos pos, Direction direction, BlockState block, int updateFlags ) {
        if( canPlaceBlock( entity, pos, direction ) ) {
            entity.level().setBlock( pos, block, updateFlags );
            return true;
        }
        return false;
    }
    
    // Note to future self - I should probably also make 'destroy block' methods for whatever Forge event those should have,
    // generally I do fire mob griefing events already for everything, though
    
    /** @return Fires the Forge event to check if a block can be placed and returns the result. */
    public static boolean canPlaceBlock( Entity entity, BlockPos pos ) {
        return canPlaceBlock( entity, pos, Direction.UP );
    }
    
    /** @return Fires the Forge event to check if a block can be placed and returns the result. */
    public static boolean canPlaceBlock( Entity entity, BlockPos pos, Direction direction ) {
        return !ForgeEventFactory.onBlockPlace( entity, BlockSnapshot.create( entity.level().dimension(), entity.level(), pos ), direction );
    }
}