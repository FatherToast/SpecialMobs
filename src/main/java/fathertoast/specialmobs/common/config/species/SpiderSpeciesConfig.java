package fathertoast.specialmobs.common.config.species;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.field.DoubleField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.util.ConfigUtil;

/**
 * This is the base species config for spiders and cave spiders.
 */
public class SpiderSpeciesConfig extends SpeciesConfig {
    
    public final Spiders SPIDERS;
    
    /** Builds the config spec that should be used for this config. */
    public SpiderSpeciesConfig( MobFamily.Species<?> species, double spitChance ) {
        super( species );
        
        SPIDERS = new Spiders( SPEC, species, speciesName, spitChance );
    }
    
    public static class Spiders extends Config.AbstractCategory {
        
        public final DoubleField spitterChance;
        
        Spiders( ToastConfigSpec parent, MobFamily.Species<?> species, String speciesName, double spitChance ) {
            super( parent, ConfigUtil.noSpaces( species.family.configName ),
                    "Options standard to all " + species.family.configName + "." );
            
            // Automatically set the default spitter chance to 0% if the mob has ranged attacks disabled by default
            final double effectiveDefault = species.bestiaryInfo.rangedAttackMaxRange > 0.0F ? spitChance : 0.0;
            spitterChance = SPEC.define( new DoubleField( "spitter_chance", effectiveDefault, DoubleField.Range.PERCENT,
                    "Chance for " + speciesName + " to spawn as a 'spitter', which enables their ranged attack (if max range > 0)." ) );
        }
    }
}