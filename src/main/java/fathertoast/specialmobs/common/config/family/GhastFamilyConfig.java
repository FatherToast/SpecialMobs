package fathertoast.specialmobs.common.config.family;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.field.BooleanField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.util.ConfigUtil;

/**
 * This is the family config for ghasts.
 */
public class GhastFamilyConfig extends FamilyConfig {
    
    public final Ghasts GHASTS;
    
    /** Builds the config spec that should be used for this config. */
    public GhastFamilyConfig( MobFamily<?, ?> family ) {
        super( family );
        
        GHASTS = new Ghasts( SPEC, family );
    }
    
    public static class Ghasts extends Config.AbstractCategory {
        
        public final BooleanField allowVerticalTargeting;
        
        Ghasts( ToastConfigSpec parent, MobFamily<?, ?> family ) {
            super( parent, ConfigUtil.noSpaces( family.configName ),
                    "Options specific to the family of " + family.configName + "." );
            
            allowVerticalTargeting = SPEC.define( new BooleanField( "allow_vertical_targeting", true,
                    "If true, " + family.configName + " will be allowed to target any visible player in their follow range.",
                    "As of MC 1.8, vanilla ghasts can only start targeting players nearly completely horizontal from them." ) );
        }
    }
}