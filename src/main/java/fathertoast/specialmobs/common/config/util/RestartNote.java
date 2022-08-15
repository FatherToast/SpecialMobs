package fathertoast.specialmobs.common.config.util;

import javax.annotation.Nullable;
import java.util.List;

public enum RestartNote {
    NONE( " * Does NOT require a world/game restart to take effect *" ),
    
    WORLD( " * Requires a WORLD restart to take effect *" ),
    WORLD_PARTIAL( " * Requires a WORLD restart to take full effect *" ),
    
    GAME( " * Requires a GAME restart to take effect *" ),
    GAME_PARTIAL( " * Requires a GAME restart to take full effect *" );
    
    /** Adds the note's comment, if any. */
    public static void appendComment( List<String> comment, @Nullable RestartNote note ) {
        if( note != null ) comment.add( note.COMMENT );
    }
    
    private final String COMMENT;
    
    RestartNote( String comment ) { COMMENT = comment; }
}