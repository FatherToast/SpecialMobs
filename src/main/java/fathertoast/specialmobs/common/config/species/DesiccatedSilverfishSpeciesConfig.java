package fathertoast.specialmobs.common.config.species;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.field.IntField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.util.ConfigUtil;

public class DesiccatedSilverfishSpeciesConfig extends SilverfishSpeciesConfig {
    
    public final Desiccated DESICCATED;
    
    /** Builds the config spec that should be used for this config. */
    public DesiccatedSilverfishSpeciesConfig( MobFamily.Species<?> species, double spitChance, int minAbsorb, int maxAbsorb ) {
        super( species, spitChance );
        
        DESICCATED = new Desiccated( SPEC, species, speciesName, minAbsorb, maxAbsorb );
    }
    
    public static class Desiccated extends Config.AbstractCategory {
        
        public final IntField.RandomRange absorbCount;
        
        Desiccated( ToastConfigSpec parent, MobFamily.Species<?> species, String speciesName, int minAbsorb, int maxAbsorb ) {
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