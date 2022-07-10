package fathertoast.specialmobs.common.config.species;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.field.IntField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.util.ConfigUtil;

public class WebSpiderSpeciesConfig extends SpiderSpeciesConfig {
    
    public final Web WEB;
    
    /** Builds the config spec that should be used for this config. */
    public WebSpiderSpeciesConfig( MobFamily.Species<?> species, double spitChance, int minWebs, int maxWebs ) {
        super( species, spitChance );
        
        WEB = new Web( SPEC, species, speciesName, minWebs, maxWebs );
    }
    
    public static class Web extends Config.AbstractCategory {
        
        public final IntField.RandomRange webCount;
        
        Web( ToastConfigSpec parent, MobFamily.Species<?> species, String speciesName, int minWebs, int maxWebs ) {
            super( parent, ConfigUtil.camelCaseToLowerUnderscore( species.specialVariantName ),
                    "Options specific to " + speciesName + "." );
            
            webCount = new IntField.RandomRange(
                    SPEC.define( new IntField( "webs.min", minWebs, IntField.Range.NON_NEGATIVE,
                            "The minimum and maximum (inclusive) number of cobwebs " + speciesName + " can place." ) ),
                    SPEC.define( new IntField( "webs.max", maxWebs, IntField.Range.NON_NEGATIVE ) )
            );
        }
    }
}