package fathertoast.specialmobs.common.config.file;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.*;
import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.core.SpecialMobs;

import java.io.Writer;
import java.util.Iterator;
import java.util.List;

/**
 * A simple toml writer implementation that allows the config spec to entirely define how to write.
 */
public class ToastTomlWriter implements ConfigWriter {
    /** The chars to write to create a single indent level. */
    private static final char[] CHARS_INDENT = IndentStyle.TABS.chars;
    /** The chars to write to start a new line. */
    private static final char[] CHARS_NEW_LINE = NewlineStyle.system().chars;
    
    /** The chars to write to start each comment line. */
    private static final char[] CHARS_COMMENT = { '#', ' ' };
    /** The chars to write after a key and before the value. */
    private static final char[] CHARS_ASSIGN = { ' ', '=', ' ' };
    
    /** The current indent level to write at. */
    private int currentIndentLevel;
    /** Skips the next indent step. Set true when a line is not completed, but needs to pass to other methods. */
    private boolean skipNextIndent;
    
    /** The config spec that drives this writer. */
    private final ToastConfigSpec CONFIG_SPEC;
    
    ToastTomlWriter( ToastConfigSpec spec ) { CONFIG_SPEC = spec; }
    
    /**
     * Writes a configuration.
     *
     * @param config The config to write.
     * @param writer The writer to write it to.
     * @throws WritingException If an error occurs.
     */
    @Override
    public void write( UnmodifiableConfig config, Writer writer ) {
        SpecialMobs.LOG.debug( "Writing config file! ({}{})", CONFIG_SPEC.NAME, ToastConfigFormat.FILE_EXT );
        CharacterOutput output = new WriterOutput( writer );
        currentIndentLevel = 0;
        CONFIG_SPEC.write( this, output );
    }
    
    /** Increases the indent level by 1. */
    public void increaseIndentLevel() { changeIndentLevel( +1 ); }
    
    /** Decreases the indent level by 1. */
    public void decreaseIndentLevel() { changeIndentLevel( -1 ); }
    
    /** Changes the indent level by a specified amount. */
    public void changeIndentLevel( int amount ) { currentIndentLevel += amount; }
    
    /** Writes the indent based on the current indent level. */
    private void writeIndent( CharacterOutput output ) {
        if( skipNextIndent ) {
            skipNextIndent = false;
        }
        else {
            for( int i = 0; i < currentIndentLevel; i++ ) {
                output.write( CHARS_INDENT );
            }
        }
    }
    
    /** Tells the writer to continue the current line, skipping the next indent. */
    public void continueLine() { skipNextIndent = true; }
    
    /** Writes the system-preferred new line character(s) to start a new line. */
    public void writeNewLine( CharacterOutput output ) {
        output.write( CHARS_NEW_LINE );
    }
    
    /** Writes a single-line string. */
    public void writeLine( String line, CharacterOutput output ) {
        writeIndent( output );
        output.write( line );
        writeNewLine( output );
    }
    
    /** Writes a literal array of single-line strings. */
    public void writeStringArray( List<String> list, CharacterOutput output ) {
        if( list == null || list.isEmpty() ) {
            writeLine( "[]", output );
            return;
        }
        
        writeLine( "[", output );
        increaseIndentLevel();
        
        Iterator<String> itr = list.listIterator();
        while( itr.hasNext() ) {
            writeIndent( output );
            output.write( String.format( "\"%s\"", itr.next() ) );
            if( itr.hasNext() ) {
                output.write( ',' );
            }
            writeNewLine( output );
        }
        
        decreaseIndentLevel();
        writeLine( "]", output );
    }
    
    /** Writes a list of single-line comments. */
    public void writeComment( List<String> comment, CharacterOutput output ) {
        for( String line : comment ) {
            writeComment( line, output );
        }
    }
    
    /** Writes a single-line comment. */
    public void writeComment( String comment, CharacterOutput output ) {
        writeIndent( output );
        output.write( CHARS_COMMENT );
        output.write( comment );
        writeNewLine( output );
    }
    
    /** Writes a literal list of single-line strings. Assumes the indent has already been written on the first line. */
    public void writeField( AbstractConfigField field, CharacterOutput output ) {
        // Write the comment, if any
        if( field.getComment() != null ) {
            writeComment( field.getComment(), output );
        }
        
        // Write the key
        writeIndent( output );
        output.write( field.getKey() );
        output.write( CHARS_ASSIGN );
        continueLine();
        
        // Write the value
        field.writeValue( this, output );
    }
}