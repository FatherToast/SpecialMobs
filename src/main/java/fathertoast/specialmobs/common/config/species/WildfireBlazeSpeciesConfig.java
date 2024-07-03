package fathertoast.specialmobs.common.config.species;

import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.IntField;
import fathertoast.specialmobs.common.bestiary.MobFamily;


public class WildfireBlazeSpeciesConfig extends BlazeSpeciesConfig {
    
    public final Wildfire WILDFIRE;
    
    /** Builds the config spec that should be used for this config. */
    public WildfireBlazeSpeciesConfig( ConfigManager manager, MobFamily.Species<?> species, int fireballBurstCount, int fireballBurstDelay,
                                      int minBabies, int maxBabies, int minSummons, int maxSummons ) {
        super( manager, species, fireballBurstCount, fireballBurstDelay );
        
        WILDFIRE = new Wildfire( this, species, species.getConfigName(), minBabies, maxBabies, minSummons, maxSummons );
    }
    
    public static class Wildfire extends AbstractConfigCategory<WildfireBlazeSpeciesConfig> {
        
        public final IntField.RandomRange babies;
        
        public final IntField.RandomRange summons;
        
        Wildfire( WildfireBlazeSpeciesConfig parent, MobFamily.Species<?> species, String speciesName,
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