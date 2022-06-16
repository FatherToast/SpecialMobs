package fathertoast.specialmobs.common.config.file;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.toml.TomlParser;
import fathertoast.specialmobs.common.core.SpecialMobs;

import java.io.Reader;

/**
 * A simple toml writer implementation that allows the config spec to entirely define how to write.
 */
public class ToastTomlParser implements ConfigParser<CommentedConfig> {
    /** The actual parser. */
    private final TomlParser WRAPPED_PARSER = new TomlParser();
    
    /** The config spec that drives this writer. */
    private final ToastConfigSpec CONFIG_SPEC;
    
    ToastTomlParser( ToastConfigSpec spec ) { CONFIG_SPEC = spec; }
    
    /** @return The format supported by this parser. */
    @Override
    public ConfigFormat<CommentedConfig> getFormat() { return WRAPPED_PARSER.getFormat(); }
    
    /**
     * Parses a configuration.
     *
     * @param reader The reader to parse
     * @return A new Config
     * @throws ParsingException If an error occurs.
     */
    @Override
    public CommentedConfig parse( Reader reader ) {
        SpecialMobs.LOG.error( "Attempting to parse NEW config file! ({})", CONFIG_SPEC.CONFIG_FILE.getFile() );
        throw new ParsingException( "Attempted to generate new config! This is not supported." );
    }
    
    /**
     * Parses a configuration.
     *
     * @param reader      The reader to parse.
     * @param destination The config where to put the data.
     * @param parsingMode The set parsing mode.
     */
    @Override
    public void parse( Reader reader, Config destination, ParsingMode parsingMode ) {
        SpecialMobs.LOG.debug( "Parsing config file! ({}{})", CONFIG_SPEC.NAME, ToastConfigFormat.FILE_EXT );
        WRAPPED_PARSER.parse( reader, destination, parsingMode );
        CONFIG_SPEC.onLoad();
    }
}