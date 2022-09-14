package fathertoast.specialmobs.common.config.species;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.field.IntField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.util.ConfigUtil;

public class UndeadWitchSpeciesConfig extends SpeciesConfig {
    
    public final Undead UNDEAD;
    
    /** Builds the config spec that should be used for this config. */
    public UndeadWitchSpeciesConfig( MobFamily.Species<?> species, int minSummons, int maxSummons ) {
        super( species );
        
        UNDEAD = new Undead( SPEC, species, species.getConfigName(), minSummons, maxSummons );
    }
    
    public static class Undead extends Config.AbstractCategory {
        
        public final IntField.RandomRange summons;
        
        Undead( ToastConfigSpec parent, MobFamily.Species<?> species, String speciesName, int minSummons, int maxSummons ) {
            super( parent, ConfigUtil.camelCaseToLowerUnderscore( species.specialVariantName ),
                    "Options specific to " + speciesName + "." );
            
            summons = new IntField.RandomRange(
                    SPEC.define( new IntField( "summons.min", minSummons, IntField.Range.NON_NEGATIVE,
                            "The minimum and maximum (inclusive) number of times " + speciesName + " can summon minions." ) ),
                    SPEC.define( new IntField( "summons.max", maxSummons, IntField.Range.NON_NEGATIVE ) )
            );
        }
    }
}