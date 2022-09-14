package fathertoast.specialmobs.common.config.family;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.field.DoubleField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.util.ConfigUtil;

/**
 * This is the family config for skeletons and wither skeletons.
 */
public class SkeletonFamilyConfig extends FamilyConfig {
    
    public final Skeletons SKELETONS;
    
    /** Builds the config spec that should be used for this config. */
    public SkeletonFamilyConfig( MobFamily<?, ?> family ) {
        super( family );
        
        SKELETONS = new Skeletons( SPEC, family );
    }
    
    public static class Skeletons extends Config.AbstractCategory {
        
        public final DoubleField babyChance;
        
        Skeletons( ToastConfigSpec parent, MobFamily<?, ?> family ) {
            super( parent, ConfigUtil.noSpaces( family.configName ),
                    "Options specific to the family of " + family.configName + "." );
            
            babyChance = SPEC.define( new DoubleField( "baby_chance", 0.05, DoubleField.Range.PERCENT,
                    "Chance for valid " + family.configName + " to spawn as babies. Baby mobs are about half-sized,",
                    "move 50% faster, drop 150% more experience, and are 50% cuter." ) );
        }
    }
}