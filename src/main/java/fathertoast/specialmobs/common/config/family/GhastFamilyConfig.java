package fathertoast.specialmobs.common.config.family;

import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.BooleanField;
import fathertoast.specialmobs.common.bestiary.MobFamily;

/**
 * This is the family config for ghasts.
 */
public class GhastFamilyConfig extends FamilyConfig {
    
    public final Ghasts GHASTS;
    
    /** Builds the config spec that should be used for this config. */
    public GhastFamilyConfig( ConfigManager manager, MobFamily<?, ?> family ) {
        super( manager, family );
        
        GHASTS = new Ghasts( this, family );
    }
    
    public static class Ghasts extends AbstractConfigCategory<GhastFamilyConfig> {
        
        public final BooleanField allowVerticalTargeting;
        
        Ghasts( GhastFamilyConfig parent, MobFamily<?, ?> family ) {
            super( parent, ConfigUtil.noSpaces( family.configName ),
                    "Options specific to the family of " + family.configName + "." );
            
            allowVerticalTargeting = SPEC.define( new BooleanField( "allow_vertical_targeting", true,
                    "If true, " + family.configName + " will be allowed to target any visible player in their follow range. " +
                            "As of MC 1.8, vanilla ghasts can only start targeting players nearly completely horizontal from them." ) );
        }
    }
}