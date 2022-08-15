package fathertoast.specialmobs.common.config.species;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.field.IntField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.util.ConfigUtil;

public class SplittingCreeperSpeciesConfig extends CreeperSpeciesConfig {
    
    public final Splitting SPLITTING;
    
    /** Builds the config spec that should be used for this config. */
    public SplittingCreeperSpeciesConfig( MobFamily.Species<?> species,
                                          boolean cannotExplodeWhileWet, boolean explodeWhileBurning, boolean explodeWhenShot,
                                          int minExtraBabies, int maxExtraBabies ) {
        super( species, cannotExplodeWhileWet, explodeWhileBurning, explodeWhenShot );
        
        SPLITTING = new Splitting( SPEC, species, species.getConfigName(), minExtraBabies, maxExtraBabies );
    }
    
    public static class Splitting extends Config.AbstractCategory {
        
        public final IntField.RandomRange extraBabies;
        
        Splitting( ToastConfigSpec parent, MobFamily.Species<?> species, String speciesName, int minExtraBabies, int maxExtraBabies ) {
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