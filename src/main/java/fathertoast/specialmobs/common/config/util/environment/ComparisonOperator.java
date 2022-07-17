package fathertoast.specialmobs.common.config.util.environment;

import javax.annotation.Nullable;

public enum ComparisonOperator {
    NOT_EQUAL_TO( "!=" ), LESS_OR_EQUAL( "<=" ), GREATER_OR_EQUAL( ">=" ),
    EQUAL_TO( "=" ), LESS_THAN( "<" ), GREATER_THAN( ">" );
    
    private final String LITERAL;
    
    ComparisonOperator( String str ) { LITERAL = str; }
    
    @Override
    public String toString() { return LITERAL; }
    
    public boolean apply( float first, float second ) {
        switch( this ) {
            case LESS_THAN:
                return first < second;
            case LESS_OR_EQUAL:
                return first <= second;
            case GREATER_THAN:
                return first > second;
            case GREATER_OR_EQUAL:
                return first >= second;
            case EQUAL_TO:
                return first == second;
            case NOT_EQUAL_TO:
                return first != second;
        }
        throw new IllegalStateException( "Float comparison implementation is invalid! :(" );
    }
    
    public boolean apply( int first, int second ) {
        switch( this ) {
            case LESS_THAN:
                return first < second;
            case LESS_OR_EQUAL:
                return first <= second;
            case GREATER_THAN:
                return first > second;
            case GREATER_OR_EQUAL:
                return first >= second;
            case EQUAL_TO:
                return first == second;
            case NOT_EQUAL_TO:
                return first != second;
        }
        throw new IllegalStateException( "Integer comparison implementation is invalid! :(" );
    }
    
    public boolean apply( long first, long second ) {
        switch( this ) {
            case LESS_THAN:
                return first < second;
            case LESS_OR_EQUAL:
                return first <= second;
            case GREATER_THAN:
                return first > second;
            case GREATER_OR_EQUAL:
                return first >= second;
            case EQUAL_TO:
                return first == second;
            case NOT_EQUAL_TO:
                return first != second;
        }
        throw new IllegalStateException( "Long comparison implementation is invalid! :(" );
    }
    
    /** @return The operator described by a given string, or null if invalid. */
    @Nullable
    public static ComparisonOperator parse( String op ) {
        for( ComparisonOperator operator : values() ) {
            if( op.startsWith( operator.LITERAL ) ) return operator;
        }
        return null;
    }
}