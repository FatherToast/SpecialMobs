package fathertoast.specialmobs.common.config.species;

import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.IntField;
import fathertoast.specialmobs.common.bestiary.MobFamily;

public class MadScientistZombieSpeciesConfig extends ZombieSpeciesConfig {
    
    public final MadScientist MAD_SCIENTIST;
    
    /** Builds the config spec that should be used for this config. */
    public MadScientistZombieSpeciesConfig( ConfigManager manager, MobFamily.Species<?> species, double bowChance, double shieldChance, int minCharges, int maxCharges ) {
        super( manager, species, bowChance, shieldChance );
        
        MAD_SCIENTIST = new MadScientist( this, species, species.getConfigName(), minCharges, maxCharges );
    }
    
    public static class MadScientist extends AbstractConfigCategory<MadScientistZombieSpeciesConfig> {
        
        public final IntField.RandomRange chargeCount;
        
        MadScientist( MadScientistZombieSpeciesConfig parent, MobFamily.Species<?> species, String speciesName, int minCharges, int maxCharges ) {
            super( parent, ConfigUtil.camelCaseToLowerUnderscore( species.specialVariantName ),
                    "Options specific to " + speciesName + "." );
            
            chargeCount = new IntField.RandomRange(
                    SPEC.define( new IntField( "charges.min", minCharges, IntField.Range.NON_NEGATIVE,
                            "The minimum and maximum (inclusive) number of creepers a " + speciesName + " can charge." ) ),
                    SPEC.define( new IntField( "charges.max", maxCharges, IntField.Range.NON_NEGATIVE ) )
            );
        }
    }
}