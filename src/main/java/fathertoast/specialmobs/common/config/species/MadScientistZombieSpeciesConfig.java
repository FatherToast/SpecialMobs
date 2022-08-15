package fathertoast.specialmobs.common.config.species;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.field.IntField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.util.ConfigUtil;

public class MadScientistZombieSpeciesConfig extends ZombieSpeciesConfig {
    
    public final MadScientist MAD_SCIENTIST;
    
    /** Builds the config spec that should be used for this config. */
    public MadScientistZombieSpeciesConfig( MobFamily.Species<?> species, double bowChance, double shieldChance, int minCharges, int maxCharges ) {
        super( species, bowChance, shieldChance );
        
        MAD_SCIENTIST = new MadScientist( SPEC, species, species.getConfigName(), minCharges, maxCharges );
    }
    
    public static class MadScientist extends Config.AbstractCategory {
        
        public final IntField.RandomRange chargeCount;
        
        MadScientist( ToastConfigSpec parent, MobFamily.Species<?> species, String speciesName, int minCharges, int maxCharges ) {
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