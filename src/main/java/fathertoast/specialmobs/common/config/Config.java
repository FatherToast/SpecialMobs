package fathertoast.specialmobs.common.config;

import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.specialmobs.common.bestiary.MobFamily;

/**
 * Used as the sole hub for all config access from outside the config package.
 * <p>
 * Contains references to all config files used in this mod, which in turn provide direct 'getter' access to each
 * configurable value.
 */
public class Config {
    
    public static final ConfigManager MANAGER = ConfigManager.create( "SpecialMobs" );
    
    public static final MainConfig MAIN = new MainConfig( MANAGER, "main" );
    
    /** Performs initial loading of all configs in this mod. */
    public static void initialize() {
        ReadMeConfig.makeReadMe( MANAGER );
        MAIN.SPEC.initialize();
        MobFamily.initBestiary(); // Just make sure this class gets loaded
    }
}