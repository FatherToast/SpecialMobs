package fathertoast.specialmobs.common.config.family;

import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.BooleanField;
import fathertoast.specialmobs.common.bestiary.MobFamily;

/**
 * This is the family config for witches.
 */
public class WitchFamilyConfig extends FamilyConfig {
    
    public final Witches WITCHES;
    
    /** Builds the config spec that should be used for this config. */
    public WitchFamilyConfig( ConfigManager manager, MobFamily<?, ?> family ) {
        super( manager, family );
        
        WITCHES = new Witches( this, family );
    }
    
    public static class Witches extends AbstractConfigCategory<WitchFamilyConfig> {
        
        public final BooleanField useSplashSwiftness;
        
        Witches( WitchFamilyConfig parent, MobFamily<?, ?> family ) {
            super( parent, ConfigUtil.noSpaces( family.configName ),
                    "Options specific to the family of " + family.configName + "." );
            
            useSplashSwiftness = SPEC.define( new BooleanField( "use_splash_swiftness", true,
                    "If true, " + family.configName + " will use splash potions of swiftness when trying to keep up,",
                    "rather than drinking a swiftness potion. Helps them keep up just a little better than vanilla." ) );
        }
    }
}