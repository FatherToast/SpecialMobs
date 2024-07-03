package fathertoast.specialmobs.common.config.species;

import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.DoubleField;
import fathertoast.specialmobs.common.bestiary.MobFamily;

/**
 * This is the base species config for spiders and cave spiders.
 */
public class SpiderSpeciesConfig extends SpeciesConfig {
    
    public final Spiders SPIDERS;
    
    /** Builds the config spec that should be used for this config. */
    public SpiderSpeciesConfig( ConfigManager manager, MobFamily.Species<?> species, double spitChance ) {
        super( manager, species );
        
        SPIDERS = new Spiders( this, species, species.getConfigName(), spitChance );
    }
    
    public static class Spiders extends AbstractConfigCategory<SpiderSpeciesConfig> {
        
        public final DoubleField spitterChance;
        
        Spiders( SpiderSpeciesConfig parent, MobFamily.Species<?> species, String speciesName, double spitChance ) {
            super( parent, ConfigUtil.noSpaces( species.family.configName ),
                    "Options standard to all " + species.family.configName + "." );
            
            // Automatically set the default spitter chance to 0% if the mob has ranged attacks disabled by default
            final double effectiveDefault = species.bestiaryInfo.rangedAttackMaxRange > 0.0F ? spitChance : 0.0;
            spitterChance = SPEC.define( new DoubleField( "spitter_chance", effectiveDefault, DoubleField.Range.PERCENT,
                    "Chance for " + speciesName + " to spawn as a 'spitter', which enables their ranged attack (if max range > 0)." ) );
        }
    }
}