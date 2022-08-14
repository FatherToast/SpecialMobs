package fathertoast.specialmobs.common.config.util;

import javax.annotation.Nullable;
import java.util.List;

public enum RestartNote {
    NONE( " * Does NOT require a world/game restart to take effect *" ),
    WORLD( " * Requires a world restart to take effect *" ),
    GAME( " * Requires a game restart to take effect *" );
    
    /** Adds the note's comment, if any. */
    public static void appendComment( List<String> comment, @Nullable RestartNote note ) {
        if( note != null ) comment.add( note.COMMENT );
    }
    
    private final String COMMENT;
    
    RestartNote( String comment ) { COMMENT = comment; }
}