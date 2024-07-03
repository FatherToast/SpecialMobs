package fathertoast.specialmobs.common.config.species;

import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.DoubleField;
import fathertoast.specialmobs.common.bestiary.MobFamily;

public class SnowCreeperSpeciesConfig extends CreeperSpeciesConfig {
    
    public final Snow SNOW;
    
    /** Builds the config spec that should be used for this config. */
    public SnowCreeperSpeciesConfig( ConfigManager manager, MobFamily.Species<?> species,
                                    boolean cannotExplodeWhileWet, boolean explodeWhileBurning, boolean explodeWhenShot,
                                    double globeChance ) {
        super( manager, species, cannotExplodeWhileWet, explodeWhileBurning, explodeWhenShot );
        
        SNOW = new Snow( this, species, species.getConfigName(), globeChance );
    }
    
    public static class Snow extends AbstractConfigCategory<SnowCreeperSpeciesConfig> {
        
        public final DoubleField snowGlobeChance;
        
        Snow( SnowCreeperSpeciesConfig parent, MobFamily.Species<?> species, String speciesName, double globeChance ) {
            super( parent, ConfigUtil.camelCaseToLowerUnderscore( species.specialVariantName ),
                    "Options specific to " + speciesName + "." );
            
            snowGlobeChance = SPEC.define( new DoubleField( "snow_globe_chance", globeChance, DoubleField.Range.PERCENT,
                    "Chance for " + speciesName + " to create a snow globe instead of regular walls when exploding.",
                    "The globe is always chosen if the " + species.getConfigNameSingular() + " is underwater for some reason." ) );
        }
    }
}