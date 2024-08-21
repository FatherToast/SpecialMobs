package fathertoast.specialmobs.common.config.species;

import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.AbstractConfigFile;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.*;
import fathertoast.crust.api.config.common.value.EnvironmentList;
import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.family.FamilyConfig;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.Block;

/**
 * This is the base config for mob species. This may be extended to add categories specific to the species, but all
 * options that are used by all species should be defined in this class.
 */
public class SpeciesConfig extends AbstractConfigFile {
    public static final String SPECIAL_DATA_SUBCAT = "special_data.";
    
    /** Set this field right before creating a new species config; this will then be set as a default value for that config. */
    public static EnvironmentList NEXT_NATURAL_SPAWN_CHANCE_EXCEPTIONS;
    
    protected static String fileName( MobFamily.Species<?> species ) {
        return (species.specialVariantName == null ? "_normal" : ConfigUtil.camelCaseToLowerUnderscore( species.specialVariantName ))
                + "_" + ConfigUtil.noSpaces( species.family.configName );
    }
    
    /** Category containing all options applicable to mob species as a whole; i.e. not specific to any particular species. */
    public final General GENERAL;
    
    /** Builds the config spec that should be used for this config. */
    public SpeciesConfig( ConfigManager manager, MobFamily.Species<?> species ) {
        super( manager, FamilyConfig.dir( species.family ) + "/" + fileName( species ),
                "This config contains options that apply only to the " + species.getConfigNameSingular() + " species." );
        
        GENERAL = new General( this, species, species.getConfigName() );
    }
    
    public static class General extends AbstractConfigCategory<SpeciesConfig> {
        
        public final DoubleField.EnvironmentSensitive naturalSpawnChance;
        
        public final DoubleField randomScaling;
        
        public final AttributeListField attributeChanges;
        
        public final IntField experience;
        public final IntField healTime;
        public final DoubleField fallDamageMultiplier;
        public final BooleanField isImmuneToFire;
        public final BooleanField isImmuneToBurning;
        public final BooleanField canBreatheInWater;
        public final BooleanField ignoreWaterPush;
        public final BooleanField isDamagedByWater;
        public final BooleanField allowLeashing;
        public final BooleanField ignorePressurePlates;
        public final RegistryEntryListField<Block> immuneToStickyBlocks;
        public final RegistryEntryListField<MobEffect> immuneToPotions;
        
        // These are at the end because they may or may not be present (not applicable for all families)
        public final DoubleField rangedAttackDamage;
        public final DoubleField rangedAttackSpread;
        public final DoubleField rangedWalkSpeed;
        public final IntField rangedAttackCooldown;
        public final IntField rangedAttackMaxCooldown;
        public final DoubleField rangedAttackMaxRange;
        
        General( SpeciesConfig parent, MobFamily.Species<?> species, String speciesName ) {
            super( parent, "general",
                    "Options standard to all mob species (that is, not specific to any particular mob species)." );
            final BestiaryInfo info = species.bestiaryInfo;
            
            naturalSpawnChance = new DoubleField.EnvironmentSensitive(
                    SPEC.define( new DoubleField( "natural_spawn_chance.base", 1.0, DoubleField.Range.PERCENT,
                            "The chance for " + speciesName + " to succeed at natural spawn attempts. Does not affect Mob Replacement.",
                            "Note: Most species do NOT naturally spawn - they must be added by a mod or data pack for this option to do anything." ) ),
                    SPEC.define( new EnvironmentListField( "natural_spawn_chance.exceptions", getDefaultSpawnExceptions().setRange( DoubleField.Range.PERCENT ),
                            "The chance for " + speciesName + " to succeed at natural spawn attempts when specific environmental conditions are met." ) )
            );
            
            SPEC.newLine();
            
            randomScaling = SPEC.define( new DoubleField( "random_scaling", -1.0, DoubleField.Range.SIGNED_PERCENT,
                    "When greater than 0, " + speciesName + " will have a random render scale applied. This is a visual effect " +
                            "only. If this is set to a non-negative value, it overrides the value set for both \"master_random_scaling\" " +
                            "and \"family_random_scaling\". The priority is species value > family value > master value." ) );
            
            SPEC.newLine();
            
            attributeChanges = SPEC.define( new AttributeListField( "attributes", info.defaultAttributes,
                    "Attribute modifiers for " + speciesName + ". If no attribute changes are defined here, " +
                            speciesName + " will have the exact same attributes as their parent vanilla mob." ) );
            
            SPEC.increaseIndent();
            SPEC.subcategory( SPECIAL_DATA_SUBCAT.substring( 0, SPECIAL_DATA_SUBCAT.length() - 1 ),
                    "Special Mob Data. These are the values set to each " + species.getConfigNameSingular() + " on spawn (in their NBT)." );
            
            experience = SPEC.define( new IntField( SPECIAL_DATA_SUBCAT + "experience", info.experience, IntField.Range.NON_NEGATIVE,
                    "The amount of experience " + speciesName + " drop when killed by a player. Multiplied by 2.5 for babies. " +
                            "Extra experience may drop based on equipment. Slime-style mobs also drop experience equal to slime size." ) );
            healTime = SPEC.define( new IntField( SPECIAL_DATA_SUBCAT + "heal_time", info.healTime, IntField.Range.NON_NEGATIVE,
                    "If greater than 0, " + speciesName + " will heal 1 half-heart of health every \"heal_time\" ticks. (20 ticks = 1 second)" ) );
            fallDamageMultiplier = SPEC.define( new DoubleField( SPECIAL_DATA_SUBCAT + "fall_damage_multiplier", info.fallDamageMultiplier, DoubleField.Range.NON_NEGATIVE,
                    "Fall damage taken by " + speciesName + " is multiplied by this value. 0 is fall damage immunity." ) );
            isImmuneToFire = SPEC.define( new BooleanField( SPECIAL_DATA_SUBCAT + "immune_to_fire", info.isImmuneToFire,
                    "If true, " + speciesName + " will take no fire damage. Does not affect spawn restrictions." ) );
            isImmuneToBurning = SPEC.define( new BooleanField( SPECIAL_DATA_SUBCAT + "immune_to_burning", info.isImmuneToBurning,
                    "If true, " + speciesName + " cannot be set on fire (this setting only matters if not immune to fire)." ) );
            canBreatheInWater = SPEC.define( new BooleanField( SPECIAL_DATA_SUBCAT + "immune_to_drowning", info.canBreatheInWater,
                    "If true, " + speciesName + " can breathe in water." ) );
            ignoreWaterPush = SPEC.define( new BooleanField( SPECIAL_DATA_SUBCAT + "immune_to_fluid_push", info.ignoreWaterPush,
                    "If true, " + speciesName + " will ignore forces applied by flowing fluids." ) );
            isDamagedByWater = SPEC.define( new BooleanField( SPECIAL_DATA_SUBCAT + "sensitive_to_water", info.isDamagedByWater,
                    "If true, " + speciesName + " will be continuously damaged while wet." ) );
            allowLeashing = SPEC.define( new BooleanField( SPECIAL_DATA_SUBCAT + "allow_leashing", info.allowLeashing,
                    "If true, " + speciesName + " can be leashed. (Note: Leashed mobs can still attack you.)" ) );
            ignorePressurePlates = SPEC.define( new BooleanField( SPECIAL_DATA_SUBCAT + "immune_to_pressure_plates", info.ignorePressurePlates,
                    "If true, " + speciesName + " will not trigger pressure plates." ) );
            immuneToStickyBlocks = SPEC.define( new LazyRegistryEntryListField<>( SPECIAL_DATA_SUBCAT + "immune_to_sticky_blocks", info.immuneToStickyBlocks,
                    ConfigUtil.properCase( speciesName ) + " will not be 'trapped' in any blocks specified here (e.g. \"cobweb\" or \"sweet_berry_bush\")." ) );
            immuneToPotions = SPEC.define( new LazyRegistryEntryListField<>( SPECIAL_DATA_SUBCAT + "immune_to_effects", info.immuneToPotions,
                    ConfigUtil.properCase( speciesName ) + " cannot be inflicted with any effects specified here (e.g. \"instant_damage\" or \"regeneration\")." ) );
            
            if( hasNoRangedStats( info ) ) {
                SPEC.decreaseIndent();
                rangedAttackDamage = rangedAttackSpread = rangedWalkSpeed = rangedAttackMaxRange = null;
                rangedAttackCooldown = rangedAttackMaxCooldown = null;
                return;
            }
            
            SPEC.subcategory( "ranged_stats",
                    "Like Special Mob Data, these are set to NBT on spawn. Unlike SMD, ranged stats are not all applicable to all mobs." );
            
            rangedAttackDamage = info.rangedAttackDamage < 0.0F ? null :
                    SPEC.define( new DoubleField( SPECIAL_DATA_SUBCAT + "ranged_attack.damage", info.rangedAttackDamage, DoubleField.Range.NON_NEGATIVE,
                            "The base ranged attack damage for " + speciesName + " (in half-hearts)." ) );
            rangedAttackSpread = info.rangedAttackSpread < 0.0F ? null :
                    SPEC.define( new DoubleField( SPECIAL_DATA_SUBCAT + "ranged_attack.spread", info.rangedAttackSpread, DoubleField.Range.NON_NEGATIVE,
                            "The ranged attack spread (inaccuracy) for " + speciesName + ". 0 is perfect accuracy." ) );
            rangedWalkSpeed = info.rangedWalkSpeed < 0.0F ? null :
                    SPEC.define( new DoubleField( SPECIAL_DATA_SUBCAT + "ranged_attack.walk_speed", info.rangedWalkSpeed, DoubleField.Range.NON_NEGATIVE,
                            "The walk speed multiplier for " + speciesName + " while using their ranged attack AI." ) );
            rangedAttackCooldown = info.rangedAttackCooldown < 0 ? null :
                    SPEC.define( new IntField( SPECIAL_DATA_SUBCAT + "ranged_attack.charge_time", info.rangedAttackCooldown, IntField.Range.NON_NEGATIVE,
                            "The delay (in ticks) for " + speciesName + " to perform a ranged attack from rest. (20 ticks = 1 second)" ) );
            rangedAttackMaxCooldown = info.rangedAttackMaxCooldown < 0 ? null :
                    SPEC.define( new IntField( SPECIAL_DATA_SUBCAT + "ranged_attack.refire_time", info.rangedAttackMaxCooldown, IntField.Range.NON_NEGATIVE,
                            "The total delay (in ticks) " + speciesName + " wait between each ranged attack. (20 ticks = 1 second)" ) );
            rangedAttackMaxRange = info.rangedAttackMaxRange < 0.0F ? null :
                    SPEC.define( new DoubleField( SPECIAL_DATA_SUBCAT + "ranged_attack.max_range", info.rangedAttackMaxRange, DoubleField.Range.NON_NEGATIVE,
                            "The maximum distance (in blocks) at which " + speciesName + " can use their ranged attacks. " +
                                    "0 disables ranged attacks." ) );
            
            SPEC.decreaseIndent();
        }
        
        /** @return True if the config should NOT have any ranged stat fields. */
        private static boolean hasNoRangedStats( BestiaryInfo info ) {
            return info.rangedAttackDamage < 0.0F && info.rangedAttackSpread < 0.0F && info.rangedWalkSpeed < 0.0F &&
                    info.rangedAttackCooldown < 0 && info.rangedAttackMaxCooldown < 0 && info.rangedAttackMaxRange < 0.0F;
        }
        
        /** @return The next default natural spawn chance exceptions to use. */
        private static EnvironmentList getDefaultSpawnExceptions() {
            if( NEXT_NATURAL_SPAWN_CHANCE_EXCEPTIONS == null ) return new EnvironmentList();
            
            // A hacky way to have an extra optional constructor parameter without overloading EVERY SINGLE constructor
            final EnvironmentList presetValue = NEXT_NATURAL_SPAWN_CHANCE_EXCEPTIONS;
            NEXT_NATURAL_SPAWN_CHANCE_EXCEPTIONS = null;
            return presetValue;
        }
    }
}