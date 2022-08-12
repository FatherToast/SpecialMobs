package fathertoast.specialmobs.common.bestiary;

import fathertoast.specialmobs.common.config.family.*;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.core.register.SMEntities;
import fathertoast.specialmobs.common.core.register.SMItems;
import fathertoast.specialmobs.common.util.AnnotationHelper;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

/**
 * Special mobs are broken up into distinct 'families', each of which correspond to a type of vanilla mob that can be
 * replaced. During mob replacement, any member of the family (species) may be chosen, depending on location and config.
 *
 * @see MobFamily.Species
 */
public class MobFamily<T extends LivingEntity, V extends FamilyConfig> {
    /** List of all families, generated to make iteration possible. */
    private static final ArrayList<MobFamily<?, ?>> FAMILY_LIST = new ArrayList<>();
    /** List of all species, generated to make iteration more convenient. */
    private static final List<Species<?>> SPECIES_LIST;
    
    /** Maps each replaceable entity type to the family that replaces it. */
    private static final Map<EntityType<?>, MobFamily<?, ?>> TYPE_TO_FAMILY_MAP;
    
    // NOTE: When adding a new mob family, do not forget to also register its renderer in the client register!
    
    public static final MobFamily<CreeperEntity, CreeperFamilyConfig> CREEPER = new MobFamily<>( CreeperFamilyConfig::new,
            "Creeper", "creepers", 0x0DA70B, new EntityType[] { EntityType.CREEPER },
            "Dark", "Death", "Dirt", "Doom", "Drowning", "Ender", "Fire", "Gravel", "Jumping", "Lightning",
            "Mini", "Sand", /*"Scope",*/ "Snow", "Skeleton", "Splitting"
    );//TODO scope
    
    public static final MobFamily<ZombieEntity, FamilyConfig> ZOMBIE = new MobFamily<>( FamilyConfig::newLessSpecial,
            "Zombie", "zombies", 0x00AFAF, new EntityType[] { EntityType.ZOMBIE, EntityType.HUSK },
            "Brute", "Fire", "Fishing", "Frozen", "Giant", "Hungry", "Husk", "MadScientist", "Plague"
    );
    public static final MobFamily<DrownedEntity, FamilyConfig> DROWNED = new MobFamily<>( FamilyConfig::new,
            "Drowned", "drowned", 0x8FF1D7, new EntityType[] { EntityType.DROWNED },
            "Brute", "Fishing", "Giant", "Hungry", "Knight", "Plague" //TODO Textures! - brute, hungry, plague, cold ocean, warm ocean
    );// ice/fire themes? (cold/warm ocean) - convert from frozen/fire zombies
    public static final MobFamily<ZombifiedPiglinEntity, FamilyConfig> ZOMBIFIED_PIGLIN = new MobFamily<>( FamilyConfig::new,
            "ZombifiedPiglin", "zombified piglins", 0xEA9393, new EntityType[] { EntityType.ZOMBIFIED_PIGLIN },
            "Brute", "Fishing", "Giant", "Hungry", "Knight", "Plague", "Vampire"//TODO figure out crossbows
    );
    
    public static final MobFamily<AbstractSkeletonEntity, SkeletonFamilyConfig> SKELETON = new MobFamily<>( SkeletonFamilyConfig::new,
            "Skeleton", "skeletons", 0xC1C1C1, new EntityType[] { EntityType.SKELETON, EntityType.STRAY },
            "Brute", "Fire", "Gatling", "Giant", "Knight", "Ninja", "Poison", "Sniper", "Spitfire", "Stray", "Weathered"
    );
    public static final MobFamily<AbstractSkeletonEntity, SkeletonFamilyConfig> WITHER_SKELETON = new MobFamily<>( SkeletonFamilyConfig::new,
            "WitherSkeleton", "wither skeletons", 0x141414, new EntityType[] { EntityType.WITHER_SKELETON },
            "Brute", "Gatling", "Giant", "Knight", "Ninja", "Sniper", "Spitfire"
    );
    
    public static final MobFamily<SlimeEntity, SlimeFamilyConfig> SLIME = new MobFamily<>( SlimeFamilyConfig::new,
            "Slime", "slimes", 0x51A03E, new EntityType[] { EntityType.SLIME },
            "Blackberry", "Blueberry", "Caramel", "Frozen", "Grape", "Lemon", "Potion", "Strawberry", "Watermelon"
    );
    public static final MobFamily<MagmaCubeEntity, FamilyConfig> MAGMA_CUBE = new MobFamily<>( FamilyConfig::newMoreSpecial,
            "MagmaCube", "magma cubes", 0x340000, new EntityType[] { EntityType.MAGMA_CUBE },
            "Bouncing", "Hardened", "Sticky", "Volatile"
    );
    
    public static final MobFamily<SpiderEntity, FamilyConfig> SPIDER = new MobFamily<>( FamilyConfig::newMoreSpecial,
            "Spider", "spiders", 0x342D27, new EntityType[] { EntityType.SPIDER },
            "Baby", "Desert", "Fire", "Flying", "Giant", "Hungry", "Mother", "Pale", "Poison", "Water", "Web", "Witch"
    );
    public static final MobFamily<CaveSpiderEntity, FamilyConfig> CAVE_SPIDER = new MobFamily<>( FamilyConfig::newMoreSpecial,
            "CaveSpider", "cave spiders", 0x0C424E, new EntityType[] { EntityType.CAVE_SPIDER },
            "Baby", "Desert", "Fire", "Flying", "Mother", "Pale", "Water", "Web", "Witch"
    );
    
    public static final MobFamily<SilverfishEntity, SilverfishFamilyConfig> SILVERFISH = new MobFamily<>( SilverfishFamilyConfig::new,
            "Silverfish", "silverfish", 0x6E6E6E, new EntityType[] { EntityType.SILVERFISH },
            "Albino", "Blinding", "Desiccated", "Fire", "Fishing", "Flying", "Poison", "Puffer", "Tough"
    );
    
    public static final MobFamily<EndermanEntity, FamilyConfig> ENDERMAN = new MobFamily<>( FamilyConfig::new,
            "Enderman", "endermen", 0x161616, new EntityType[] { EntityType.ENDERMAN },
            "Blinding", "Flame", "Icy", "Lightning", "Mini", "Mirage", "Runic", "Thief"
    );
    
    public static final MobFamily<WitchEntity, WitchFamilyConfig> WITCH = new MobFamily<>( WitchFamilyConfig::new,
            "Witch", "witches", 0x340000, new EntityType[] { EntityType.WITCH },
            "Domination", "Shadows", "Undead", "Wilds", "Wind"
    );
    
    public static final MobFamily<GhastEntity, GhastFamilyConfig> GHAST = new MobFamily<>( GhastFamilyConfig::new,
            "Ghast", "ghasts", 0xF9F9F9, new EntityType[] { EntityType.GHAST },
            "Baby", "CorporealShift", "Fighter", "King", "Queen", "Unholy"
    );
    
    public static final MobFamily<BlazeEntity, FamilyConfig> BLAZE = new MobFamily<>( FamilyConfig::new,
            "Blaze", "blazes", 0xF6B201, new EntityType[] { EntityType.BLAZE },
            "Cinder", "Conflagration", "Ember", "Hellfire", "Inferno", "Jolt", "Wildfire"
    );
    
    static {
        FAMILY_LIST.trimToSize();
        
        final HashMap<EntityType<?>, MobFamily<?, ?>> classToFamilyMap = new HashMap<>();
        final ArrayList<Species<?>> allSpecies = new ArrayList<>();
        
        for( MobFamily<?, ?> family : FAMILY_LIST ) {
            for( EntityType<?> replaceable : family.replaceableTypes )
                classToFamilyMap.put( replaceable, family );
            
            allSpecies.add( family.vanillaReplacement );
            allSpecies.addAll( Arrays.asList( family.variants ) );
        }
        allSpecies.trimToSize();
        
        TYPE_TO_FAMILY_MAP = Collections.unmodifiableMap( classToFamilyMap );
        SPECIES_LIST = Collections.unmodifiableList( allSpecies );
    }
    
    
    //--------------- Static Helper Methods ----------------
    
    /** Called during mod construction to initialize the bestiary. */
    public static void initBestiary() { }
    
    /** @return A list of all families. */
    public static List<MobFamily<?, ?>> getAll() { return Collections.unmodifiableList( FAMILY_LIST ); }
    
    /** @return A list of all species. */
    public static List<Species<?>> getAllSpecies() { return SPECIES_LIST; }
    
    /** @return The family of mobs that can replace the passed entity; returns null if the entity is not replaceable. */
    @Nullable
    public static MobFamily<?, ?> getReplacementFamily( Entity entity ) { return TYPE_TO_FAMILY_MAP.get( entity.getType() ); }
    
    
    //--------------- Family Instance Implementations ----------------
    
    /** The technical name that refers to this family. Note that this is UpperCamelCase, but is often used in lowercase. */
    public final String name;
    /** The plural name used to refer to this family in unlocalized situations; e.g. config comments. */
    public final String configName;
    
    /** The base color for spawn eggs of species in this family. Species' eggs differ visually only by spot color. */
    public final int eggBaseColor;
    
    /** List of replaceable entity types. The vanilla replacement's entity type is based on the first entry in this array. */
    public final EntityType<?>[] replaceableTypes;
    
    /**
     * The vanilla replacement species for this family. These will subtly replace the vanilla mob and provide extra
     * capabilities (unless disabled by config) - both for the mob and for map/pack creators.
     */
    public final Species<? extends T> vanillaReplacement;
    /** Array of all special variant species in this family. In practice, these are the true "special mobs". */
    public final Species<? extends T>[] variants;
    
    /** This family's config. */
    public final V config;
    
    private MobFamily( Function<MobFamily<?, ?>, V> configSupplier,
                       String familyName, String readableName, int eggColor, EntityType<?>[] replaceable,
                       String... variantNames ) {
        name = familyName;
        configName = readableName.toLowerCase( Locale.ROOT );
        eggBaseColor = eggColor;
        replaceableTypes = replaceable;
        if( replaceable.length < 1 )
            throw new IllegalArgumentException( familyName + " family must have at least one replaceable type!" );
        
        final String packageRoot = References.ENTITY_PACKAGE + name.toLowerCase();
        vanillaReplacement = new Species<>( this, packageRoot, null );
        
        //noinspection unchecked
        variants = new Species[variantNames.length];
        for( int i = 0; i < variants.length; i++ ) {
            variants[i] = new Species<>( this, packageRoot, variantNames[i] );
        }
        
        config = configSupplier.apply( this );
        config.SPEC.initialize();
        
        // We register here because otherwise there's no way to find all families
        FAMILY_LIST.add( this );
    }
    
    /** Pick a new species from this family, based on the location. */
    public Species<? extends T> nextVariant( World world, @Nullable BlockPos pos ) {
        return nextVariant( world, pos, null, vanillaReplacement );
    }
    
    /** Pick a new species from this family, based on the location. */
    public Species<? extends T> nextVariant( World world, @Nullable BlockPos pos, @Nullable Function<Species<?>, Boolean> selector, Species<? extends T> fallback ) {
        final Species<?> species = config.GENERAL.specialVariantList.next( world.random, world, pos, selector );
        //noinspection unchecked
        return species == null ? fallback : (Species<? extends T>) species;
    }
    
    
    //--------------- Species Instance Implementations ----------------
    
    /**
     * Each special mob family is effectively a collection of special mob species, and each species corresponds to one
     * entity type.
     * <p>
     * There are two types of species; vanilla replacements that closely resemble their vanilla counterparts, and
     * special variants that differ both visually and mechanically. Each family has exactly one vanilla replacement
     * species and may have any number of special variants.
     * <p>
     * Though typically special variant entity classes will extend the vanilla replacement, this cannot always be assumed.
     *
     * @see MobFamily
     */
    public static class Species<T extends LivingEntity> {
        /** The special mob family this species belongs to. */
        public final MobFamily<? super T, ?> family;
        /** The name of this special variant; or null for vanilla replacement species. */
        public final String specialVariantName;
        /** The technical name that refers to this species. Note that this is UpperCamelCase, but is often used in lowercase. */
        public final String name;
        
        /** The entity class for this species. */
        public final Class<T> entityClass;
        /** The bestiary info describing this species. */
        public final BestiaryInfo bestiaryInfo;
        
        /** This species's entity type, wrapped in its registry object. */
        public final RegistryObject<EntityType<T>> entityType;
        /** This species's spawn egg item, wrapped in its registry object. */
        public final RegistryObject<ForgeSpawnEggItem> spawnEgg;
        
        /** This species's config. */
        public final SpeciesConfig config;
        
        /** Constructs a new mob species. For vanilla replacements, the variant name is null. */
        private Species( MobFamily<? super T, ?> parentFamily, String packageRoot, @Nullable String variantName ) {
            final boolean vanillaReplacement = variantName == null;
            
            family = parentFamily;
            specialVariantName = variantName;
            name = vanillaReplacement ? parentFamily.name : variantName + parentFamily.name;
            
            // Below require unlocalized name to be defined
            entityClass = findClass( vanillaReplacement ?
                    References.VANILLA_REPLACEMENT_FORMAT : References.SPECIAL_VARIANT_FORMAT, packageRoot );
            
            // Below require variant class to be defined
            final EntityType.Builder<T> entityTypeBuilder = makeEntityTypeBuilder( parentFamily.replaceableTypes[0] );
            bestiaryInfo = AnnotationHelper.getBestiaryInfo( this, BestiaryInfo.of( this, entityTypeBuilder ) ).build();
            
            // Initialize deferred registry objects
            entityType = SMEntities.register( name.toLowerCase( Locale.ROOT ), entityTypeBuilder );
            spawnEgg = SMItems.registerSpawnEgg( entityType, parentFamily.eggBaseColor, bestiaryInfo.eggSpotsColor );
            
            // Config uses bestiary info for default values
            config = AnnotationHelper.createConfig( this );
            config.SPEC.initialize();
            
            // Register this species with the entity class
            AnnotationHelper.injectSpeciesReference( this );
            AnnotationHelper.verifySpeciesSupplier( this );
        }
        
        /** Finds the entity class based on a standard format. */
        private Class<T> findClass( String format, String packageRoot ) {
            try {
                //noinspection unchecked
                return (Class<T>) Class.forName( String.format( format, packageRoot, name ) );
            }
            catch( ClassNotFoundException ex ) {
                throw new RuntimeException( "Failed to find entity class for mob species " + name, ex );
            }
        }
        
        /**
         * Builds a deep copy of an entity type with a different entity constructor.
         * Leaves the new entity type "un-built" so it can be further modified, if needed.
         */
        private EntityType.Builder<T> makeEntityTypeBuilder( EntityType<?> original ) {
            final EntityType.IFactory<T> factory = AnnotationHelper.getEntityFactory( this );
            final EntityType.Builder<T> clone = EntityType.Builder.of( factory, original.getCategory() );
            
            if( !original.canSummon() ) clone.noSummon();
            if( !original.canSerialize() ) clone.noSave();
            if( original.fireImmune() ) clone.fireImmune();
            if( original.canSpawnFarFromPlayer() ) clone.canSpawnFarFromPlayer();
            
            return clone.sized( original.getWidth(), original.getHeight() ).immuneTo( original.immuneTo.toArray( new Block[0] ) )
                    // Note: the below are (or also have) suppliers and they cannot be built with the vanilla builder
                    //          - this is okay for us because we only replace vanilla mobs
                    .clientTrackingRange( original.clientTrackingRange() ).updateInterval( original.updateInterval() )
                    .setShouldReceiveVelocityUpdates( original.trackDeltas() );
        }
    }
}