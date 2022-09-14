package fathertoast.specialmobs.common.config;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.file.TomlHelper;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;

/**
 * Used as the sole hub for all config access from outside the config package.
 * <p>
 * Contains references to all config files used in this mod, which in turn provide direct 'getter' access to each
 * configurable value.
 */
public class Config {
    /** The root folder for config files in this mod. */
    public static final File CONFIG_DIR = new File( FMLPaths.CONFIGDIR.get().toFile(), "FatherToast/" + SpecialMobs.MOD_ID + "/" );
    
    public static final MainConfig MAIN = new MainConfig( CONFIG_DIR, "_main" );
    
    /** Performs initial loading of all configs in this mod. */
    public static void initialize() {
        ToastConfigSpec.freezeFileWatcher = true;
        ReadMeConfig.makeReadMe( CONFIG_DIR );
        MAIN.SPEC.initialize();
        MobFamily.initBestiary();
        ToastConfigSpec.freezeFileWatcher = false;
        
        // We don't really need to do this, but it's nice to remove the reference once we're done making config specs
        AbstractConfigField.loadingCategory = null;
    }
    
    /**
     * Represents one config file that contains a reference for each configurable value within and a specification
     * that defines the file's format.
     */
    public static abstract class AbstractConfig {
        /** The spec used by this config that defines the file's format. */
        public final ToastConfigSpec SPEC;
        
        public AbstractConfig( File dir, String fileName, String... fileDescription ) {
            AbstractConfigField.loadingCategory = "";
            SPEC = new ToastConfigSpec( dir, fileName );
            SPEC.header( TomlHelper.newComment( fileDescription ) );
        }
    }
    
    /**
     * Represents one config file that contains a reference for each configurable value within and a specification
     * that defines the file's format.
     */
    public static abstract class AbstractCategory {
        /** The spec used by this config that defines the file's format. */
        protected final ToastConfigSpec SPEC;
        
        public AbstractCategory( ToastConfigSpec parent, String name, String... categoryDescription ) {
            AbstractConfigField.loadingCategory = name + ".";
            SPEC = parent;
            SPEC.category( name, TomlHelper.newComment( categoryDescription ) );
        }
    }
}