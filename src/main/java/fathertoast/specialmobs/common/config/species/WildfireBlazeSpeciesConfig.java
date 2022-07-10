package fathertoast.specialmobs.common.config.species;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.field.IntField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.util.ConfigUtil;

public class WildfireBlazeSpeciesConfig extends BlazeSpeciesConfig {
    
    public final Wildfire WILDFIRE;
    
    /** Builds the config spec that should be used for this config. */
    public WildfireBlazeSpeciesConfig( MobFamily.Species<?> species, int fireballBurstCount, int fireballBurstDelay,
                                       int minBabies, int maxBabies, int minSummons, int maxSummons ) {
        super( species, fireballBurstCount, fireballBurstDelay );
        
        WILDFIRE = new Wildfire( SPEC, species, speciesName, minBabies, maxBabies, minSummons, maxSummons );
    }
    
    public static class Wildfire extends Config.AbstractCategory {
        
        public final IntField.RandomRange babies;
        
        public final IntField.RandomRange summons;
        
        Wildfire( ToastConfigSpec parent, MobFamily.Species<?> species, String speciesName,
                  int minBabies, int maxBabies, int minSummons, int maxSummons ) {
            super( parent, ConfigUtil.camelCaseToLowerUnderscore( species.specialVariantName ),
                    "Options specific to " + speciesName + "." );
            
            babies = new IntField.RandomRange(
                    SPEC.define( new IntField( "babies.min", minBabies, IntField.Range.NON_NEGATIVE,
                            "The minimum and maximum (inclusive) number of babies " + speciesName + " spawn on death." ) ),
                    SPEC.define( new IntField( "babies.max", maxBabies, IntField.Range.NON_NEGATIVE ) )
            );
            
            SPEC.newLine();
            
            summons = new IntField.RandomRange(
                    SPEC.define( new IntField( "summons.min", minSummons, IntField.Range.NON_NEGATIVE,
                            "The minimum and maximum (inclusive) number of times " + speciesName + " can summon minions." ) ),
                    SPEC.define( new IntField( "summons.max", maxSummons, IntField.Range.NON_NEGATIVE ) )
            );
        }
    }
}