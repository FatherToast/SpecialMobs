package fathertoast.specialmobs.common.config.family;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.field.BooleanField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.util.ConfigUtil;

/**
 * This is the family config for witches.
 */
public class WitchFamilyConfig extends FamilyConfig {
    
    public final Witches WITCHES;
    
    /** Builds the config spec that should be used for this config. */
    public WitchFamilyConfig( MobFamily<?, ?> family ) {
        super( family );
        
        WITCHES = new Witches( SPEC, family );
    }
    
    public static class Witches extends Config.AbstractCategory {
        
        public final BooleanField useSplashSwiftness;
        
        Witches( ToastConfigSpec parent, MobFamily<?, ?> family ) {
            super( parent, ConfigUtil.noSpaces( family.configName ),
                    "Options specific to the family of " + family.configName + "." );
            
            useSplashSwiftness = SPEC.define( new BooleanField( "use_splash_swiftness", true,
                    "If true, " + family.configName + " will use splash potions of swiftness when trying to keep up,",
                    "rather than drinking a swiftness potion. Helps them keep up just a little better than vanilla." ) );
        }
    }
}