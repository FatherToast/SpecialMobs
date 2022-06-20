package fathertoast.specialmobs.datagen;

import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.HashMap;

public class SMLanguageProvider extends LanguageProvider {
    
    /** All supported translations. */
    public enum TranslationKey {
        ENGLISH( "en" ), SPANISH( "es" ), PORTUGUESE( "pt" ), FRENCH( "fr" ), ITALIAN( "it" ),
        GERMAN( "de" ), PIRATE( "en" );
        
        public final String code;
        
        TranslationKey( String id ) { code = id; }
    }
    
    /** This method provides helper tags to make linking translations up easier, and also enforces the correct array length. */
    private static String[] translations( String key, String en, String es, String pt, String fr, String it, String de, String pir ) {
        // Note that this must match up EXACTLY to the TranslationKey enum above
        String[] translation = { key, en, es, pt, fr, it, de, pir };
        
        // Fix the encoding to allow us to use accented characters in the translation string literals
        // Note: If a translation uses any non-ASCII characters, make sure they are all in this matrix! (case-sensitive)
        final String[][] utf8ToUnicode = {
                { "à", "\u00E0" }, { "á", "\u00E1" }, { "ã", "\u00E3" }, { "ä", "\u00E4" },
                { "ç", "\u00E7" },
                { "è", "\u00E8" }, { "é", "\u00E9" }, { "ê", "\u00EA" },
                { "í", "\u00ED" },
                { "ó", "\u00F3" }, { "õ", "\u00F5" }, { "ö", "\u00F6" },
                { "ù", "\u00F9" }, { "û", "\u00FB" }, { "ü", "\u00FC" },
                { "œ", "\u0153" }
        };
        for( int i = 1; i < translation.length; i++ ) {
            for( String[] fix : utf8ToUnicode )
                translation[i] = translation[i].replace( fix[0], fix[1] ); // Note: This is kinda dumb, but it works so idc
        }
        return translation;
    }
    
    /**
     * Matrix linking the actual translations to their lang key.
     * <p>
     * Each row of the matrix is one translation array.
     * In each translation array, the lang key is at index 0 and the translation for a particular
     * translation key is at index (translationKey.ordinal() + 1).
     *
     * @see #addTranslations()
     */
    @SuppressWarnings( "SpellCheckingInspection" )
    private static final String[][] TRANSLATIONS = {
            // NYI
    };
    
    /** Maps which translation key each lang code uses, allowing multiple lang codes to use the same translations. */
    public static final HashMap<String, TranslationKey> LANG_CODE_MAP = new HashMap<>();
    
    static {
        // Assign all specific locales to the translation we want to use
        mapAll( TranslationKey.ENGLISH, "us" ); // We can ignore other English locales, en_us is the fallback for all languages
        mapAll( TranslationKey.SPANISH, "es", "ar", "cl", "ec", "mx", "uy", "ve" );
        mapAll( TranslationKey.PORTUGUESE, "pt", "br" );
        mapAll( TranslationKey.FRENCH, "fr", "ca" );
        mapAll( TranslationKey.ITALIAN, "it" );
        mapAll( TranslationKey.GERMAN, "de", "at", "ch" );
        mapAll( TranslationKey.PIRATE, "pt" );
        
        // Make sure all supported languages are completely implemented
        SpecialMobs.LOG.info( "Starting translation key verification..." );
        for( TranslationKey key : TranslationKey.values() ) {
            if( !LANG_CODE_MAP.containsValue( key ) ) {
                SpecialMobs.LOG.error( "Translation key {} has no lang codes assigned!", key.name() );
            }
            final int k = key.ordinal() + 1;
            for( String[] translationArray : TRANSLATIONS ) {
                if( translationArray[k] == null || translationArray[k].equals( "" ) ) {
                    SpecialMobs.LOG.error( "Translation key {} is missing a translation for lang key \"{}\"!",
                            key.name(), translationArray[0] );
                }
            }
        }
        SpecialMobs.LOG.info( "Translation key verification complete!" );
    }
    
    /** Maps any number of locale codes to a single translation. */
    private static void mapAll( TranslationKey translation, String... locales ) {
        for( String locale : locales ) {
            LANG_CODE_MAP.put( translation.code + "_" + locale, translation );
        }
    }
    
    /** The translation key to use for this locale. */
    private final TranslationKey translationKey;
    
    /** Creates a language provider for a specific locale. This correlates to exactly one .json file. */
    public SMLanguageProvider( DataGenerator gen, String locale, TranslationKey translateKey ) {
        super( gen, SpecialMobs.MOD_ID, locale );
        translationKey = translateKey;
    }
    
    /**
     * Build the .json file for this locale (based solely on its translation key).
     *
     * @see SMLanguageProvider#TRANSLATIONS
     */
    @Override
    protected void addTranslations() {
        final int k = translationKey.ordinal() + 1;
        for( String[] translationArray : TRANSLATIONS ) {
            add( translationArray[0], translationArray[k] );
        }
    }
}