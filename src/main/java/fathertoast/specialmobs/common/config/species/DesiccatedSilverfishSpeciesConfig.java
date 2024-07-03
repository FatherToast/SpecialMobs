package fathertoast.specialmobs.common.config.species;

import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.IntField;
import fathertoast.specialmobs.common.bestiary.MobFamily;

public class DesiccatedSilverfishSpeciesConfig extends SilverfishSpeciesConfig {
    
    public final Desiccated DESICCATED;
    
    /** Builds the config spec that should be used for this config. */
    public DesiccatedSilverfishSpeciesConfig( ConfigManager manager, MobFamily.Species<?> species, double spitChance, int minAbsorb, int maxAbsorb ) {
        super( manager, species, spitChance );
        
        DESICCATED = new Desiccated( this, species, species.getConfigName(), minAbsorb, maxAbsorb );
    }
    
    public static class Desiccated extends AbstractConfigCategory<DesiccatedSilverfishSpeciesConfig> {
        
        public final IntField.RandomRange absorbCount;
        
        Desiccated( DesiccatedSilverfishSpeciesConfig parent, MobFamily.Species<?> species, String speciesName, int minAbsorb, int maxAbsorb ) {
            super( parent, ConfigUtil.camelCaseToLowerUnderscore( species.specialVariantName ),
                    "Options specific to " + speciesName + "." );
            
            absorbCount = new IntField.RandomRange(
                    SPEC.define( new IntField( "water_absorbed.min", minAbsorb, IntField.Range.NON_NEGATIVE,
                            "The minimum and maximum (inclusive) number of water blocks " + speciesName + " can absorb." ) ),
                    SPEC.define( new IntField( "water_absorbed.max", maxAbsorb, IntField.Range.NON_NEGATIVE ) )
            );
        }
    }
}