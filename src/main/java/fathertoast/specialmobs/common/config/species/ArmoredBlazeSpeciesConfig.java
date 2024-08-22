package fathertoast.specialmobs.common.config.species;

import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.IntField;
import fathertoast.specialmobs.common.bestiary.MobFamily;


public class ArmoredBlazeSpeciesConfig extends BlazeSpeciesConfig {
    
    public final Armored ARMORED;
    
    /** Builds the config spec that should be used for this config. */
    public ArmoredBlazeSpeciesConfig( ConfigManager manager, MobFamily.Species<?> species, int fireballBurstCount, int fireballBurstDelay,
                                      int minArmor, int maxArmor ) {
        super( manager, species, fireballBurstCount, fireballBurstDelay );
        
        ARMORED = new Armored( this, species, species.getConfigName(), minArmor, maxArmor );
    }
    
    public static class Armored extends AbstractConfigCategory<ArmoredBlazeSpeciesConfig> {
        
        public final IntField.RandomRange armor;
        
        Armored( ArmoredBlazeSpeciesConfig parent, MobFamily.Species<?> species, String speciesName,
                 int minArmor, int maxArmor ) {
            super( parent, ConfigUtil.camelCaseToLowerUnderscore( species.specialVariantName ),
                    "Options specific to " + speciesName + "." );
            
            armor = new IntField.RandomRange(
                    SPEC.define( new IntField( "armor.min", minArmor, IntField.Range.NON_NEGATIVE,
                            "The minimum and maximum (inclusive) number of hits " + speciesName + " can absorb before their super armor breaks." ) ),
                    SPEC.define( new IntField( "armor.max", maxArmor, IntField.Range.NON_NEGATIVE ) )
            );
        }
    }
}