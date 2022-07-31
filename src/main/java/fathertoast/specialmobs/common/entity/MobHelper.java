package fathertoast.specialmobs.common.entity;

import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.entity.creeper._SpecialCreeperEntity;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.FrostWalkerEnchantment;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;

import javax.annotation.Nullable;
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
    
    /** Called on spawn to initialize properties based on the world, difficulty, and the group it spawns with. */
    @Nullable
    public static ILivingEntityData finalizeSpawn( LivingEntity entity, IServerWorld world, DifficultyInstance difficulty,
                                                   @Nullable SpawnReason spawnReason, @Nullable ILivingEntityData groupData ) {
        final ItemStack[] startingEquipment = captureEquipment( entity );
        ((ISpecialMob<?>) entity).finalizeSpecialSpawn( world, difficulty, spawnReason, groupData );
        processSpawnEquipmentChanges( entity, startingEquipment, difficulty );
        return groupData;
    }
    
    /** @return An array of the entity's current equipment so that any changes can be identified. */
    public static ItemStack[] captureEquipment( LivingEntity entity ) {
        final EquipmentSlotType[] slots = EquipmentSlotType.values();
        final ItemStack[] equipment = new ItemStack[slots.length];
        for( int i = 0; i < slots.length; i++ ) {
            equipment[i] = entity.getItemBySlot( slots[i] );
        }
        return equipment;
    }
    
    /** Performs random enchanting and modification to any new equipment. */
    public static void processSpawnEquipmentChanges( LivingEntity entity, ItemStack[] oldEquipment, DifficultyInstance difficulty ) {
        final float diffMulti = difficulty.getSpecialMultiplier();
        final EquipmentSlotType[] slots = EquipmentSlotType.values();
        for( int i = 0; i < slots.length; i++ ) {
            final ItemStack newItem = entity.getItemBySlot( slots[i] );
            if( !newItem.isEmpty() && !ItemStack.matches( newItem, oldEquipment[i] ) &&
                    entity.getRandom().nextFloat() < (slots[i].getType() == EquipmentSlotType.Group.HAND ? 0.25F : 0.5F) * diffMulti ) {
                
                entity.setItemSlot( slots[i], EnchantmentHelper.enchantItem( entity.getRandom(), newItem,
                        (int) (5.0F + diffMulti * entity.getRandom().nextInt( 18 )), false ) );
            }
        }
    }
    
    /** Charges a creeper, potentially supercharging it. */
    public static void charge( CreeperEntity creeper ) {
        if( creeper instanceof _SpecialCreeperEntity ) {
            ((_SpecialCreeperEntity) creeper).charge();
        }
        else {
            creeper.getEntityData().set( CreeperEntity.DATA_IS_POWERED, true );
        }
    }
    
    /** @return True if the damage source can deal normal damage to vampire-type mobs (e.g., wooden or smiting weapons). */
    public static boolean isDamageSourceIneffectiveAgainstVampires( DamageSource source ) {
        if( source.isBypassMagic() || source.isBypassInvul() ) return false;
        
        final Entity attacker = source.getEntity();
        if( attacker instanceof LivingEntity ) {
            final ItemStack weapon = ((LivingEntity) attacker).getMainHandItem();
            return !isWoodenTool( weapon ) && !hasSmite( weapon );
        }
        return true;
    }
    
    /** @return True if the given item is made of wood. */
    private static boolean isWoodenTool( ItemStack item ) {
        if( item.isEmpty() ) return false;
        //TODO Consider Tinkers compat - striking component must be wood
        return item.getItem() instanceof TieredItem && ((TieredItem) item.getItem()).getTier() == ItemTier.WOOD ||
                item.getItem() instanceof BowItem || item.getItem() instanceof CrossbowItem;
    }
    
    /** @return True if the given item deals bonus damage against undead. */
    private static boolean hasSmite( ItemStack item ) {
        //TODO Consider Tinkers compat if this doesn't already work - must have smite modifier
        return EnchantmentHelper.getDamageBonus( item, CreatureAttribute.UNDEAD ) > 0.0F;
    }
    
    /** Pulls the target. */
    public static void pull( @Nullable Entity angler, @Nullable Entity fish, double power ) {
        if( angler instanceof LivingEntity && fish instanceof LivingEntity ) {
            fish.setDeltaMovement( fish.getDeltaMovement().scale( 0.2 ).add(
                    (angler.getX() - fish.getX()) * power,
                    Math.min( (angler.getY() - fish.getY()) * power + Math.sqrt( fish.distanceTo( angler ) ) * 0.1, 2.0 ),
                    (angler.getZ() - fish.getZ()) * power ) );
            fish.hurtMarked = true;
            ((LivingEntity) angler).swing( Hand.MAIN_HAND );
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
        final float angle = source.yRot * (float) Math.PI / 180.0F;
        knockback( target, power, upwardMulti, MathHelper.sin( angle ), -MathHelper.cos( angle ), momentum );
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
        final float angle = target.yRot * (float) Math.PI / 180.0F;
        knockback( target, power, upwardMulti, -MathHelper.sin( angle ), MathHelper.cos( angle ), momentum );
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
            final Vector3d v = target.getDeltaMovement().scale( momentum );
            final Vector3d vKB = new Vector3d( -event.getRatioX(), 0.0, -event.getRatioZ() ).normalize().scale( power );
            final double vY = v.y + power * upwardMulti;
            target.setDeltaMovement(
                    v.x + vKB.x,
                    target.isOnGround() ? Math.max( 0.2, vY ) : vY,
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
    
    /** Removes night vision from the target. Typically used when blindness is involved due to the awkward interaction. */
    public static void removeNightVision( LivingEntity target ) { target.removeEffect( Effects.NIGHT_VISION ); }
    
    /** Applies a random 'plague' potion effect to the target. */
    public static void applyPlagueEffect( LivingEntity target, Random random ) {
        applyEffectFromTemplate( target, PLAGUE_EFFECTS[random.nextInt( PLAGUE_EFFECTS.length -
                (Config.MAIN.GENERAL.enableNausea.get() ? 0 : 1) )] );
    }
    
    /** Applies a random 'witch spider' potion effect to the target, optionally including poison in the effect pool. */
    public static void applyWitchSpiderEffect( LivingEntity target, Random random, boolean includePoison ) {
        applyEffectFromTemplate( target, WITCH_EFFECTS[random.nextInt( WITCH_EFFECTS.length - (includePoison ? 0 : 1) )] );
    }
    
    /** Applies a potion effect to the target with default duration. */
    public static void applyEffect( LivingEntity target, Effect effect ) { applyEffect( target, effect, 1, 1.0F ); }
    
    /** Applies a potion effect to the target with default duration and a specified level (amplifier + 1). */
    public static void applyEffect( LivingEntity target, Effect effect, int level ) {
        applyEffect( target, effect, level, 1.0F );
    }
    
    /** Applies a potion effect to the target with default duration and a specified level (amplifier + 1). */
    public static void applyEffect( LivingEntity target, Effect effect, float durationMulti ) {
        applyEffect( target, effect, 1, durationMulti );
    }
    
    /** Applies a potion effect to the target with default duration and a specified level (amplifier + 1). */
    public static void applyEffect( LivingEntity target, Effect effect, int level, float durationMulti ) {
        applyEffect( target, effect, level, effect.isInstantenous() ? 1 :
                (int) (MobHelper.defaultEffectDuration( target.level.getDifficulty() ) * durationMulti) );
    }
    
    /** Applies a potion effect to the target with a specified level (amplifier + 1) and duration. */
    public static void applyDurationEffect( LivingEntity target, Effect effect, int duration ) {
        applyEffect( target, effect, 1, duration );
    }
    
    /** Applies a potion effect to the target with a specified level (amplifier + 1) and duration. */
    public static void applyEffect( LivingEntity target, Effect effect, int level, int duration ) {
        target.addEffect( new EffectInstance( effect, duration, level - 1 ) );
    }
    
    /** Applies a potion effect to the target based on a template effect. The template's duration is used as a multiplier. */
    public static void applyEffectFromTemplate( LivingEntity target, EffectInstance template ) {
        applyEffectFromTemplate( target, template, MobHelper.defaultEffectDuration( target.level.getDifficulty() ) );
    }
    
    /** Applies a potion effect to the target based on a template effect. The template's duration is used as a multiplier. */
    public static void applyEffectFromTemplate( LivingEntity target, EffectInstance template, int baseDuration ) {
        target.addEffect( new EffectInstance( template.getEffect(), template.getEffect().isInstantenous() ? 1 :
                baseDuration * template.getDuration(), template.getAmplifier() ) );
    }
    
    /** Applies a random 'plague' potion effect to the arrow. */
    public static AbstractArrowEntity tipPlagueArrow( AbstractArrowEntity arrow, Random random ) {
        return tipArrowFromTemplate( arrow, PLAGUE_EFFECTS[random.nextInt( PLAGUE_EFFECTS.length -
                (Config.MAIN.GENERAL.enableNausea.get() ? 0 : 1) )] );
    }
    
    //    /** Applies a random 'witch spider' potion effect to the arrow, optionally including poison in the effect pool. */
    //    public static AbstractArrowEntity tipWitchSpiderArrow( AbstractArrowEntity arrow, Random random, boolean includePoison ) {
    //        return tipArrowFromTemplate( arrow, WITCH_EFFECTS[random.nextInt( WITCH_EFFECTS.length - (includePoison ? 0 : 1) )] );
    //    }
    
    /** Applies a potion effect to the arrow with default duration. */
    public static AbstractArrowEntity tipArrow( AbstractArrowEntity arrow, Effect effect ) {
        return tipArrow( arrow, effect, 1, 1.0F );
    }
    
    /** Applies a potion effect to the arrow with default duration and a specified level (amplifier + 1). */
    public static AbstractArrowEntity tipArrow( AbstractArrowEntity arrow, Effect effect, int level ) {
        return tipArrow( arrow, effect, level, 1.0F );
    }
    
    /** Applies a potion effect to the arrow with default duration and a specified level (amplifier + 1). */
    public static AbstractArrowEntity tipArrow( AbstractArrowEntity arrow, Effect effect, float durationMulti ) {
        return tipArrow( arrow, effect, 1, durationMulti );
    }
    
    /** Applies a potion effect to the arrow with default duration and a specified level (amplifier + 1). */
    public static AbstractArrowEntity tipArrow( AbstractArrowEntity arrow, Effect effect, int level, float durationMulti ) {
        return tipArrow( arrow, effect, level, effect.isInstantenous() ? 1 :
                (int) (MobHelper.defaultEffectDuration( arrow.level.getDifficulty() ) * durationMulti) );
    }
    
    /** Applies a potion effect to the arrow with a specified level (amplifier + 1) and duration. */
    public static AbstractArrowEntity tipArrow( AbstractArrowEntity arrow, Effect effect, int level, int duration ) {
        if( arrow instanceof ArrowEntity )
            ((ArrowEntity) arrow).addEffect( new EffectInstance( effect, duration, level - 1 ) );
        return arrow;
    }
    
    /** Applies a potion effect to the arrow based on a template effect. The template's duration is used as a multiplier. */
    public static AbstractArrowEntity tipArrowFromTemplate( AbstractArrowEntity arrow, EffectInstance template ) {
        return tipArrowFromTemplate( arrow, template, MobHelper.defaultEffectDuration( arrow.level.getDifficulty() ) );
    }
    
    /** Applies a potion effect to the arrow based on a template effect. The template's duration is used as a multiplier. */
    public static AbstractArrowEntity tipArrowFromTemplate( AbstractArrowEntity arrow, EffectInstance template, int baseDuration ) {
        if( arrow instanceof ArrowEntity )
            ((ArrowEntity) arrow).addEffect( new EffectInstance( template.getEffect(), template.getEffect().isInstantenous() ? 1 :
                    baseDuration * template.getDuration(), template.getAmplifier() ) );
        return arrow;
    }
    
    /** @return The base debuff duration. */
    public static int defaultEffectDuration( Difficulty difficulty ) {
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
     * Tries to block an incoming damage source on behalf of the blocker, possibly destroying their shield.
     * Note that the blocker will have to call blockUsingShield() and cancel the damage event themselves if this works.
     *
     * @param blocker     The entity that is being attacked.
     * @param source      The damage source.
     * @param needsShield If false, allows the blocker to succeed without a shield.
     * @return True if the block was successful.
     */
    public static boolean tryBlockAttack( LivingEntity blocker, DamageSource source, boolean needsShield ) {
        if( blocker.level.isClientSide() || blocker.isInvulnerableTo( source ) || source.isBypassArmor() ) return false;
        
        // Block everything coming from entities at least 6 blocks away, otherwise 33% block chance
        if( blocker.getRandom().nextFloat() >= 0.33F ) {
            final Entity attacker = source.getEntity();
            if( attacker == null || blocker.distanceToSqr( attacker ) < 36.0 ) return false;
        }
        
        // Cannot block piercing arrows
        final Entity entity = source.getDirectEntity();
        if( entity instanceof AbstractArrowEntity ) {
            final AbstractArrowEntity arrow = (AbstractArrowEntity) entity;
            if( arrow.getPierceLevel() > 0 ) return false;
        }
        
        // Make sure we actually have a shield
        Hand shieldHand = Hand.OFF_HAND;
        ItemStack shield = blocker.getItemInHand( shieldHand );
        if( needsShield && (shield.isEmpty() || !shield.isShield( blocker )) ) {
            shieldHand = Hand.MAIN_HAND;
            shield = blocker.getItemInHand( shieldHand );
            if( shield.isEmpty() || !shield.isShield( blocker ) ) return false;
        }
        
        // Block frontal attacks only
        final Vector3d sourcePos = source.getSourcePosition();
        if( sourcePos != null ) {
            final Vector3d lookVec = blocker.getViewVector( 1.0F );
            Vector3d targetVec = sourcePos.vectorTo( blocker.position() ).normalize();
            targetVec = new Vector3d( targetVec.x, 0.0, targetVec.z );
            if( targetVec.dot( lookVec ) < 0.0 ) {
                blocker.level.playSound( null, blocker.getX() + 0.5D, blocker.getY(), blocker.getZ() + 0.5D, SoundEvents.SHIELD_BLOCK, SoundCategory.NEUTRAL, 0.9F, 1.0F );
                if( needsShield && entity instanceof PlayerEntity ) {
                    maybeDestroyShield( blocker, shield, shieldHand, ((PlayerEntity) entity).getMainHandItem() );
                }
                if( !source.isProjectile() && entity instanceof LivingEntity ) {
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
    private static void maybeDestroyShield( LivingEntity blocker, ItemStack shield, Hand shieldHand, ItemStack weapon ) {
        if( !weapon.isEmpty() && !shield.isEmpty() && weapon.getItem() instanceof AxeItem && shield.getItem() == Items.SHIELD &&
                blocker.getRandom().nextFloat() < 0.25F - EnchantmentHelper.getBlockEfficiency( blocker ) * 0.05F ) {
            blocker.level.playSound( null, blocker.getX() + 0.5D, blocker.getY(), blocker.getZ() + 0.5D, SoundEvents.SHIELD_BREAK, SoundCategory.NEUTRAL, 0.9F, 1.0F );
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
     * @param fluid        The fluid to float in.
     */
    public static void floatInFluid( Entity entity, double acceleration, ITag<Fluid> fluid ) {
        if( entity.tickCount > 1 && entity.getFluidHeight( fluid ) > 0.0 ) {
            if( ISelectionContext.of( entity ).isAbove( FlowingFluidBlock.STABLE_SHAPE, entity.blockPosition(), true ) &&
                    !entity.level.getFluidState( entity.blockPosition().above() ).is( fluid ) ) {
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
     * Should be called in the entity's {@link LivingEntity#onChangedBlock(BlockPos)} method right after super.
     *
     * @param entity The entity.
     * @param pos    The block pos argument from #onChangedBlock.
     */
    public static void updateFrostWalker( LivingEntity entity, BlockPos pos ) { updateFrostWalker( entity, pos, 1 ); }
    
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
        final boolean actualOnGround = entity.isOnGround();
        entity.setOnGround( true ); // Spoof the frost walker enchant requirement to be on the ground
        FrostWalkerEnchantment.onEntityMoved( entity, entity.level, pos, level );
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
        if( entity.tickCount > 1 && entity.isOnGround() && entity.level.random.nextInt( 10 ) == 0 ) {
            if( ISelectionContext.of( entity ).isAbove( FlowingFluidBlock.STABLE_SHAPE, entity.blockPosition(), true ) &&
                    !entity.level.getFluidState( entity.blockPosition().above() ).is( FluidTags.WATER ) ) {
                // Break water plants, otherwise frost walker will not work
                final BlockState block = entity.level.getBlockState( entity.blockPosition() );
                if( block.getMaterial() == Material.WATER_PLANT || block.getMaterial() == Material.REPLACEABLE_WATER_PLANT ) {
                    final TileEntity tileEntity = block.hasTileEntity() ? entity.level.getBlockEntity( entity.blockPosition() ) : null;
                    Block.dropResources( block, entity.level, entity.blockPosition(), tileEntity );
                    entity.level.setBlock( entity.blockPosition(), Blocks.WATER.defaultBlockState(), References.SetBlockFlags.DEFAULTS );
                }
                
                entity.setDeltaMovement( entity.getDeltaMovement().scale( 0.5 ).add( 0.0, 0.4, 0.0 ) );
            }
        }
    }
}