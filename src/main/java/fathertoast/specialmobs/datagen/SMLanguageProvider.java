package fathertoast.specialmobs.datagen;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.core.register.SMEffects;
import fathertoast.specialmobs.common.core.register.SMEntities;
import fathertoast.specialmobs.common.core.register.SMItems;
import fathertoast.specialmobs.common.util.AnnotationHelper;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.fml.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SMLanguageProvider extends LanguageProvider {
    
    /** All supported translations. */
    public enum TranslationKey {
        ENGLISH( "en" ), SPANISH( "es" ), PORTUGUESE( "pt" ), FRENCH( "fr" ), ITALIAN( "it" ),
        GERMAN( "de" ), PIRATE( "en" );
        
        public final String code;
        
        TranslationKey( String id ) { code = id; }
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
    private static final String[][] TRANSLATIONS;
    
    /** Maps which translation key each lang code uses, allowing multiple lang codes to use the same translations. */
    public static final HashMap<String, TranslationKey> LANG_CODE_MAP = new HashMap<>();
    
    static {
        final ArrayList<String[]> translationList = new ArrayList<>();
        
        final String[] spawnEggTranslationPattern = References.translations( "%s", "%s Spawn Egg",
                "%s", "%s", "%s", "%s", "%s", "%s" ); //TODO
        final String[] vanillaReplacementSpawnEggTranslationPattern = References.translations( "%s", "Normal %s Spawn Egg",
                "%s", "%s", "%s", "%s", "%s", "%s" ); //TODO
        
        // Bestiary-generated translations
        for( MobFamily.Species<?> species : MobFamily.getAllSpecies() ) {
            final String[] speciesTranslations = AnnotationHelper.getTranslations( species );
            String[] spawnEggTranslations = format( species.specialVariantName == null ?
                    vanillaReplacementSpawnEggTranslationPattern : spawnEggTranslationPattern, speciesTranslations );
            spawnEggTranslations[0] = species.spawnEgg.get().getDescriptionId();
            
            translationList.add( speciesTranslations );
            translationList.add( spawnEggTranslations );
        }
        
        // Other entities
        translationList.add( References.translations( SMEntities.BONE_SHRAPNEL.get().getDescriptionId(), "Bone Shrapnel",
                "", "", "", "", "", "" ) ); //TODO
        translationList.add( References.translations( SMEntities.BUG_SPIT.get().getDescriptionId(), "Bug Spit",
                "", "", "", "", "", "" ) ); //TODO
        translationList.add( References.translations( SMEntities.CORPOREAL_FIREBALL.get().getDescriptionId(), "Bug Spit",
                "", "", "", "", "", "" ) ); //TODO
        translationList.add( References.translations( SMEntities.FISHING_BOBBER.get().getDescriptionId(), "Fishing Bobber",
                "", "", "", "", "", "" ) ); //TODO
        
        // Items
        for( RegistryObject<Item> regObject : SMItems.REGISTRY.getEntries() ) {
            // Lazy method of avoiding duplicate entries for now
            if( regObject.get() instanceof ForgeSpawnEggItem ) continue;
            
            final String[] itemTranslations = AnnotationHelper.getTranslations( regObject.get() );
            translationList.add( itemTranslations );
        }
        
        // Misc
        translationList.add( References.translations( SMEffects.VULNERABILITY.get().getDescriptionId(), "Vulnerability",
                "", "", "", "", "", "" ) ); //TODO
        translationList.add( References.translations( SMEffects.WEIGHT.get().getDescriptionId(), "Weight",
                "", "", "", "", "", "" ) ); //TODO
        
        TRANSLATIONS = translationList.toArray( new String[0][0] );
        
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
            boolean missedAny = false;
            for( String[] translationArray : TRANSLATIONS ) {
                if( translationArray[k] == null || translationArray[k].equals( "" ) ) {
                    SpecialMobs.LOG.error( "Translation key {} is missing a translation for lang key \"{}\"!",
                            key.name(), translationArray[0] );
                    missedAny = true;
                }
            }
            if( missedAny && key == TranslationKey.ENGLISH ) throw new IllegalStateException( "Come on, man!" );
        }
        SpecialMobs.LOG.info( "Translation key verification complete!" );
    }
    
    /** Applies single argument string formats 1:1 given an array of formats and an array of arguments. */
    private static String[] format( String[] formats, String[] args ) {
        final String[] formatted = new String[formats.length];
        for( int i = 0; i < formatted.length; i++ ) {
            formatted[i] = String.format( formats[i], args[i] );
        }
        return formatted;
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
            if( translationArray[k] != null && !translationArray[k].isEmpty() )
                add( translationArray[0], translationArray[k] );
        }
    }
}