package fathertoast.specialmobs.common.config.species;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.field.DoubleField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.util.ConfigUtil;

public class SnowCreeperSpeciesConfig extends CreeperSpeciesConfig {
    
    public final Snow SNOW;
    
    /** Builds the config spec that should be used for this config. */
    public SnowCreeperSpeciesConfig( MobFamily.Species<?> species,
                                     boolean cannotExplodeWhileWet, boolean explodeWhileBurning, boolean explodeWhenShot,
                                     double globeChance ) {
        super( species, cannotExplodeWhileWet, explodeWhileBurning, explodeWhenShot );
        
        SNOW = new Snow( SPEC, species, species.getConfigName(), globeChance );
    }
    
    public static class Snow extends Config.AbstractCategory {
        
        public final DoubleField snowGlobeChance;
        
        Snow( ToastConfigSpec parent, MobFamily.Species<?> species, String speciesName, double globeChance ) {
            super( parent, ConfigUtil.camelCaseToLowerUnderscore( species.specialVariantName ),
                    "Options specific to " + speciesName + "." );
            
            snowGlobeChance = SPEC.define( new DoubleField( "snow_globe_chance", globeChance, DoubleField.Range.PERCENT,
                    "Chance for " + speciesName + " to create a snow globe instead of regular walls when exploding.",
                    "The globe is always chosen if the " + species.getConfigNameSingular() + " is underwater for some reason." ) );
        }
    }
}