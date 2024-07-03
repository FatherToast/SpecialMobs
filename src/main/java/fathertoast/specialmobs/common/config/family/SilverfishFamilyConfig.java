package fathertoast.specialmobs.common.config.family;

import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.DoubleField;
import fathertoast.specialmobs.common.bestiary.MobFamily;

/**
 * This is the family config for silverfish.
 */
public class SilverfishFamilyConfig extends FamilyConfig {
    
    public final Silverfish SILVERFISH;
    
    /** Builds the config spec that should be used for this config. */
    public SilverfishFamilyConfig( ConfigManager manager, MobFamily<?, ?> family ) {
        super( manager, family, VARIANT_CHANCE_LOW );
        
        SILVERFISH = new Silverfish( this, family );
    }
    
    public static class Silverfish extends AbstractConfigCategory<SilverfishFamilyConfig> {
        
        public final DoubleField familyAggressiveChance;
        
        Silverfish( SilverfishFamilyConfig parent, MobFamily<?, ?> family ) {
            super( parent, ConfigUtil.noSpaces( family.configName ),
                    "Options specific to the family of " + family.configName + "." );
            
            familyAggressiveChance = SPEC.define( new DoubleField( "family_aggressive_chance", 0.05, DoubleField.Range.PERCENT,
                    "Chance for " + family.configName + " to spawn already calling for reinforcements.",
                    "By default, this applies to all " + family.configName + "; but species configs can override it." ) );
        }
    }
}