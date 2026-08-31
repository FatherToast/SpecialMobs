package fathertoast.specialmobs.common.block;

import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.core.register.SMBlocks;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;

import java.util.function.Function;

/** Functions identically to normal fire; only visually different. */
public class SlabFireBlock extends FireBlock {
    
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
        public Block block() { return SMBlocks.INFESTED_CORAL.get( ordinal() ).get(); }
        
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
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return Type.getTranslationsFor( langKey );
    }
    
    /** @return The type of fire this fire block is. */
    public Type getType() { return type; }
}
