package fathertoast.specialmobs.common.config.family;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.field.DoubleField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.util.ConfigUtil;

/**
 * This is the family config for creepers.
 */
public class CreeperFamilyConfig extends FamilyConfig {
    
    public final Creepers CREEPERS;
    
    /** Builds the config spec that should be used for this config. */
    public CreeperFamilyConfig( MobFamily<?, ?> family ) {
        super( family, VARIANT_CHANCE_HIGH );
        
        CREEPERS = new Creepers( SPEC, family );
    }
    
    public static class Creepers extends Config.AbstractCategory {
        
        public final DoubleField familyStormChargeChance;
        
        public final DoubleField superchargeChance;
        
        Creepers( ToastConfigSpec parent, MobFamily<?, ?> family ) {
            super( parent, ConfigUtil.noSpaces( family.configName ),
                    "Options specific to the family of " + family.configName + "." );
            
            familyStormChargeChance = SPEC.define( new DoubleField( "family_storm_charge_chance", 0.01, DoubleField.Range.PERCENT,
                    "Chance for " + family.configName + " to spawn charged during thunderstorms.",
                    "By default, this applies to all " + family.configName + "; but species configs can override it." ) );
            
            SPEC.newLine();
            
            superchargeChance = SPEC.define( new DoubleField( "supercharge_chance", 0.1, DoubleField.Range.PERCENT,
                    "Chance for " + family.configName + " to become supercharged when charged in any way." ) );
        }
    }
}