package fathertoast.specialmobs.common.config.util;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.file.TomlHelper;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

/**
 * One block state entry in a block list.
 */
@SuppressWarnings( "unused" )
public class BlockEntry implements Cloneable {
    /** The block to match. */
    public final Block BLOCK;
    
    /** The states of the block to match. If this is empty, any state will match. */
    private final List<State> MATCHERS = new ArrayList<>();
    
    /** Creates a target block that matches any state. */
    public BlockEntry( Block block ) { BLOCK = block; }
    
    /** Creates a target block that matches one specific state. */
    public BlockEntry( BlockState block ) {
        this( block.getBlock() );
        
        // Add all the properties present in the block state
        StateBuilder state = new StateBuilder( BLOCK );
        for( Property<? extends Comparable<?>> property : block.getProperties() ) {
            state.add( property, block.getValue( property ) );
        }
        if( !state.isEmpty() ) {
            MATCHERS.add( state.toTargetState() );
        }
    }
    
    /** Creates a target block from a string that specifies a single target. */
    public BlockEntry( AbstractConfigField field, String line ) {
        // Parse the base block
        final String[] pair = line.split( "\\[", 2 );
        final Block block = ForgeRegistries.BLOCKS.getValue( new ResourceLocation( pair[0] ) );
        BLOCK = block == null ? Blocks.AIR : block;
        
        // We are done constructing if the entry is invalid or does not specify block states
        if( BLOCK == Blocks.AIR || pair.length < 2 ) {
            return;
        }
        
        // Try to parse block states
        if( !pair[1].endsWith( "]" ) ) {
            // For now, this is okay; if we ever allow values to be added to block lists, this should fail
            SpecialMobs.LOG.info( "Adding closing bracket on block state properties for {} \"{}\". Invalid entry: {}",
                    field.getClass(), field.getKey(), line );
            pair[1] = pair[1] + "]";
        }
        State state = BlockEntry.parseState( field, pair[1].substring( 0, pair[1].length() - 1 ), BLOCK );
        if( !state.PROPERTIES_TO_MATCH.isEmpty() ) {
            MATCHERS.add( state );
        }
    }
    
    /** @return A clone of this entry, backed by a new state list. */
    @SuppressWarnings( "MethodDoesntCallSuperMethod" )
    @Override
    public BlockEntry clone() {
        BlockEntry cloned = new BlockEntry( BLOCK );
        cloned.MATCHERS.addAll( MATCHERS );
        return cloned;
    }
    
    /**
     * @return The string representation of this block list entry, as it would appear in a config file.
     * <p>
     * Format is "registry_key[properties]", the properties are optional.
     */
    @Override
    public String toString() {
        // Block name
        String registryName = SpecialMobs.toString( BLOCK );
        if( MATCHERS.isEmpty() ) {
            return registryName;
        }
        // Append block states
        StringBuilder str = new StringBuilder();
        for( State state : MATCHERS ) {
            str.append( registryName ).append( "[" ).append( state.toString() ).append( "]," );
        }
        return str.substring( 0, str.length() - 1 );
    }
    
    /** Used to sort this object in a hash table. */
    @Override
    public int hashCode() {
        ResourceLocation regKey = BLOCK.getRegistryName();
        return regKey == null ? 0 : regKey.hashCode();
    }
    
    /** @return Returns true if the block state matches the description in this entry. */
    public boolean matches( BlockState block ) {
        if( MATCHERS.isEmpty() ) {
            return true;
        }
        
        for( State state : MATCHERS ) {
            if( state.matches( block ) ) {
                return true;
            }
        }
        return false;
    }
    
    /** @param other Merges all matching from another block entry with this entry's matchers. */
    void mergeFrom( BlockEntry other ) {
        if( MATCHERS.isEmpty() ) {
            SpecialMobs.LOG.warn( "Ignoring attempt to add redundant block state to config with block state wildcard '{}'", other );
        }
        else if( other.MATCHERS.isEmpty() ) {
            SpecialMobs.LOG.warn( "Adding block state wildcard to config with redundant block state(s) '{}'", this );
            MATCHERS.clear();
        }
        else {
            MATCHERS.addAll( other.MATCHERS );
        }
    }
    
    /** @return Builds and returns a new block state matcher parsed from the state string provided. */
    private static State parseState( AbstractConfigField field, String stateString, Block block ) {
        if( stateString.isEmpty() ) {
            return new State( Collections.emptyList() );
        }
        final StateContainer<Block, BlockState> stateContainer = block.getStateDefinition();
        
        // Parse the state and build the matcher
        final StateBuilder builder = new StateBuilder( block );
        final String[] properties = stateString.split( "," );
        for( String combinedEntry : properties ) {
            // Parse an individual property key-value pair
            String[] pair = combinedEntry.trim().split( "=", 2 );
            if( pair.length != 2 ) {
                SpecialMobs.LOG.warn( "Invalid block property for {} \"{}\". Format must be 'property=value'. " +
                        "Deleting property. Invalid property: {}", field.getClass(), field.getKey(), combinedEntry.trim() );
                continue;
            }
            else if( pair[1].equals( "*" ) ) {
                // This is to match all values, which is the same as not including the property at all
                continue;
            }
            // Parse the property key
            final Property<? extends Comparable<?>> property = stateContainer.getProperty( pair[0] );
            if( property == null ) {
                // Make a list of valid state keys to give better feedback
                List<Object> propertyNames = new ArrayList<>();
                for( Property<? extends Comparable<?>> allowed : stateContainer.getProperties() ) {
                    propertyNames.add( allowed.getName() );
                }
                SpecialMobs.LOG.warn( "Invalid block property key for {} \"{}\". Valid property keys for '{}' are {}. " +
                                "Deleting property. Invalid property: {}", field.getClass(), field.getKey(), SpecialMobs.toString( block ),
                        TomlHelper.literalList( propertyNames ), combinedEntry.trim() );
                continue;
            }
            // Parse the property value
            final Optional<? extends Comparable<?>> value = property.getValue( pair[1] );
            if( !value.isPresent() ) {
                // Make a list of valid property values to give better feedback
                List<Object> valueNames = new ArrayList<>();
                for( Comparable<?> allowed : property.getPossibleValues() ) {
                    valueNames.add( property.getName( value( allowed ) ) );
                }
                SpecialMobs.LOG.warn( "Invalid block property value for {} \"{}\". Valid values for property '{}' are {}. " +
                                "Deleting property. Invalid property: {}", field.getClass(), field.getKey(), property.getName(),
                        TomlHelper.literalList( valueNames ), combinedEntry.trim() );
                continue;
            }
            
            // Add the completed entry to the state
            builder.add( property, value.get() );
        }
        return builder.toTargetState();
    }
    
    /** A helper method to get the name of a property's value; gets it around the weird generic type issues. */
    private static <T extends Comparable<T>> T value( Comparable<?> allowed ) {
        //noinspection unchecked
        return (T) allowed;
    }
    
    /**
     * A block state matcher. Essentially, this is a block state where the properties are optional.
     * <p>
     * When matching/comparing to block states, properties that have not been defined here are ignored on the block state.
     */
    private static final class State {
        /** The block state properties to match. */
        private final List<Map.Entry<Property<?>, Comparable<?>>> PROPERTIES_TO_MATCH;
        
        /** Creates a new block state matcher with the given properties. */
        private State( List<Map.Entry<Property<?>, Comparable<?>>> properties ) {
            PROPERTIES_TO_MATCH = Collections.unmodifiableList( properties );
        }
        
        /** @return Returns true if the block state matches the description in this state. */
        boolean matches( BlockState block ) {
            for( Map.Entry<Property<?>, Comparable<?>> entry : PROPERTIES_TO_MATCH ) {
                if( notEqual( block.getValue( entry.getKey() ), entry.getValue() ) ) {
                    return false;
                }
            }
            return true;
        }
        
        /** @return Returns the block state with the properties from this matcher merged in. */
        public BlockState asMatching( BlockState block ) {
            if( PROPERTIES_TO_MATCH.isEmpty() ) {
                // All states match the empty set
                return block;
            }
            for( Map.Entry<Property<?>, Comparable<?>> entry : PROPERTIES_TO_MATCH ) {
                block = withProperty( block, entry.getKey(), entry.getValue() );
            }
            return block;
        }
        
        /** @return Returns a string representation of this matcher. Uses the same format as a block state. */
        @Override
        public String toString() {
            if( PROPERTIES_TO_MATCH.isEmpty() ) {
                return "";
            }
            StringBuilder str = new StringBuilder();
            for( Map.Entry<Property<?>, Comparable<?>> entry : PROPERTIES_TO_MATCH ) {
                str.append( entry.getKey().getName() ).append( "=" ).append( getPropertyName( entry.getKey(), entry.getValue() ) ).append( "," );
            }
            return str.substring( 0, str.length() - 1 );
        }
        
        /** @return Returns the block state with a single property merged in. */
        private <T extends Comparable<T>> BlockState withProperty( BlockState block, Property<T> property, Comparable<?> value ) {
            //noinspection unchecked
            return block.setValue( property, (T) value );
        }
        
        /** @return Returns true if the two values do not match. */
        private <T extends Comparable<T>> boolean notEqual( T stateValue, Comparable<?> targetValue ) { return !stateValue.equals( targetValue ); }
        
        /** @return Returns the name of a property value. */
        private <T extends Comparable<T>> String getPropertyName( Property<T> property, Comparable<?> value ) {
            //noinspection unchecked
            return property.getName( (T) value );
        }
    }
    
    public static final class StateBuilder {
        /** The block in this state. Only used for building default config values. */
        private final Block BLOCK;
        
        /** The underlying properties map. */
        private final Map<Property<? extends Comparable<?>>, Comparable<?>> propertiesToMatch = new HashMap<>();
        
        /** Can be used for building default configs to make block entries with target states. */
        public StateBuilder( Block block ) { BLOCK = block; }
        
        /** @return Returns true if this state builder has no properties set. */
        @SuppressWarnings( "BooleanMethodIsAlwaysInverted" )
        public boolean isEmpty() { return propertiesToMatch.isEmpty(); }
        
        /** @return Returns true if the property has already been set in this builder. */
        public boolean contains( Property<? extends Comparable<?>> property ) { return propertiesToMatch.containsKey( property ); }
        
        /** Adds a property and its value to this builder. */
        public StateBuilder add( Property<? extends Comparable<?>> property, Comparable<?> value ) {
            propertiesToMatch.put( property, value );
            return this;
        }
        
        /** @return Returns a block entry reflecting the current state of this builder. */
        public BlockEntry toBlockEntry() {
            final BlockEntry target = new BlockEntry( BLOCK );
            if( !isEmpty() ) {
                target.MATCHERS.add( toTargetState() );
            }
            return target;
        }
        
        /** @return Returns a block state matcher reflecting the current state of this builder. */
        private State toTargetState() { return new State( new ArrayList<>( propertiesToMatch.entrySet() ) ); }
    }
}