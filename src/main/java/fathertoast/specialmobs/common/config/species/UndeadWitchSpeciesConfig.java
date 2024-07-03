package fathertoast.specialmobs.common.config.species;

import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.IntField;
import fathertoast.specialmobs.common.bestiary.MobFamily;

public class UndeadWitchSpeciesConfig extends SpeciesConfig {
    
    public final Undead UNDEAD;
    
    /** Builds the config spec that should be used for this config. */
    public UndeadWitchSpeciesConfig( ConfigManager manager, MobFamily.Species<?> species, int minSummons, int maxSummons ) {
        super( manager, species );
        
        UNDEAD = new Undead( this, species, species.getConfigName(), minSummons, maxSummons );
    }
    
    public static class Undead extends AbstractConfigCategory<UndeadWitchSpeciesConfig> {
        
        public final IntField.RandomRange summons;
        
        Undead( UndeadWitchSpeciesConfig parent, MobFamily.Species<?> species, String speciesName, int minSummons, int maxSummons ) {
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