package fathertoast.specialmobs.common.config;

import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.AbstractConfigFile;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.*;
import fathertoast.crust.api.config.common.file.TomlHelper;
import fathertoast.crust.api.config.common.value.collection.value.DoubleValueCodec;
import fathertoast.crust.api.config.common.value.environment.EnvironmentList;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;

import java.util.List;

public class MainConfig extends AbstractConfigFile {
    
    public final General GENERAL;
    public final AddedSpawns ADDED_SPAWNS;
    
    /** Builds the config spec that should be used for this config. */
    MainConfig( ConfigManager manager, String fileName ) {
        super( manager, fileName, false,
                "This config contains options that apply to the mod as a whole, including some master " +
                        "settings toggles for convenience." );
        
        SPEC.fileOnlyNewLine();
        EnvironmentListField.describe1of2( SPEC );
        SPEC.fileOnlyNewLine();
        
        GENERAL = new General( this );
        ADDED_SPAWNS = new AddedSpawns( this );
        
        SPEC.fileOnlyNewLine();
        EnvironmentListField.describe2of2( SPEC );
        SPEC.fileOnlyNewLine();
    }
    
    public static class General extends AbstractConfigCategory<MainConfig> {
        
        public final BooleanField enableMobReplacement;
        public final StringListField skippedSpawnTypes;
        
        public final BooleanField useNaturalSpawner;
        public final IntField naturalSpawnFactor;
        
        public final BooleanField masterVanillaReplacement;
        public final DoubleField masterRandomScaling;
        
        public final BooleanField enableNausea;
        public final BooleanField fancyFishingMobs;
        
        public final BooleanField patchAdvancements;
        
        General( MainConfig parent ) {
            super( parent, "general",
                    "Options that apply to the Special Mobs mod as a whole. Also includes several " +
                            "'master toggles' for convenience." );
            
            enableMobReplacement = SPEC.define( new BooleanField( "enable_mob_replacer", true,
                    "Whether the Mob Replacer is enabled. This 'hijacks' vanilla mob spawns to use as its own.",
                    "The Mob Replacer is the traditional spawn method for Special Mobs which allows everything that spawns " +
                            "valid vanilla mobs (e.g. dungeon spawners) to spawn this mod's mobs based on your configs instead." ) );
            skippedSpawnTypes = SPEC.define( new StringListField( "skipped_spawn_types", "MobSpawnType",
                    makeDefaultSkippedSpawnTypes(), SpawnType::isValid,
                    "A list of mob spawn types that the mob replacer should not process.",
                    "For example, listing \"spawner\" here will stop the mob replacer from processing mobs spawned from spawners.",
                    "Valid types: " + TomlHelper.toLiteralList( (Object[]) SpawnType.values() ) ) );
            
            SPEC.newLine();
            
            useNaturalSpawner = SPEC.define( new BooleanField( "use_natural_spawner", false,
                    "Whether natural spawning is enabled. This is an alternative to using the Mob Replacer above " +
                            "for natural spawns and forces the Mob Replacer to skip all \"natural\" spawns when enabled.",
                    "This overhauls all spawns in all biomes that have replaceable mobs and may or may not have accurate " +
                            "results. This tries to follow your configured base weights and ignores weight exceptions; " +
                            "finer tuning can still be done based on environment/biome through each species' natural " +
                            "spawn chance setting." ), RestartNote.WORLD );
            naturalSpawnFactor = SPEC.define( new IntField( "natural_spawn_factor", 200, IntField.Range.POSITIVE,
                    "If natural spawning is enabled, this is a factor applied to all spawn weights in all " +
                            "biomes that have replaceable mobs. Higher values allow more precision in spawn chances, but " +
                            "if this is too large and you have too many spawn entries, the game's spawn logic will break." ), RestartNote.WORLD );
            
            SPEC.newLine();
            
            masterVanillaReplacement = SPEC.define( new BooleanField( "master_vanilla_replacement", true,
                    "Whether the mod uses Special Mobs entities in place of vanilla entities for non-special species. " +
                            "This allows your config options to apply to non-special species and allows them to benefit from " +
                            "Special Mob Data and any improvements made to the entity (for example, zombies can use bows & shields).",
                    "If false, vanilla replacements are disabled for all families; if true, it is determined by the family's config." ) );
            masterRandomScaling = SPEC.define( new DoubleField( "master_random_scaling", 0.07, DoubleField.Range.PERCENT,
                    "When greater than 0, mobs will have a random render scale applied. This is a visual effect only. " +
                            "For example, with a value of 0.07, mob scale will vary " + ConfigUtil.PLUS_OR_MINUS + "7% of normal size.",
                    "By default, this applies to all mobs in the mod; but family and species configs can override it." ) );
            
            SPEC.newLine();
            
            enableNausea = SPEC.define( new BooleanField( "enable_nausea_effects", true,
                    "Set to false to prevent any of this mod's mobs from applying nausea (aka confusion). " +
                            "Use this if the screen warping from nausea hurts your face or makes you sick." ) );
            fancyFishingMobs = SPEC.define( new BooleanField( "fancy_fishing_mobs", true,
                    "Overrides the default fishing rod item animation so that it is compatible with fishing mobs " +
                            "from this mod. Set to false if it causes problems with another mod. Fishing mobs will instead " +
                            "render a stick while casting." ), RestartNote.GAME );
            
            SPEC.newLine();
            
            patchAdvancements = SPEC.define( new BooleanField( "patch_advancements", true,
                    "If vanilla replacements are enabled, some vanilla advancements usually either become unobtainable or just very hard to get.",
                    "If this is enabled, Special Mobs will tweak said advancements' criteria during server startup so it becomes possible to obtain them." ), RestartNote.WORLD );
        }
        
        private List<String> makeDefaultSkippedSpawnTypes() {
            return List.of(
                    SpawnType.CHUNK_GENERATION.toString(), SpawnType.STRUCTURE.toString(), SpawnType.BUCKET.toString()
            );
        }
    }
    
    public static class AddedSpawns extends AbstractConfigCategory<MainConfig> {
        
        public final BooleanField enableAddedSpawns;
        
        public final DoubleField caveSpiderSpawnMultiplier;
        public final DoubleField.EnvironmentSensitive caveSpiderSpawnChance;
        
        public final DoubleField enderCreeperSpawnMultiplier;
        
        public final IntField drowningCreeperOceanWeight;
        public final IntField drowningCreeperRiverWeight;
        
        public final IntField pirateSkeletonOceanWeight;
        public final IntField pirateSkeletonRiverWeight;
        
        public final IntField blueberrySlimeOceanWeight;
        public final IntField blueberrySlimeRiverWeight;
        
        public final IntField witherSkeletonNetherWeight;
        public final IntField witherSkeletonSoulSandValleyWeight;
        
        public final IntField blazeNetherWeight;
        public final IntField blazeBasaltDeltasWeight;
        
        public final IntField fireCreeperNetherWeight;
        public final IntField fireZombieNetherWeight;
        public final IntField fireSpiderNetherWeight;
        
        public final IntField doomCreeperSoulSandValleyWeight;
        public final IntField skeletonCreeperSoulSandValleyWeight;
        public final IntField undeadWitchSoulSandValleyWeight;
        
        AddedSpawns( MainConfig parent ) {
            super( parent, "added_spawns",
                    "Options to customize the additional natural monster spawning from this mod. " +
                            "Most changes to options in this category require the world/server to be restarted to take effect.",
                    "Note: The primary spawn method for Special Mobs is the 'mob replacer', not natural spawns. These " +
                            "added natural spawns either introduce vanilla mobs to locations they don't normally spawn " +
                            "(which will then get mob-replaced) or add specific Special Mobs species to places that " +
                            "wouldn't make sense for their vanilla mobs to spawn (e.g., aquatic variants of ground mobs)." );
            
            enableAddedSpawns = SPEC.define( new BooleanField( "enable_added_spawns", true,
                    "Whether this category is enabled." ) );
            
            SPEC.increaseIndent();
            SPEC.subcategory( "general_spawns",
                    "Added natural spawns derived from existing spawns (in all biomes)." );
            
            caveSpiderSpawnMultiplier = SPEC.define( new DoubleField( "cave_spider_spawn_multiplier", 0.5, DoubleField.Range.NON_NEGATIVE,
                    "Option to add vanilla cave spiders as natural spawns. These spawns will be added to all biomes " +
                            "that can spawn regular spiders. Cave spider spawn weight is the same as the spider spawn weight, " +
                            "multiplied by this value. When set to 0, the added cave spider spawn feature is completely disabled. " +
                            "Finer tuning can be done with the spawn chances below." ), RestartNote.WORLD );
            caveSpiderSpawnChance = new DoubleField.EnvironmentSensitive(
                    SPEC.define( new DoubleField( "cave_spider_chance.base", 0.0, DoubleField.Range.PERCENT,
                            "The chance for added cave spider natural spawn attempts to succeed. Does not affect Mob Replacement." ) ),
                    SPEC.define( new EnvironmentListField<>( "cave_spider_chance.exceptions",
                            EnvironmentList.builder( DoubleValueCodec.PERCENT )
                                    .entryBuilder( 1.0 ).inStructure( BuiltinStructures.MINESHAFT ).build()
                                    .entryBuilder( 0.33 ).belowSeaFloor().build()
                                    .build(),
                            "The chance for added cave spider natural spawn attempts to succeed when specific environmental conditions are met." ) )
            );
            
            SPEC.newLine();
            
            enderCreeperSpawnMultiplier = SPEC.define( new DoubleField( "ender_creeper_spawn_multiplier", 0.1, DoubleField.Range.NON_NEGATIVE,
                    "Option to add ender creepers as natural spawns. These spawns will be added to all biomes " +
                            "that can spawn endermen. Ender creeper spawn weight is the same as the enderman weight, " +
                            "multiplied by this value. When set to 0, the added ender creeper spawn feature is completely disabled. " +
                            "Finer tuning can be done with the natural spawn chances in the species config file." ), RestartNote.WORLD );
            
            SPEC.subcategory( "water_spawns",
                    "Added natural ocean/river spawns for aquatic variants of land-based mobs." );
            
            drowningCreeperOceanWeight = SPEC.define( new IntField( "drowning_creeper_weight.ocean", 1, IntField.Range.NON_NEGATIVE,
                    "Option to add drowning creepers as natural spawns to oceans.",
                    "When set to 0, this added spawn feature is completely disabled.",
                    "Finer tuning can be done with the natural spawn chances in the species config file." ), RestartNote.WORLD );
            drowningCreeperRiverWeight = SPEC.define( new IntField( "drowning_creeper_weight.river", 1, IntField.Range.NON_NEGATIVE,
                    "Option to add drowning creepers as natural spawns to rivers. When set to 0, this added " +
                            "spawn feature is completely disabled. Finer tuning can be done with the natural spawn chances " +
                            "in the species config file." ), RestartNote.WORLD );
            
            SPEC.newLine();
            
            pirateSkeletonOceanWeight = SPEC.define( new IntField( "pirate_skeleton_weight.ocean", 1, IntField.Range.NON_NEGATIVE,
                    "Option to add pirate skeletons as natural spawns to oceans.",
                    "When set to 0, this added spawn feature is completely disabled.",
                    "Finer tuning can be done with the natural spawn chances in the species config file." ), RestartNote.WORLD );
            pirateSkeletonRiverWeight = SPEC.define( new IntField( "pirate_skeleton_weight.river", 1, IntField.Range.NON_NEGATIVE,
                    "Option to add pirate skeletons as natural spawns to rivers. When set to 0, this added " +
                            "spawn feature is completely disabled. Finer tuning can be done with the natural spawn chances " +
                            "in the species config file." ), RestartNote.WORLD );
            
            SPEC.newLine();
            
            blueberrySlimeOceanWeight = SPEC.define( new IntField( "blueberry_slime_weight.ocean", 2, IntField.Range.NON_NEGATIVE,
                    "Option to add blueberry slimes as natural spawns to oceans. When set to 0, this added " +
                            "spawn feature is completely disabled. Finer tuning can be done with the natural spawn chances " +
                            "in the species config file." ), RestartNote.WORLD );
            blueberrySlimeRiverWeight = SPEC.define( new IntField( "blueberry_slime_weight.river", 1, IntField.Range.NON_NEGATIVE,
                    "Option to add blueberry slimes as natural spawns to rivers. When set to 0, this added " +
                            "spawn feature is completely disabled. Finer tuning can be done with the natural spawn chances " +
                            "in the species config file." ), RestartNote.WORLD );
            
            SPEC.subcategory( "nether_spawns",
                    "Added natural Nether spawns for rare Nether mobs and fire/Nether-themed variants of overworld mobs." );
            
            witherSkeletonNetherWeight = SPEC.define( new IntField( "wither_skeleton_weight.nether", 2, IntField.Range.NON_NEGATIVE,
                    "Option to add vanilla wither skeletons as natural spawns to the Nether (except for soul sand valley " +
                            "and warped forest biomes). When set to 0, this added spawn feature is completely disabled." ), RestartNote.WORLD );
            witherSkeletonSoulSandValleyWeight = SPEC.define( new IntField( "wither_skeleton_weight.soul_sand_valley", 5, IntField.Range.NON_NEGATIVE,
                    "Option to add vanilla wither skeletons as natural spawns to the soul sand valley biome. " +
                            "When set to 0, this added spawn feature is completely disabled." ), RestartNote.WORLD );
            
            SPEC.newLine();
            
            blazeNetherWeight = SPEC.define( new IntField( "blaze_weight.nether", 1, IntField.Range.NON_NEGATIVE,
                    "Option to add vanilla blazes as natural spawns to the Nether (except for soul sand valley, " +
                            "warped forest, and basalt deltas biomes). When set to 0, this added spawn feature is completely disabled." ), RestartNote.WORLD );
            blazeBasaltDeltasWeight = SPEC.define( new IntField( "blaze_weight.basalt_deltas", 20, IntField.Range.NON_NEGATIVE,
                    "Option to add vanilla blazes as natural spawns to the basalt deltas biome. When set to 0, " +
                            "this added spawn feature is completely disabled." ), RestartNote.WORLD );
            
            SPEC.newLine();
            
            fireCreeperNetherWeight = SPEC.define( new IntField( "fire_creeper_weight.nether", 1, IntField.Range.NON_NEGATIVE,
                    "Option to add fire creepers, zombies, and spiders as natural spawns to the Nether (except for " +
                            "soul sand valley and warped forest biomes). When set to 0, the added spawn feature is completely disabled. " +
                            "Finer tuning can be done with the natural spawn chances in the species config files." ), RestartNote.WORLD );
            fireZombieNetherWeight = SPEC.define( new IntField( "fire_zombie_weight.nether", 1, IntField.Range.NON_NEGATIVE,
                    (String[]) null ) );
            fireSpiderNetherWeight = SPEC.define( new IntField( "fire_spider_weight.nether", 1, IntField.Range.NON_NEGATIVE,
                    (String[]) null ) );
            
            SPEC.newLine();
            
            doomCreeperSoulSandValleyWeight = SPEC.define( new IntField( "doom_creeper.soul_sand_valley", 1, IntField.Range.NON_NEGATIVE,
                    "Option to add doom creepers, skeleton creepers, and liches (undead witches) as natural spawns to the " +
                            "soul sand valley biome. When set to 0, the added spawn feature is completely disabled. " +
                            "Finer tuning can be done with the natural spawn chances in the species config files." ), RestartNote.WORLD );
            skeletonCreeperSoulSandValleyWeight = SPEC.define( new IntField( "skeleton_creeper.soul_sand_valley", 1, IntField.Range.NON_NEGATIVE,
                    (String[]) null ) );
            undeadWitchSoulSandValleyWeight = SPEC.define( new IntField( "lich.soul_sand_valley", 1, IntField.Range.NON_NEGATIVE,
                    (String[]) null ) );
            
            SPEC.decreaseIndent();
        }
    }
}