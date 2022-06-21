package fathertoast.specialmobs.common.bestiary;

import fathertoast.specialmobs.common.core.register.SMEntities;
import fathertoast.specialmobs.common.core.register.SMItems;
import fathertoast.specialmobs.common.util.AnnotationHelper;
import fathertoast.specialmobs.common.util.References;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Special mobs are broken up into distinct 'families', each of which correspond to a type of vanilla mob that can be
 * replaced. During mob replacement, any member of the family (species) may be chosen, depending on location and config.
 *
 * @see MobFamily.Species
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MobFamily<T extends LivingEntity> {
    /** List of all families, generated to make iteration possible. */
    private static final List<MobFamily<?>> FAMILY_LIST = new ArrayList<>();
    /** List of all species, generated to make iteration more convenient. */
    private static final List<Species<?>> SPECIES_LIST;
    
    /** Maps each replaceable entity type to the family that replaces it. */
    private static final Map<EntityType<?>, MobFamily<?>> TYPE_TO_FAMILY_MAP;
    
    // NOTE: When adding a new mob family, do not forget to also register its renderer in the client register!
    
    public static final MobFamily<CreeperEntity> CREEPER = new MobFamily<>(
            "Creeper", "creepers", 0x0DA70B, new EntityType[] { EntityType.CREEPER },
            "Dark", "Death", "Dirt", "Doom", "Drowning", /*"Ender",*/ "Fire", "Gravel"//, "Jumping", "Lightning",
            //"Mini", "Scope", "Splitting"
    );
    
    //    public static final MobFamily<ZombieEntity> ZOMBIE = new MobFamily<>(
    //            "Zombie", "zombies", 0x00AFAF, new EntityType[] { EntityType.ZOMBIE, EntityType.HUSK },
    //            "Brute", "Fire", "Fishing", "Giant", "Hungry", "Husk", "Plague"
    //    );
    //    public static final MobFamily<ZombieEntity> ZOMBIFIED_PIGLIN = new MobFamily<>(
    //            "ZombifiedPiglin", "zombie pigmen", 0xEA9393, new EntityType[] { EntityType.ZOMBIFIED_PIGLIN },
    //            "Brute", "Fishing", "Giant", "Hungry", "Knight", "Plague", "Vampire"
    //    );
    
    //    public static final MobFamily<AbstractSkeletonEntity> SKELETON = new MobFamily<>(
    //            "Skeleton", "skeletons", 0xC1C1C1, new EntityType[] { EntityType.SKELETON, EntityType.STRAY },
    //            "Brute", "Fire", "Gatling", "Giant", "Knight", "Ninja", "Poison", "Sniper", "Spitfire", "Stray"
    //    );
    //    public static final MobFamily<AbstractSkeletonEntity> WITHER_SKELETON = new MobFamily<>(
    //            "WitherSkeleton", "wither skeletons", 0x141414, new EntityType[] { EntityType.WITHER_SKELETON },
    //            "Brute", "Gatling", "Giant", "Knight", "Ninja", "Sniper", "Spitfire"
    //    );
    
    //    public static final MobFamily<SlimeEntity> SLIME = new MobFamily<>(
    //            "Slime", "slimes", 0x51A03E, new EntityType[] { EntityType.SLIME },
    //            "Blackberry", "Blueberry", "Caramel", "Grape", "Lemon", "Strawberry", "Watermelon"
    //    );
    //    public static final MobFamily<MagmaCubeEntity> MAGMA_CUBE = new MobFamily<>(
    //            "MagmaCube", "magma cubes", 0x340000, new EntityType[] { EntityType.MAGMA_CUBE },
    //            "Flying", "Hardened", "Sticky", "Volatile"
    //    );
    
    //    public static final MobFamily<SpiderEntity> SPIDER = new MobFamily<>(
    //            "Spider", "spiders", 0x342D27, new EntityType[] { EntityType.SPIDER },
    //            "Baby", "Desert", "Flying", "Giant", "Hungry", "Mother", "Pale", "Poison", "Web", "Witch"
    //    );
    //    public static final MobFamily<CaveSpiderEntity> CAVE_SPIDER = new MobFamily<>(
    //            "CaveSpider", "cave spiders", 0x0C424E, new EntityType[] { EntityType.CAVE_SPIDER },
    //            "Baby", "Flying", "Mother", "Web", "Witch"
    //    );
    
    //    public static final MobFamily<SilverfishEntity> SILVERFISH = new MobFamily<>(
    //            "Silverfish", "silverfish", 0x6E6E6E, new EntityType[] { EntityType.SILVERFISH },
    //            "Blinding", "Fishing", "Flying", "Poison", "Tough"
    //    );
    
    //    public static final MobFamily<EndermanEntity> ENDERMAN = new MobFamily<>(
    //            "Enderman", "endermen", 0x161616, new EntityType[] { EntityType.ENDERMAN },
    //            "Blinding", "Icy", "Lightning", "Mini", "Mirage", "Thief"
    //    );
    
    //    public static final MobFamily<WitchEntity> WITCH = new MobFamily<>(
    //            "Witch", "witches", 0x340000, new EntityType[] { EntityType.WITCH },
    //            "Domination", "Shadows", "Undead", "Wilds", "Wind"
    //    );
    
    //    public static final MobFamily<GhastEntity> GHAST = new MobFamily<>(
    //            "Ghast", "ghasts", 0xF9F9F9, new EntityType[] { EntityType.GHAST },
    //            "Baby", "Fighter", "King", "Queen", "Unholy"
    //    );
    
    //    public static final MobFamily<BlazeEntity> BLAZE = new MobFamily<>(
    //            "Blaze", "blazes", 0xF6B201, new EntityType[] { EntityType.BLAZE },
    //            "Cinder", "Conflagration", "Ember", "Hellfire", "Inferno", "Jolt", "Wildfire"
    //    );
    
    static {
        final HashMap<EntityType<?>, MobFamily<?>> classToFamilyMap = new HashMap<>();
        final ArrayList<Species<?>> allSpecies = new ArrayList<>();
        
        for( MobFamily<?> family : FAMILY_LIST ) {
            for( EntityType<?> replaceable : family.replaceableTypes )
                classToFamilyMap.put( replaceable, family );
            
            allSpecies.add( family.vanillaReplacement );
            allSpecies.addAll( Arrays.asList( family.variants ) );
        }
        allSpecies.trimToSize();
        
        TYPE_TO_FAMILY_MAP = Collections.unmodifiableMap( classToFamilyMap );
        SPECIES_LIST = Collections.unmodifiableList( allSpecies );
    }
    
    /** Called during mod construction to initialize the bestiary. */
    public static void initBestiary() { }
    
    /** @return A list of all families. */
    public static List<MobFamily<?>> getAll() { return Collections.unmodifiableList( FAMILY_LIST ); }
    
    /** @return A list of all species. */
    public static List<Species<?>> getAllSpecies() { return SPECIES_LIST; }
    
    /** @return The family of mobs that can replace the passed entity; returns null if the entity is not replaceable. */
    @Nullable
    public static MobFamily<?> getReplacementFamily( LivingEntity entity ) {
        return TYPE_TO_FAMILY_MAP.get( entity.getType() );
    }
    
    /** The technical name that refers to this family. Note that this is UpperCamelCase, but is often used in lowercase. */
    public final String name;
    /** The name used to refer to this family in unlocalized situations; e.g. config comments. */
    public final String configName;
    
    /** The base egg color for species in this family. Species' eggs differ visually only by spot color. */
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
    
    //public Config.FamilyConfig config;
    
    private MobFamily( String familyName, String familyKey, int eggColor, EntityType<?>[] replaceable,
                       String... variantNames ) {
        name = familyName;
        configName = familyKey;
        eggBaseColor = eggColor;
        replaceableTypes = replaceable;
        if( replaceable.length < 1 )
            throw new IllegalArgumentException( familyName + " family must have at least one replaceable type!" );
        
        final String packageRoot = References.ENTITY_PACKAGE + name.toLowerCase() + ".";
        vanillaReplacement = new Species<>( this, packageRoot, null );
        //noinspection unchecked
        variants = new Species[variantNames.length];
        for( int i = 0; i < variants.length; i++ ) {
            variants[i] = new Species<>( this, packageRoot, variantNames[i] );
        }
        
        // We register here because otherwise there's no way to find all families
        FAMILY_LIST.add( this );
    }
    
    /** Pick a new species from this family, based on the location. */
    public Species<? extends T> nextVariant( World world, BlockPos pos ) { // TODO mob replacer
        // Build weights for the current location
        int totalWeight = 0;
        int[] variantWeights = new int[variants.length];
        for( int i = 0; i < variants.length; i++ ) {
            int weight = 0;//config.getVariantWeight( world, pos, variants[i] ); TODO configs
            if( weight > 0 ) {
                totalWeight += weight;
                variantWeights[i] = weight;
            }
        }
        
        // Pick one item at random
        if( totalWeight > 0 ) {
            int weight = world.random.nextInt( totalWeight );
            for( int i = 0; i < variants.length; i++ ) {
                if( variantWeights[i] > 0 ) {
                    weight -= variantWeights[i];
                    if( weight < 0 ) {
                        return variants[i];
                    }
                }
            }
        }
        return vanillaReplacement;
    }
    
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
    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    public static class Species<T extends LivingEntity> {
        /** The special mob family this species belongs to. */
        public final MobFamily<? super T> family;
        /** The name of this special variant; or null for vanilla replacement species. */
        public final String specialVariantName;
        /** The technical name that refers to this species. Note that this is UpperCamelCase, but is often used in lowercase. */
        public final String name;
        
        /** The entity class for this species. */
        public final Class<T> entityClass;
        /** The bestiary info describing this species */
        public final BestiaryInfo bestiaryInfo;
        
        /** This species's entity type, wrapped in its registry object. */
        public final RegistryObject<EntityType<T>> entityType;
        /** This species's spawn egg item, wrapped in its registry object. */
        public final RegistryObject<ForgeSpawnEggItem> spawnEgg;
        
        /** Constructs a new mob species. For vanilla replacements, the variant name is null. */
        private Species( MobFamily<? super T> parentFamily, String packageRoot, @Nullable String variantName ) {
            final boolean vanillaReplacement = variantName == null;
            
            family = parentFamily;
            specialVariantName = variantName;
            name = vanillaReplacement ? parentFamily.name : variantName + parentFamily.name;
            
            // Below require unlocalized name to be defined
            entityClass = findClass( vanillaReplacement ?
                    References.VANILLA_REPLACEMENT_FORMAT : References.SPECIAL_VARIANT_FORMAT, packageRoot );
            
            // Below require variant class to be defined
            final EntityType.Builder<T> entityTypeBuilder = makeEntityTypeBuilder( parentFamily.replaceableTypes[0] );
            bestiaryInfo = makeBestiaryInfo( entityTypeBuilder );
            
            // Initialize deferred registry objects
            entityType = SMEntities.register( name.toLowerCase( Locale.ROOT ), entityTypeBuilder );
            spawnEgg = SMItems.registerSpawnEgg( entityType, parentFamily.eggBaseColor, bestiaryInfo.eggSpotsColor );
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
        
        /** Calls on this species' entity class to generate its bestiary info. */
        private BestiaryInfo makeBestiaryInfo( EntityType.Builder<T> entityTypeBuilder ) {
            try {
                return AnnotationHelper.getBestiaryInfo( this, entityTypeBuilder );
            }
            catch( IllegalAccessException | NoSuchMethodException | InvocationTargetException ex ) {
                throw new RuntimeException( "Entity class for " + name + " has invalid bestiary info method", ex );
            }
        }
        
        /**
         * Builds a deep copy of an entity type with a different entity constructor.
         * Leaves the new entity type "un-built" so it can be further modified, if needed.
         */
        private EntityType.Builder<T> makeEntityTypeBuilder( EntityType<?> original ) {
            final EntityType.IFactory<T> factory;
            try {
                factory = AnnotationHelper.getEntityFactory( this );
            }
            catch( NoSuchMethodException ex ) {
                throw new RuntimeException( "Entity class for " + name + " has no valid constructors", ex );
            }
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