package fathertoast.specialmobs.common.config.family;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.field.DoubleField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.util.ConfigUtil;

/**
 * This is the family config for silverfish.
 */
public class SilverfishFamilyConfig extends FamilyConfig {
    
    public final Silverfish SILVERFISH;
    
    /** Builds the config spec that should be used for this config. */
    public SilverfishFamilyConfig( MobFamily<?, ?> family ) {
        super( family );
        
        SILVERFISH = new Silverfish( SPEC, family );
    }
    
    public static class Silverfish extends Config.AbstractCategory {
        
        public final DoubleField familyAggressiveChance;
        
        Silverfish( ToastConfigSpec parent, MobFamily<?, ?> family ) {
            super( parent, ConfigUtil.noSpaces( family.configName ),
                    "Options specific to the family of " + family.configName + "." );
            
            familyAggressiveChance = SPEC.define( new DoubleField( "family_aggressive_chance", 0.05, DoubleField.Range.PERCENT,
                    "Chance for " + family.configName + " to spawn already calling for reinforcements.",
                    "By default, this applies to all " + family.configName + "; but species configs can override it." ) );
        }
    }
}