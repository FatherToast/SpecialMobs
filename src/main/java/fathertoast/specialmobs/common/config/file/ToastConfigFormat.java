package fathertoast.specialmobs.common.config.file;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;

import java.util.Map;
import java.util.function.Supplier;

/**
 * The config file format used by the configs in this mod. Specifies the writer and parser for config files.
 */
public class ToastConfigFormat implements ConfigFormat<CommentedConfig> {
    /** The file extension used by config files. */
    public static final String FILE_EXT = ".toml";
    
    /** The config spec that drives this format. */
    private final ToastConfigSpec CONFIG_SPEC;
    
    ToastConfigFormat( ToastConfigSpec spec ) { CONFIG_SPEC = spec; }
    
    /** @return A writer of this config format. */
    @Override
    public ConfigWriter createWriter() { return new ToastTomlWriter( CONFIG_SPEC ); }
    
    /** @return A parser of this config format. */
    @Override
    public ConfigParser<CommentedConfig> createParser() { return new ToastTomlParser( CONFIG_SPEC ); }
    
    /**
     * Creates a config that uses the given map supplier for all its levels (top level and sub configs).
     *
     * @param mapCreator The map supplier for the config.
     * @return A config of this format with the given map creator.
     */
    @Override
    public CommentedConfig createConfig( Supplier<Map<String, Object>> mapCreator ) {
        return CommentedConfig.of( mapCreator, this );
    }
    
    /** @return Returns true if this format supports commented configs. */
    @Override
    public boolean supportsComments() { return true; }
}