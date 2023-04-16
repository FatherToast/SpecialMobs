package fathertoast.specialmobs.common.block;

import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.core.register.SMBlocks;
import fathertoast.specialmobs.common.entity.silverfish.PufferSilverfishEntity;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.material.Material;

import java.util.function.Function;

/**
 * A variation of the regular silverfish block intended for placement under water.
 * Contains a puffer silverfish instead of a vanilla one.
 */
public class UnderwaterSilverfishBlock extends InfestedBlock {
    
    public enum Type {
        TUBE( "tube", Blocks.TUBE_CORAL_BLOCK,
                ( langKey ) -> References.translations( langKey, "Infested Tube Coral Block",
                        "", "", "", "", "", "" ) ),//TODO
        
        BRAIN( "brain", Blocks.BRAIN_CORAL_BLOCK,
                ( langKey ) -> References.translations( langKey, "Infested Brain Coral Block",
                        "", "", "", "", "", "" ) ),//TODO
        
        BUBBLE( "bubble", Blocks.BUBBLE_CORAL_BLOCK,
                ( langKey ) -> References.translations( langKey, "Infested Bubble Coral Block",
                        "", "", "", "", "", "" ) ),//TODO
        
        FIRE( "fire", Blocks.FIRE_CORAL_BLOCK,
                ( langKey ) -> References.translations( langKey, "Infested Fire Coral Block",
                        "", "", "", "", "", "" ) ),//TODO
        
        HORN( "horn", Blocks.HORN_CORAL_BLOCK,
                ( langKey ) -> References.translations( langKey, "Infested Horn Coral Block",
                        "", "", "", "", "", "" ) );//TODO
        
        private final String ID;
        private final Block HOST_BLOCK;
        private final Function<String, String[]> TRANSLATIONS;
        
        Type( String id, Block hostBlock, Function<String, String[]> translations ) {
            ID = id;
            HOST_BLOCK = hostBlock;
            TRANSLATIONS = translations;
        }
        
        /** @return The block id for this underwater silverfish block type. */
        public String blockId() { return "infested_" + ID + "_coral_block"; }
        
        /** @return A new underwater silverfish block for this type. */
        public Block blockSupplier() { return new UnderwaterSilverfishBlock( HOST_BLOCK ); }
        
        /** @return The 'host block' of this type; that is, the block this type imitates. */
        public Block hostBlock() { return HOST_BLOCK; }
        
        /** @return The 'infested block' of this type; that is, the actual silverfish block. */
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
        
        /** @return A random underwater silverfish block type. */
        public static Type next( RandomSource random ) { return values()[random.nextInt( values().length )]; }
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) { return Type.getTranslationsFor( langKey ); }
    
    public UnderwaterSilverfishBlock( Block block ) {
        super( block, Properties.of( Material.CLAY ).strength( 0.0F, 0.75F ) );
    }
    
    @Override
    protected void spawnInfestation( ServerLevel level, BlockPos pos ) {
        final PufferSilverfishEntity silverfish = PufferSilverfishEntity.SPECIES.entityType.get().create( level );
        if( silverfish == null ) return;
        
        silverfish.moveTo( pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F );
        level.addFreshEntity( silverfish );
        silverfish.spawnAnim();
    }
}