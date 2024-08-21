package fathertoast.specialmobs.common.config.family;

import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.DoubleField;
import fathertoast.specialmobs.common.bestiary.MobFamily;

/**
 * This is the family config for skeletons and wither skeletons.
 */
public class SkeletonFamilyConfig extends FamilyConfig {
    
    public final Skeletons SKELETONS;
    
    /** Builds the config spec that should be used for this config. */
    public SkeletonFamilyConfig( ConfigManager manager, MobFamily<?, ?> family ) {
        super( manager, family );
        
        SKELETONS = new Skeletons( this, family );
    }
    
    public static class Skeletons extends AbstractConfigCategory<SkeletonFamilyConfig> {
        
        public final DoubleField babyChance;
        
        Skeletons( SkeletonFamilyConfig parent, MobFamily<?, ?> family ) {
            super( parent, ConfigUtil.noSpaces( family.configName ),
                    "Options specific to the family of " + family.configName + "." );
            
            babyChance = SPEC.define( new DoubleField( "baby_chance", 0.05, DoubleField.Range.PERCENT,
                    "Chance for valid " + family.configName + " to spawn as babies. Baby mobs are about " +
                            "half-sized, move 50% faster, drop 150% more experience, and are 50% cuter." ) );
        }
    }
}