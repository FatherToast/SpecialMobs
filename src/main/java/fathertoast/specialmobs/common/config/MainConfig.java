package fathertoast.specialmobs.common.config;

import fathertoast.specialmobs.common.config.field.BooleanField;
import fathertoast.specialmobs.common.config.field.DoubleField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.util.ConfigUtil;
import fathertoast.specialmobs.common.config.util.EnvironmentEntry;
import fathertoast.specialmobs.common.config.util.EnvironmentList;
import fathertoast.specialmobs.common.config.util.RestartNote;
import net.minecraft.world.gen.feature.structure.Structure;

import java.io.File;

public class MainConfig extends Config.AbstractConfig {
    
    public final General GENERAL;
    public final NaturalSpawning NATURAL_SPAWNING;
    
    /** Builds the config spec that should be used for this config. */
    MainConfig( File dir, String fileName ) {
        super( dir, fileName,
                "This config contains options that apply to the mod as a whole, including some master settings",
                "toggles for convenience." );
        
        GENERAL = new General( SPEC );
        NATURAL_SPAWNING = new NaturalSpawning( SPEC );
    }
    
    public static class General extends Config.AbstractCategory {
        
        public final BooleanField enableMobReplacement;
        
        public final BooleanField masterVanillaReplacement;
        public final DoubleField masterRandomScaling;
        
        public final BooleanField enableNausea;
        public final BooleanField fancyFishingMobs;
        
        General( ToastConfigSpec parent ) {
            super( parent, "general",
                    "Options that apply to the Special Mobs mod as a whole.",
                    "Also includes several 'master toggles' for convenience." );
            
            enableMobReplacement = SPEC.define( new BooleanField( "enable_mob_replacer", true,
                    "Whether the Mob Replacer is enabled. This 'hijacks' vanilla mob spawns to use as its own.",
                    "The Mob Replacer is the traditional spawn method for Special Mobs which allows everything that spawns",
                    "valid vanilla mobs (e.g. dungeon spawners) to spawn this mod's mobs based on your configs instead." ) );
            
            SPEC.newLine();
            
            masterVanillaReplacement = SPEC.define( new BooleanField( "master_vanilla_replacement", true,
                    "Whether the mod uses Special Mobs entities in place of vanilla entities for non-special species.",
                    "This allows your config options to apply to non-special species and allows them to benefit from",
                    "Special Mob Data and any improvements made to the entity (for example, zombies can use bows & shields).",
                    "If false, vanilla replacements are disabled for all families; if true, it is determined by the family's config." ) );
            masterRandomScaling = SPEC.define( new DoubleField( "master_random_scaling", 0.07, DoubleField.Range.PERCENT,
                    "When greater than 0, mobs will have a random render scale applied. This is a visual effect only.",
                    "For example, with a value of 0.07, mob scale will vary " + ConfigUtil.PLUS_OR_MINUS + "7% of normal size.",
                    "By default, this applies to all mobs in the mod; but family and species configs can override it." ) );
            
            SPEC.newLine();
            
            enableNausea = SPEC.define( new BooleanField( "enable_nausea_effects", true,
                    "Set to false to prevent any of this mod's mobs from applying nausea (aka confusion).",
                    "Use this if the screen warping from nausea hurts your face or makes you sick." ) );
            fancyFishingMobs = SPEC.define( new BooleanField( "fancy_fishing_mobs", true,
                    "Overrides the default fishing rod item animation so that it is compatible with fishing mobs from this mod.",
                    "Set to false if it causes problems with another mod. Fishing mobs will instead render a stick while casting.",
                    "You must restart the client for changes to this setting to take effect." ) );
        }
    }
    
    public static class NaturalSpawning extends Config.AbstractCategory {
        
        public final DoubleField caveSpiderSpawnMultiplier;
        public final DoubleField.EnvironmentSensitive caveSpiderSpawnChance;
        
        NaturalSpawning( ToastConfigSpec parent ) {
            super( parent, "natural_spawning",
                    "Options to customize the additional natural monster spawning from this mod.",
                    "Most changes to options in this category require the game to be restarted to take effect." );
            
            caveSpiderSpawnMultiplier = SPEC.define( new DoubleField( "cave_spider_spawn_multiplier", 0.5, DoubleField.Range.NON_NEGATIVE,
                    "Option to add vanilla cave spiders as natural spawns. These spawns will be added to all biomes that can",
                    "spawn regular spiders. Cave spider spawn weight is the same as the spider spawn weight, multiplied by",
                    "this value. When this is set to 0, this added cave spider spawn feature is completely disabled.",
                    "Finer tuning can be done with the spawn chances below." ), RestartNote.GAME );
            caveSpiderSpawnChance = new DoubleField.EnvironmentSensitive(
                    SPEC.define( new DoubleField( "cave_spider_chance.base", 0.0, DoubleField.Range.PERCENT,
                            "The chance for added cave spider natural spawn attempts to succeed. Does not affect mob replacement." ) ),
                    SPEC.define( new EnvironmentListField( "cave_spider_chance.exceptions", new EnvironmentList(
                            EnvironmentEntry.builder( 1.0F ).belowDiamondLevel().build(),
                            EnvironmentEntry.builder( 1.0F ).inStructure( Structure.MINESHAFT ).build(),
                            EnvironmentEntry.builder( 0.33F ).belowSeaFloor().build() )
                            .setRange( DoubleField.Range.PERCENT ),
                            "The chance for added cave spider natural spawn attempts to succeed when specific environmental conditions are met." ) )
            );
            
            //SPEC.newLine();
        }
    }
}