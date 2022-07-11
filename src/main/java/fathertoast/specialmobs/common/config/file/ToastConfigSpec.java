package fathertoast.specialmobs.common.config.file;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.file.FileConfigBuilder;
import com.electronwill.nightconfig.core.file.FileWatcher;
import com.electronwill.nightconfig.core.io.CharacterOutput;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.WritingException;
import fathertoast.specialmobs.common.config.field.*;
import fathertoast.specialmobs.common.config.util.ConfigUtil;
import fathertoast.specialmobs.common.core.SpecialMobs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A config spec maps read and write functions to the runtime variables used to hold them.
 * <p>
 * Contains helper functions to build a spec similarly to writing a default file, allowing insertion of
 * comments and formatting as desired.
 */
@SuppressWarnings( "unused" )
public class ToastConfigSpec {
    /** It's a good idea to freeze the file watcher while loading a large number of files; can prevent a few unneeded reloads. */
    public static volatile boolean freezeFileWatcher;
    
    /** The directory containing this config's file. */
    public final File DIR;
    /** The name of this config. The file name is this plus the file extension. */
    public final String NAME;
    
    /** The underlying config object. */
    public final FileConfig CONFIG_FILE;
    
    /** The list of actions to perform, in a specific order, when reading or writing the config file. */
    private final List<Action> ACTIONS = new ArrayList<>();
    
    /** True while this config spec is currently writing. */
    volatile boolean writing;
    
    /** Creates a new config spec at a specified location with only the basic 'start of file' action. */
    public ToastConfigSpec( File dir, String fileName ) {
        DIR = dir;
        NAME = fileName;
        
        // Make sure the directory exists
        if( !dir.exists() && !dir.mkdirs() ) {
            SpecialMobs.LOG.error( "Failed to make config folder! Things will likely explode. " +
                    "Create the folder manually to avoid this problem in the future: {}", dir );
        }
        
        // Create the config file format
        final FileConfigBuilder builder = FileConfig.builder( new File( dir, fileName + ToastConfigFormat.FILE_EXT ),
                new ToastConfigFormat( this ) );
        CONFIG_FILE = builder.sync().build();
        
        // Make sure the file exists (an empty file is all we need)
        if( !CONFIG_FILE.getFile().exists() ) {
            SpecialMobs.LOG.info( "Generating default config file {}", ConfigUtil.toRelativePath( CONFIG_FILE ) );
            try {
                if( !CONFIG_FILE.getFile().createNewFile() ) {
                    SpecialMobs.LOG.error( "Failed to make config file! Things will likely explode. " +
                            "Create the file manually to avoid this problem in the future: {}", dir );
                }
            }
            catch( IOException ex ) {
                SpecialMobs.LOG.error( "Caught exception while generating blank config file! Things will likely explode. " +
                        "Create the file manually to avoid this problem in the future: {}", dir, ex );
            }
        }
    }
    
    /** Loads the config from disk. */
    public void initialize() {
        SpecialMobs.LOG.info( "First-time loading config file {}", ConfigUtil.toRelativePath( CONFIG_FILE ) );
        try {
            CONFIG_FILE.load();
        }
        catch( ParsingException ex ) {
            SpecialMobs.LOG.error( "Failed first-time loading of config file {} - this is bad!",
                    ConfigUtil.toRelativePath( CONFIG_FILE ) );
        }
        
        try {
            FileWatcher.defaultInstance().addWatch( CONFIG_FILE.getFile(), this::onFileChanged );
            SpecialMobs.LOG.info( "Started watching config file {} for updates", ConfigUtil.toRelativePath( CONFIG_FILE ) );
        }
        catch( IOException ex ) {
            SpecialMobs.LOG.error( "Failed to watch config file {} - this file will NOT update in-game until restarted!",
                    ConfigUtil.toRelativePath( CONFIG_FILE ) );
        }
    }
    
    /** Called when a change to the config file is detected. */
    public void onFileChanged() {
        if( writing ) {
            SpecialMobs.LOG.debug( "Skipping config file reload (it is currently saving) {}", ConfigUtil.toRelativePath( CONFIG_FILE ) );
        }
        else if( freezeFileWatcher ) {
            SpecialMobs.LOG.debug( "Skipping config file reload (file watcher paused) {}", ConfigUtil.toRelativePath( CONFIG_FILE ) );
        }
        else {
            SpecialMobs.LOG.info( "Reloading config file {}", ConfigUtil.toRelativePath( CONFIG_FILE ) );
            try {
                CONFIG_FILE.load();
            }
            catch( ParsingException ex ) {
                SpecialMobs.LOG.error( "Failed to reload config file {}", ConfigUtil.toRelativePath( CONFIG_FILE ) );
            }
        }
    }
    
    /** Called after the config is loaded to update cached values. */
    public void onLoad() {
        // Perform load actions
        boolean rewrite = false;
        for( Action action : ACTIONS ) {
            if( action.onLoad() ) rewrite = true;
        }
        // Only rewrite if one of the load actions requests it
        if( rewrite ) save();
    }
    
    /** Saves this config to file. */
    private void save() {
        try {
            CONFIG_FILE.save();
        }
        catch( WritingException ex ) {
            SpecialMobs.LOG.error( "Failed to save config file {}", ConfigUtil.toRelativePath( CONFIG_FILE ) );
        }
    }
    
    /** Writes the current state of the config to file. */
    public void write( ToastTomlWriter writer, CharacterOutput output ) {
        for( Action action : ACTIONS ) { action.write( writer, output ); }
    }
    
    /** Represents a single action performed by the spec when reading or writing the config file. */
    private interface Action {
        /** Called when the config is loaded. */
        boolean onLoad();
        
        /** Called when the config is saved. */
        void write( ToastTomlWriter writer, CharacterOutput output );
    }
    
    /** Represents a write-only spec action. */
    private static abstract class Format implements Action {
        /** Called when the config is loaded. */
        @Override
        public final boolean onLoad() { return false; } // Formatting actions do not affect file reading
        
        /** Called when the config is saved. */
        @Override
        public abstract void write( ToastTomlWriter writer, CharacterOutput output );
    }
    
    /** Represents a variable number of new lines. */
    private static class NewLines extends Format {
        /** The number of new lines to write. */
        private final int COUNT;
        
        /** Create a new comment action that will insert a number of new lines. */
        private NewLines( int count ) { COUNT = count; }
        
        /** Called when the config is saved. */
        @Override
        public void write( ToastTomlWriter writer, CharacterOutput output ) {
            for( int i = 0; i < COUNT; i++ ) {
                writer.writeNewLine( output );
            }
        }
    }
    
    /** Represents a variable number of indent increases or decreases. */
    private static class Indent extends Format {
        /** The amount to change the indent by. */
        private final int AMOUNT;
        
        /** Create a new comment action that will insert a number of new lines. */
        private Indent( int amount ) { AMOUNT = amount; }
        
        /** Called when the config is saved. */
        @Override
        public void write( ToastTomlWriter writer, CharacterOutput output ) {
            writer.changeIndentLevel( AMOUNT );
        }
    }
    
    /** Represents a comment. */
    private static class Comment extends Format {
        /** The spec this action belongs to. */
        private final List<String> COMMENT;
        
        /** Create a new comment action that will insert a comment. */
        private Comment( List<String> comment ) { COMMENT = comment; }
        
        /** Called when the config is saved. */
        @Override
        public void write( ToastTomlWriter writer, CharacterOutput output ) { writer.writeComment( COMMENT, output ); }
    }
    
    /** Represents a file header comment. */
    private static class Header extends Format {
        /** The spec this action belongs to. */
        private final ToastConfigSpec PARENT;
        /** The file comment. */
        private final List<String> COMMENT;
        
        /** Create a new header action that will insert the opening file comment. */
        private Header( ToastConfigSpec parent, List<String> comment ) {
            PARENT = parent;
            COMMENT = comment;
        }
        
        /** Called when the config is saved. */
        @Override
        public void write( ToastTomlWriter writer, CharacterOutput output ) {
            writer.writeComment( SpecialMobs.MOD_ID + ":" + PARENT.NAME + ToastConfigFormat.FILE_EXT, output );
            writer.writeComment( COMMENT, output );
            
            writer.increaseIndentLevel();
        }
    }
    
    /** Represents a category comment. */
    private static class Category extends Format {
        /** The category comment. */
        private final List<String> COMMENT;
        
        /** Create a new category action that will insert the category comment. */
        private Category( String categoryName, List<String> comment ) {
            comment.add( 0, "Category: " + categoryName );
            COMMENT = comment;
        }
        
        /** Called when the config is saved. */
        @Override
        public void write( ToastTomlWriter writer, CharacterOutput output ) {
            writer.decreaseIndentLevel();
            
            writer.writeNewLine( output );
            writer.writeNewLine( output );
            writer.writeComment( COMMENT, output );
            writer.writeNewLine( output );
            
            writer.increaseIndentLevel();
        }
    }
    
    /** Represents a read-only spec action. */
    private static class ReadCallback implements Action {
        /** The method to call on read. */
        private final Runnable CALLBACK;
        
        /** Create a new field action that will load/create and save the field value. */
        private ReadCallback( Runnable callback ) { CALLBACK = callback; }
        
        /** Called when the config is loaded. */
        @Override
        public boolean onLoad() {
            CALLBACK.run();
            return false;
        }
        
        /** Called when the config is saved. */
        @Override
        public final void write( ToastTomlWriter writer, CharacterOutput output ) { } // Read callback actions do not affect file writing
    }
    
    /** Represents a spec action that reads and writes to a field. */
    private static class Field implements Action {
        /** The spec this action belongs to. */
        private final ToastConfigSpec PARENT;
        /** The underlying config field to perform actions for. */
        private final AbstractConfigField FIELD;
        
        /** Create a new field action that will load/create and save the field value. */
        private Field( ToastConfigSpec parent, AbstractConfigField field ) {
            PARENT = parent;
            FIELD = field;
            FIELD.finalizeComment();
        }
        
        /** Called when the config is loaded. */
        @Override
        public boolean onLoad() {
            // Get cached value to detect changes
            final Object oldRaw = FIELD.getRaw();
            
            // Fetch the newly loaded value
            final Object rawToml = PARENT.CONFIG_FILE.getOptional( FIELD.getKey() ).orElse( null );
            FIELD.load( rawToml );
            
            // Push the field's value back to the config if its value was changed
            final Object newRaw = FIELD.getRaw();
            if( rawToml == null || !Objects.equals( oldRaw, newRaw ) ) {
                PARENT.CONFIG_FILE.set( FIELD.getKey(), newRaw );
                return true;
            }
            return false;
        }
        
        /** Called when the config is saved. */
        @Override
        public void write( ToastTomlWriter writer, CharacterOutput output ) {
            // Write the key and value
            writer.writeField( FIELD, output );
        }
    }
    
    /**
     * @param field The field to define in this config spec.
     * @return The same field for convenience in constructing.
     */
    public <T extends AbstractConfigField> T define( T field ) {
        // Double check just to make sure we don't screw up the spec
        for( Action action : ACTIONS ) {
            if( action instanceof Field && field.getKey().equalsIgnoreCase( ((Field) action).FIELD.getKey() ) ) {
                throw new IllegalStateException( "Attempted to register duplicate field key '" + field.getKey() + "' in config " + NAME );
            }
        }
        ACTIONS.add( new Field( this, field ) );
        return field;
    }
    
    /** @param callback The callback to run on read. */
    public void callback( Runnable callback ) { ACTIONS.add( new ReadCallback( callback ) ); }
    
    /** Inserts a single new line. */
    public void newLine() { newLine( 1 ); }
    
    /** @param count The number of new lines to insert. */
    public void newLine( int count ) { ACTIONS.add( new NewLines( count ) ); }
    
    /** Increases the indent by one level. */
    public void increaseIndent() { indent( +1 ); }
    
    /** Decreases the indent by one level. */
    public void decreaseIndent() { indent( -1 ); }
    
    /** @param count The amount to change the indent by. */
    public void indent( int count ) { ACTIONS.add( new Indent( count ) ); }
    
    /** @param comment The comment to insert. */
    public void comment( String... comment ) { comment( TomlHelper.newComment( comment ) ); }
    
    /** @param comment The comment to insert. */
    public void comment( List<String> comment ) { ACTIONS.add( new Comment( comment ) ); }
    
    /** @param comment The file comment to insert. */
    public void header( List<String> comment ) { ACTIONS.add( new Header( this, comment ) ); }
    
    /** Inserts a detailed description of how to use the registry entry list field. */
    public void describeRegistryEntryList() { ACTIONS.add( new Comment( RegistryEntryListField.verboseDescription() ) ); }
    
    /** Inserts a detailed description of how to use the entity list field. */
    public void describeEntityList() { ACTIONS.add( new Comment( EntityListField.verboseDescription() ) ); }
    
    /** Inserts a detailed description of how to use the attribute list field. */
    public void describeAttributeList() { ACTIONS.add( new Comment( AttributeListField.verboseDescription() ) ); }
    
    /** Inserts a detailed description of how to use the block list field. */
    public void describeBlockList() { ACTIONS.add( new Comment( BlockListField.verboseDescription() ) ); }
    
    /**
     * @param name    The category name.
     * @param comment The category comment to insert.
     */
    public void category( String name, List<String> comment ) { ACTIONS.add( new Category( name, comment ) ); }
}