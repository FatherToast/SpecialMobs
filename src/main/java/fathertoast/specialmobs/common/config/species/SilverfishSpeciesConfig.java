package fathertoast.specialmobs.common.config.species;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.field.DoubleField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.util.ConfigUtil;

/**
 * This is the base species config for silverfish.
 */
public class SilverfishSpeciesConfig extends SpeciesConfig {
    
    public final Silverfish SILVERFISH;
    
    /** Builds the config spec that should be used for this config. */
    public SilverfishSpeciesConfig( MobFamily.Species<?> species, double spitChance ) {
        super( species );
        
        SILVERFISH = new Silverfish( SPEC, species, species.getConfigName(), spitChance );
    }
    
    public static class Silverfish extends Config.AbstractCategory {
        
        public final DoubleField aggressiveChance;
        
        public final DoubleField spitterChance;
        
        Silverfish( ToastConfigSpec parent, MobFamily.Species<?> species, String speciesName, double spitChance ) {
            super( parent, ConfigUtil.noSpaces( species.family.configName ),
                    "Options standard to all " + species.family.configName + "." );
            
            aggressiveChance = SPEC.define( new DoubleField( "aggressive_chance", -1.0, DoubleField.Range.SIGNED_PERCENT,
                    "Chance for " + speciesName + " to spawn already calling for reinforcements.",
                    "If this is set to a non-negative value, it overrides the value set for \"family_aggressive_chance\"." ) );
            
            SPEC.newLine();
            
            // Automatically set the default spitter chance to 0% if the mob has ranged attacks disabled by default
            final double effectiveDefault = species.bestiaryInfo.rangedAttackMaxRange > 0.0F ? spitChance : 0.0;
            spitterChance = SPEC.define( new DoubleField( "spitter_chance", effectiveDefault, DoubleField.Range.PERCENT,
                    "Chance for " + speciesName + " to spawn as a 'spitter', which enables their ranged attack (if max range > 0)." ) );
        }
    }
}