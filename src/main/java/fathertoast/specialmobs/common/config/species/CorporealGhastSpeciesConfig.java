package fathertoast.specialmobs.common.config.species;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.field.IntField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.util.ConfigUtil;

public class CorporealGhastSpeciesConfig extends SpeciesConfig {
    
    public final CorporealShift CORPOREAL_SHIFT;
    
    /** Builds the config spec that should be used for this config. */
    public CorporealGhastSpeciesConfig( MobFamily.Species<?> species, int corporealTime, int incorporealTime ) {
        super( species );
        
        CORPOREAL_SHIFT = new CorporealShift( SPEC, species, species.getConfigName(), corporealTime, incorporealTime );
    }
    
    public static class CorporealShift extends Config.AbstractCategory {
        
        public final IntField corporealTicks;
        public final IntField incorporealTicks;
        
        CorporealShift( ToastConfigSpec parent, MobFamily.Species<?> species, String speciesName,
                        int corporealTime, int incorporealTime ) {
            super( parent, ConfigUtil.camelCaseToLowerUnderscore( species.specialVariantName ),
                    "Options specific to " + speciesName + "." );
            
            corporealTicks = SPEC.define( new IntField( "ticks.corporeal", corporealTime, IntField.Range.NON_NEGATIVE,
                    "The number of ticks " + speciesName + " stay in 'corporeal' mode before shifting (20 ticks = 1 second).",
                    "In corporeal mode, " + speciesName + " can be damaged and shoot like normal " + species.family.configName + "." ) );
            incorporealTicks = SPEC.define( new IntField( "ticks.incorporeal", incorporealTime, IntField.Range.NON_NEGATIVE,
                    "The number of ticks " + speciesName + " stay in 'incorporeal' mode before shifting (20 ticks = 1 second).",
                    "In incorporeal mode, " + speciesName + " are immune to damage and shoot unique fireballs that punish movement." ) );
        }
    }
}