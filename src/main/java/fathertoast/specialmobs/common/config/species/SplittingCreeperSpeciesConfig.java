package fathertoast.specialmobs.common.config.species;

import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.IntField;
import fathertoast.specialmobs.common.bestiary.MobFamily;


public class SplittingCreeperSpeciesConfig extends CreeperSpeciesConfig {
    
    public final Splitting SPLITTING;
    
    /** Builds the config spec that should be used for this config. */
    public SplittingCreeperSpeciesConfig( ConfigManager manager, MobFamily.Species<?> species,
                                         boolean cannotExplodeWhileWet, boolean explodeWhileBurning, boolean explodeWhenShot,
                                         int minExtraBabies, int maxExtraBabies ) {
        super( manager, species, cannotExplodeWhileWet, explodeWhileBurning, explodeWhenShot );
        
        SPLITTING = new Splitting( this, species, species.getConfigName(), minExtraBabies, maxExtraBabies );
    }
    
    public static class Splitting extends AbstractConfigCategory<SplittingCreeperSpeciesConfig> {
        
        public final IntField.RandomRange extraBabies;
        
        Splitting( SplittingCreeperSpeciesConfig parent, MobFamily.Species<?> species, String speciesName, int minExtraBabies, int maxExtraBabies ) {
            super( parent, ConfigUtil.camelCaseToLowerUnderscore( species.specialVariantName ),
                    "Options specific to " + speciesName + "." );
            
            extraBabies = new IntField.RandomRange(
                    SPEC.define( new IntField( "extra_babies.min", minExtraBabies, IntField.Range.NON_NEGATIVE,
                            "The minimum and maximum (inclusive) number of extra babies that " + speciesName + " spawn with their explosion.",
                            "This is in addition to the number spawned based on explosion power." ) ),
                    SPEC.define( new IntField( "extra_babies.max", maxExtraBabies, IntField.Range.NON_NEGATIVE ) )
            );
        }
    }
}