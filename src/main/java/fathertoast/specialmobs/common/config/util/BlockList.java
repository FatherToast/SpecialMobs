package fathertoast.specialmobs.common.config.util;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.file.TomlHelper;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A list of block entries used to match specific block states.
 */
@SuppressWarnings( "unused" )
public class BlockList implements IStringArray {
    /** The block-value entries in this list. */
    private final Map<Block, BlockEntry> UNDERLYING_MAP = new HashMap<>();
    /** The list used to write back to file. Consists of cloned single-state block entries. */
    private final List<BlockEntry> PRINT_LIST = new ArrayList<>();
    
    /**
     * Create a new block list from an array of entries. Used for creating default configs.
     * <p>
     * This method of block list creation can not take advantage of the * notation.
     */
    public BlockList( BlockEntry... entries ) {
        for( BlockEntry entry : entries ) {
            mergeFrom( entry );
        }
    }
    
    /**
     * Create a new block list from a list of block state strings.
     */
    public BlockList( AbstractConfigField field, List<String> entries ) {
        for( String line : entries ) {
            if( line.endsWith( "*" ) ) {
                // Handle special case; add all blocks in namespace
                mergeFromNamespace( line.substring( 0, line.length() - 1 ) );
            }
            else {
                // Add a single block entry
                BlockEntry entry = new BlockEntry( field, line );
                if( entry.BLOCK == Blocks.AIR ) {
                    SpecialMobs.LOG.warn( "Invalid entry for {} \"{}\"! Deleting entry. Invalid entry: {}",
                            field.getClass(), field.getKey(), line );
                }
                else {
                    mergeFrom( entry );
                }
            }
        }
    }
    
    /** @return A string representation of this object. */
    @Override
    public String toString() { return TomlHelper.toLiteral( PRINT_LIST.toArray() ); }
    
    /** @return Returns true if this object has the same value as another object. */
    @Override
    public boolean equals( @Nullable Object other ) {
        if( !(other instanceof BlockList) ) return false;
        // Compare by the string list view of the object
        return toStringList().equals( ((BlockList) other).toStringList() );
    }
    
    /** @return A list of strings that will represent this object when written to a toml file. */
    @Override
    public List<String> toStringList() {
        // Create a list of the entries in string format
        List<String> list = new ArrayList<>( PRINT_LIST.size() );
        for( BlockEntry entry : PRINT_LIST ) {
            list.add( entry.toString() );
        }
        return list;
    }
    
    /** @return Returns true if there are no entries in this block list. */
    public boolean isEmpty() { return UNDERLYING_MAP.isEmpty(); }
    
    /** @return Returns true if the block is contained in this list. */
    public boolean matches( BlockState blockState ) {
        BlockEntry entry = UNDERLYING_MAP.get( blockState.getBlock() );
        return entry != null && entry.matches( blockState );
    }
    
    /** @param otherEntry Merges all matching from a block entry into this list. */
    private void mergeFrom( BlockEntry otherEntry ) {
        PRINT_LIST.add( otherEntry );
        BlockEntry currentEntry = UNDERLYING_MAP.get( otherEntry.BLOCK );
        if( currentEntry == null ) {
            UNDERLYING_MAP.put( otherEntry.BLOCK, otherEntry );
        }
        else {
            currentEntry.mergeFrom( otherEntry );
        }
    }
    
    /** @param namespace Merges all blocks (and all states) with registry keys that begin with this string into this list. */
    private void mergeFromNamespace( String namespace ) {
        for( ResourceLocation regKey : ForgeRegistries.BLOCKS.getKeys() ) {
            if( regKey.toString().startsWith( namespace ) ) {
                final Block block = ForgeRegistries.BLOCKS.getValue( regKey );
                if( block != null && block != Blocks.AIR )
                    mergeFrom( new BlockEntry( block ) );
            }
        }
    }
}