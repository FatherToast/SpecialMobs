package fathertoast.specialmobs.common.bestiary;

import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.specialmobs.common.config.family.*;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.core.register.SMEntities;
import fathertoast.specialmobs.common.core.register.SMItems;
import fathertoast.specialmobs.common.util.AnnotationHelper;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Special mobs are broken up into distinct 'families', each of which correspond to a type of vanilla mob that can be
 * replaced. During mob replacement, any member of the family (species) may be chosen, depending on location and config.
 *
 * @see MobFamily.Species
 */
public class MobFamily<T extends Mob, V extends FamilyConfig> {
    /** List of all families, generated to make iteration possible. */
    private static final ArrayList<MobFamily<?, ?>> FAMILY_LIST = new ArrayList<>();
    /** List of all species, generated to make iteration more convenient. */
    private static final List<Species<?>> SPECIES_LIST;
    
    /** Maps each replaceable entity type to the family that replaces it. */
    private static final Map<EntityType<?>, MobFamily<?, ?>> TYPE_TO_FAMILY_MAP;
    
    // NOTE: When adding a new mob family, do not forget to also register its renderer in the client register!
    
    public static final MobFamily<Creeper, CreeperFamilyConfig> CREEPER = new MobFamily<>( CreeperFamilyConfig::new,
            "Creeper", "creepers", 0x0DA70B, new EntityType[] { EntityType.CREEPER },
            "Dark", "Death", "Dirt", "Doom", "Drowning", "Ender", "Fire", "Gravel", "Jumping", "Lightning",
            "Mini", "Sand", "Scope", "Snow", "Skeleton", "Splitting"
    );
    
    public static final MobFamily<Zombie, FamilyConfig> ZOMBIE = new MobFamily<>( FamilyConfig::newLessSpecial,
            "Zombie", "zombies", 0x00AFAF, new EntityType[] { EntityType.ZOMBIE, EntityType.HUSK },
            "Brute", "Fire", "Fishing", "Frozen", "Giant", "Hungry", "Husk", "MadScientist", "Plague"
    );
    public static final MobFamily<Drowned, FamilyConfig> DROWNED = new MobFamily<>( FamilyConfig::new,
            "Drowned", "drowned", 0x8FF1D7, new EntityType[] { EntityType.DROWNED },
            "Abyssal", "Brute", "Fishing", "Frozen", "Giant", "Hungry", "Knight", "Plague"//, "Tropical"
    );
    public static final MobFamily<ZombifiedPiglin, FamilyConfig> ZOMBIFIED_PIGLIN = new MobFamily<>( FamilyConfig::new,
            "ZombifiedPiglin", "zombified piglins", 0xEA9393, new EntityType[] { EntityType.ZOMBIFIED_PIGLIN },
            "Brute", "Fishing", "Giant", "Hungry", "Knight", "Plague", "Vampire"//TODO figure out crossbows
    );//TODO crimson/warped
    
    public static final MobFamily<AbstractSkeleton, SkeletonFamilyConfig> SKELETON = new MobFamily<>( SkeletonFamilyConfig::new,
            "Skeleton", "skeletons", 0xC1C1C1, new EntityType[] { EntityType.SKELETON, EntityType.STRAY },
            "Brute", "Fire", "Gatling", "Giant", "Knight", "Ninja", "Pirate", "Poison", "Sniper", "Spitfire", "Stray", "Weathered"
    );
    public static final MobFamily<AbstractSkeleton, SkeletonFamilyConfig> WITHER_SKELETON = new MobFamily<>( SkeletonFamilyConfig::new,
            "WitherSkeleton", "wither skeletons", 0x141414, new EntityType[] { EntityType.WITHER_SKELETON },
            "Brute", "Gatling", "Giant", "Knight", "Ninja", "Sniper", "Spitfire"
    );//TODO crimson/warped
    
    public static final MobFamily<Slime, SlimeFamilyConfig> SLIME = new MobFamily<>( SlimeFamilyConfig::new,
            "Slime", "slimes", 0x51A03E, new EntityType[] { EntityType.SLIME },
            "Blackberry", "Blueberry", "Caramel", "Frozen", "Grape", "Lemon", "Potion", "Strawberry", "Watermelon"
    );
    public static final MobFamily<MagmaCube, FamilyConfig> MAGMA_CUBE = new MobFamily<>( FamilyConfig::newMoreSpecial,
            "MagmaCube", "magma cubes", 0x340000, new EntityType[] { EntityType.MAGMA_CUBE },
            "Bouncing", "Hardened", "Sticky", "Volatile"
    );
    
    public static final MobFamily<Spider, FamilyConfig> SPIDER = new MobFamily<>( FamilyConfig::newMoreSpecial,
            "Spider", "spiders", 0x342D27, new EntityType[] { EntityType.SPIDER },
            "Baby", "Desert", "Fire", "Flying", "Giant", "Hungry", "Mother", "Pale", "Poison", "Water", "Web", "Witch"
    );
    public static final MobFamily<CaveSpider, FamilyConfig> CAVE_SPIDER = new MobFamily<>( FamilyConfig::newMoreSpecial,
            "CaveSpider", "cave spiders", 0x0C424E, new EntityType[] { EntityType.CAVE_SPIDER },
            "Baby", "Desert", "Fire", "Flying", "Mother", "Pale", "Water", "Web", "Witch"
    );
    
    public static final MobFamily<Silverfish, SilverfishFamilyConfig> SILVERFISH = new MobFamily<>( SilverfishFamilyConfig::new,
            "Silverfish", "silverfish", 0x6E6E6E, new EntityType[] { EntityType.SILVERFISH },
            "Albino", "Blinding", "Desiccated", "Fire", "Fishing", "Flying", "Poison", "Puffer", "Tough"
    );
    
    public static final MobFamily<EnderMan, FamilyConfig> ENDERMAN = new MobFamily<>( FamilyConfig::new,
            "Enderman", "endermen", 0x161616, new EntityType[] { EntityType.ENDERMAN },
            "Blinding", "Flame", "Icy", "Lightning", "Mini", "Mirage", "Runic", "Thief"
    );
    
    public static final MobFamily<Witch, WitchFamilyConfig> WITCH = new MobFamily<>( WitchFamilyConfig::new,
            "Witch", "witches", 0x340000, new EntityType[] { EntityType.WITCH },
            /*"Burned",*/ "Domination", /*"Drowned",*/ "Ice", /*"Sands",*/ "Shadows", "Undead", "Wilds", "Wind"
    );//TODO burned, drowned, sands
    
    public static final MobFamily<Ghast, GhastFamilyConfig> GHAST = new MobFamily<>( GhastFamilyConfig::new,
            "Ghast", "ghasts", 0xF9F9F9, new EntityType[] { EntityType.GHAST },
            "Baby", "CorporealShift", "Fighter", "King", "Queen", "Slab", "Unholy"
    );
    
    public static final MobFamily<Blaze, FamilyConfig> BLAZE = new MobFamily<>( FamilyConfig::new,
            "Blaze", "blazes", 0xF6B201, new EntityType[] { EntityType.BLAZE },
            "Armored", "Cinder", "Conflagration", "Ember", "Hellfire", "Inferno", "Jolt", "Wildfire"
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
    
    /** True if this family has any giant species. */
    private Boolean hasAnyGiants;
    
    private MobFamily( BiFunction<ConfigManager, MobFamily<?, ?>, V> configSupplier,
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
        
        config = configSupplier.apply( ConfigManager.get( SpecialMobs.MOD_ID ), this );
        config.SPEC.initialize();
        
        // We register here because otherwise there's no way to find all families
        FAMILY_LIST.add( this );
    }
    
    /** Pick a new species from this family, based on the location. */
    public Species<? extends T> nextVariant( Level level, @Nullable BlockPos pos, @Nullable Predicate<Species<?>> selector ) {
        return nextVariant( level, pos, selector, vanillaReplacement );
    }
    
    /** Pick a new species from this family, based on the location. */
    public Species<? extends T> nextVariant( Level level, @Nullable BlockPos pos, @Nullable Predicate<Species<?>> selector, Species<? extends T> fallback ) {
        final Species<?> species = config.GENERAL.specialVariantList.next( level.random, level, pos, selector );
        //noinspection unchecked
        return species == null ? fallback : (Species<? extends T>) species;
    }
    
    /**
     * @return True if this species is NOT taller (in block height) than the base vanilla entity.
     * Used to reduce likelihood of suffocation due to Mob Replacement.
     */
    public boolean hasAnyGiants() {
        if( hasAnyGiants == null ) {
            hasAnyGiants = false;
            for( Species<?> species : variants ) {
                if( !species.isNotGiant() ) {
                    hasAnyGiants = true;
                    break;
                }
            }
        }
        return hasAnyGiants;
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
    public static class Species<T extends Mob> {
        /** Maps each species's entity type back to the species. */
        private static final Map<EntityType<?>, Species<?>> TYPE_TO_SPECIES_MAP = new HashMap<>();
        
        /** @return The entity type's species, or null if the entity type does not represent any mob species. */
        @Nullable
        public static Species<?> of( EntityType<?> entityType ) { return TYPE_TO_SPECIES_MAP.get( entityType ); }
        
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
        
        /** True if this mob is NOT taller (in block height) than the base vanilla entity. */
        private Boolean isNotGiant;
        /** The scale of this species's height in relation to the base vanilla entity's height. */
        private float heightScale = -1.0F;
        
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
            // noinspection ConstantConditions
            config = AnnotationHelper.createConfig( ConfigManager.get( SpecialMobs.MOD_ID ), this );
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
            final EntityType.EntityFactory<T> factory = AnnotationHelper.getEntityFactory( this );
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
        
        /** Registers this species's spawn placement and links the (now loaded) entity type to this species. */
        public void registerSpawnPlacement() {
            TYPE_TO_SPECIES_MAP.put( entityType.get(), this );
            AnnotationHelper.registerSpawnPlacement( this );
        }
        
        /** @return The plural name used to refer to this species in unlocalized situations; e.g. config comments. */
        public String getConfigName() {
            return (specialVariantName == null ? "vanilla replacement " :
                    ConfigUtil.camelCaseToLowerSpace( specialVariantName ) + " ") + family.configName;
        }
        
        /** @return The singular name used to refer to this species in unlocalized situations; e.g. config comments. */
        public String getConfigNameSingular() {
            return (specialVariantName == null ? "vanilla replacement " :
                    ConfigUtil.camelCaseToLowerSpace( specialVariantName ) + " ") + ConfigUtil.camelCaseToLowerSpace( family.name );
        }
        
        /**
         * @return True if this species is NOT taller (in block height) than the base vanilla entity.
         * Used to reduce likelihood of suffocation due to Mob Replacement.
         */
        public boolean isNotGiant() {
            if( isNotGiant == null ) {
                isNotGiant = Mth.ceil( entityType.get().getHeight() ) <= Mth.ceil( family.replaceableTypes[0].getHeight() );
            }
            return isNotGiant;
        }
        
        /** @return The height scale. Used to calculate eye height for families that are not auto-scaled. */
        public float getHeightScale() {
            if( heightScale < 0.0F ) {
                heightScale = entityType.get().getHeight() / family.replaceableTypes[0].getHeight();
            }
            return heightScale;
        }
    }
}