package fathertoast.specialmobs.common.config.species;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.field.IntField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.util.ConfigUtil;

public class MotherSpiderSpeciesConfig extends SpiderSpeciesConfig {
    
    public final Mother MOTHER;
    
    /** Builds the config spec that should be used for this config. */
    public MotherSpiderSpeciesConfig( MobFamily.Species<?> species, double spitChance,
                                      int minBabies, int maxBabies, int minExtraBabies, int maxExtraBabies ) {
        super( species, spitChance );
        
        MOTHER = new Mother( SPEC, species, species.getConfigName(), minBabies, maxBabies, minExtraBabies, maxExtraBabies );
    }
    
    public static class Mother extends Config.AbstractCategory {
        
        public final IntField.RandomRange babies;
        
        public final IntField.RandomRange extraBabies;
        
        Mother( ToastConfigSpec parent, MobFamily.Species<?> species, String speciesName,
                int minBabies, int maxBabies, int minExtraBabies, int maxExtraBabies ) {
            super( parent, ConfigUtil.camelCaseToLowerUnderscore( species.specialVariantName ),
                    "Options specific to " + speciesName + "." );
            
            babies = new IntField.RandomRange(
                    SPEC.define( new IntField( "babies.min", minBabies, IntField.Range.NON_NEGATIVE,
                            "The minimum and maximum (inclusive) number of babies " + speciesName + " spawn on death.",
                            "Any remaining 'extra babies' will added to the amount spawned on death (see below)." ) ),
                    SPEC.define( new IntField( "babies.max", maxBabies, IntField.Range.NON_NEGATIVE ) )
            );
            
            SPEC.newLine();
            
            extraBabies = new IntField.RandomRange(
                    SPEC.define( new IntField( "extra_babies.min", minExtraBabies, IntField.Range.NON_NEGATIVE,
                            "The minimum and maximum (inclusive) number of babies that " + speciesName + " can spawn from hits before death.",
                            "Any remaining 'extra babies' will added to the amount spawned on death (see above)." ) ),
                    SPEC.define( new IntField( "extra_babies.max", maxExtraBabies, IntField.Range.NON_NEGATIVE ) )
            );
        }
    }
}