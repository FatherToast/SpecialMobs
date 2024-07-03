package fathertoast.specialmobs.common.config.species;

import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.IntField;
import fathertoast.specialmobs.common.bestiary.MobFamily;

public class WebSpiderSpeciesConfig extends SpiderSpeciesConfig {
    
    public final Web WEB;
    
    /** Builds the config spec that should be used for this config. */
    public WebSpiderSpeciesConfig( ConfigManager manager, MobFamily.Species<?> species, double spitChance, int minWebs, int maxWebs ) {
        super( manager, species, spitChance );
        
        WEB = new Web( this, species, species.getConfigName(), minWebs, maxWebs );
    }
    
    public static class Web extends AbstractConfigCategory<WebSpiderSpeciesConfig> {
        
        public final IntField.RandomRange webCount;
        
        Web( WebSpiderSpeciesConfig parent, MobFamily.Species<?> species, String speciesName, int minWebs, int maxWebs ) {
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