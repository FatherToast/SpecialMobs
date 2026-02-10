package fathertoast.specialmobs.common.config.species;

import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.DoubleField;
import fathertoast.specialmobs.common.bestiary.MobFamily;

public class ImplodingCreeperSpeciesConfig extends CreeperSpeciesConfig {
    
    public final Imploding IMPLODING;
    
    /** Builds the config spec that should be used for this config. */
    public ImplodingCreeperSpeciesConfig( ConfigManager manager, MobFamily.Species<?> species,
                                          boolean cannotExplodeWhileWet, boolean explodeWhileBurning, boolean explodeWhenShot,
                                          double pullStrength, double pullRadius ) {
        super( manager, species, cannotExplodeWhileWet, explodeWhileBurning, explodeWhenShot );
        
        IMPLODING = new Imploding( this, species, species.getConfigName(), pullStrength, pullRadius );
    }
    
    public static class Imploding extends AbstractConfigCategory<ImplodingCreeperSpeciesConfig> {
        
        public final DoubleField basePullStrength;
        
        public final DoubleField basePullRadius;
        
        Imploding( ImplodingCreeperSpeciesConfig parent, MobFamily.Species<?> species, String speciesName,
                   double pullStrength, double pullRadius ) {
            super( parent, ConfigUtil.camelCaseToLowerUnderscore( species.specialVariantName ),
                    "Options specific to " + speciesName + "." );
            
            basePullStrength = SPEC.define( new DoubleField( "base_pull_strength", pullStrength, DoubleField.Range.NON_NEGATIVE,
                    "The strength of the pull from " + speciesName + " implosions." ) );
            
            basePullRadius = SPEC.define( new DoubleField( "base_pull_radius", pullRadius, DoubleField.Range.NON_NEGATIVE,
                    "The pull radius " + speciesName + " implosions. Any entities within this distance will get pulled by the implosion." ) );
        }
    }
}
