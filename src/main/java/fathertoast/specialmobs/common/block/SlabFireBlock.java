package fathertoast.specialmobs.common.block;

import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.core.register.SMBlocks;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;

/** Functions identically to normal fire; only visually different. */
public class SlabFireBlock extends FireBlock {
    
    private static final Logger LOG = LogManager.getLogger( SlabFireBlock.class );
    
    
    public enum Type {
        NORMAL( "normal", Blocks.FIRE,
                ( langKey ) -> References.translations( langKey, "Slab Fire",
                        "", "", "", "", "", "" ) ),//TODO
        
        SOUL( "soul", Blocks.SOUL_FIRE,
                ( langKey ) -> References.translations( langKey, "Slab Soul Fire",
                        "", "", "", "", "", "" ) );//TODO
        
        
        private final String ID;
        private final Block PARENT_BLOCK;
        private final Function<String, String[]> TRANSLATIONS;
        
        Type( String id, Block parentBlock, Function<String, String[]> translations ) {
            ID = id;
            PARENT_BLOCK = parentBlock;
            TRANSLATIONS = translations;
        }
        
        /** @return The block id for this slab fire block type. */
        public String blockId() { return "slab_" + ID + "_fire"; }
        
        /** @return A new slab fire block for this type. */
        public Block blockSupplier() { return new SlabFireBlock( this, PARENT_BLOCK ); }
        
        /** @return The 'parent block' of this type; that is, the block this type imitates. */
        public Block parentBlock() { return PARENT_BLOCK; }
        
        /** @return The 'slab block' of this type; that is, the actual slab fire block. */
        public Block block() { return SMBlocks.SLAB_FIRES.get( ordinal() ).get(); }
        
        /** @return The translations of this type. */
        private String[] getTranslations( String langKey ) { return TRANSLATIONS.apply( langKey ); }
        
        /** @return Looks up and returns the translations for the type lang key. */
        private static String[] getTranslationsFor( String langKey ) {
            for( Type type : values() ) {
                if( langKey.contains( type.ID ) ) return type.getTranslations( langKey );
            }
            // This will cause the lang provider to throw an exception for us
            return References.translations( langKey, "", "", "", "", "", "", "" );
        }
        
        /** @return A random slab fire type. */
        public static Type next( RandomSource random ) { return values()[random.nextInt( values().length )]; }
    }
    
    
    private final Type type;
    
    public SlabFireBlock( Type type, Block parentBlock ) {
        super( Properties.copy( parentBlock ) );
        this.type = type;
    }
    
    
    /** @return The type of fire this fire block is. */
    public Type getType() { return type; }
    
    @Override
    protected BlockState getStateWithAge( LevelAccessor levelAccessor, BlockPos pos, int age ) {
        BlockState state = getState( levelAccessor, pos );
        return state.is( Blocks.FIRE ) ? state.setValue( AGE, age ) : state;
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return Type.getTranslationsFor( langKey );
    }
    
    /**
     * @return The appropriate slab fire block state for placement at the given position.
     * If no slab fire state is found, the result of {@link BaseFireBlock#getState(BlockGetter, BlockPos)} is returned instead.
     */
    public static BlockState getState( BlockGetter blockGetter, BlockPos pos ) {
        BlockState fireState = BaseFireBlock.getState( blockGetter, pos );
        for( Type type : Type.values() ) {
            if( fireState.is( type.parentBlock() ) ) {
                BlockState slabFireState = type.block().defaultBlockState();
                
                for( Property<?> prop : fireState.getProperties() ) {
                    try {
                        Object val = fireState.getValue( prop );
                        slabFireState = hackySetValue( slabFireState, prop, val );
                    }
                    catch( Exception e ) {
                        LOG.error( "Failed to copy block state property {} from fire state {} over to slab fire state {}!",
                                prop, fireState, slabFireState );
                    }
                }
                return slabFireState;
            }
        }
        return fireState;
    }
    
    /**
     * Hacky helper method for setting a property value of a block state without knowing the value type.
     *
     * @throws IllegalArgumentException If the property does not exist in the given state or the value is not allowed.
     */
    private static <T extends Comparable<T>> BlockState hackySetValue( BlockState state, Property<?> prop, Object value ) {
        // noinspection unchecked
        return state.setValue( (Property<T>) prop, (T) value );
    }
    
    // TODO configurable delay?
    
    /** @return The fire spread tick delay used for slab fires. */
    private static int getFireTickDelay( RandomSource random ) {
        return 15 + random.nextInt( 10 );
    }
}
