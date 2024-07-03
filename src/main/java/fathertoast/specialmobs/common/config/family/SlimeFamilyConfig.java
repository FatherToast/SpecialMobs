package fathertoast.specialmobs.common.config.family;

import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.BooleanField;
import fathertoast.specialmobs.common.bestiary.MobFamily;

/**
 * This is the family config for slimes (NOT magma cubes).
 */
public class SlimeFamilyConfig extends FamilyConfig {
    
    public final Slimes SLIMES;
    
    /** Builds the config spec that should be used for this config. */
    public SlimeFamilyConfig( ConfigManager manager, MobFamily<?, ?> family ) {
        super( manager, family, VARIANT_CHANCE_HIGH );
        
        SLIMES = new Slimes( this, family );
    }
    
    public static class Slimes extends AbstractConfigCategory<SlimeFamilyConfig> {
        
        public final BooleanField tinySlimesDealDamage;
        
        Slimes( SlimeFamilyConfig parent, MobFamily<?, ?> family ) {
            super( parent, ConfigUtil.noSpaces( family.configName ),
                    "Options specific to the family of " + family.configName + "." );
            
            tinySlimesDealDamage = SPEC.define( new BooleanField( "tiny_slimes_deal_damage", true,
                    "If true, the smallest size " + family.configName + " will be allowed to deal damage (unlike vanilla)." ) );
        }
    }
}