package fathertoast.specialmobs.common.config.family;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.field.BooleanField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.util.ConfigUtil;

/**
 * This is the family config for slimes (NOT magma cubes).
 */
public class SlimeFamilyConfig extends FamilyConfig {
    
    public final Slimes SLIMES;
    
    /** Builds the config spec that should be used for this config. */
    public SlimeFamilyConfig( MobFamily<?, ?> family ) {
        super( family );
        
        SLIMES = new Slimes( SPEC, family );
    }
    
    public static class Slimes extends Config.AbstractCategory {
        
        public final BooleanField tinySlimesDealDamage;
        
        Slimes( ToastConfigSpec parent, MobFamily<?, ?> family ) {
            super( parent, ConfigUtil.noSpaces( family.configName ),
                    "Options specific to the family of " + family.configName + "." );
            
            tinySlimesDealDamage = SPEC.define( new BooleanField( "tiny_slimes_deal_damage", true,
                    "If true, the smallest size " + family.configName + " will be allowed to deal damage (unlike vanilla)." ) );
        }
    }
}