package fathertoast.specialmobs.common.config.family;

import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.DoubleField;
import fathertoast.specialmobs.common.bestiary.MobFamily;

/**
 * This is the family config for creepers.
 */
public class CreeperFamilyConfig extends FamilyConfig {
    
    public final Creepers CREEPERS;
    
    /** Builds the config spec that should be used for this config. */
    public CreeperFamilyConfig( ConfigManager manager, MobFamily<?, ?> family ) {
        super( manager, family, VARIANT_CHANCE_HIGH );
        
        CREEPERS = new Creepers( this, family );
    }
    
    public static class Creepers extends AbstractConfigCategory<CreeperFamilyConfig> {
        
        public final DoubleField familyStormChargeChance;
        
        public final DoubleField superchargeChance;
        
        Creepers( CreeperFamilyConfig parent, MobFamily<?, ?> family ) {
            super( parent, ConfigUtil.noSpaces( family.configName ),
                    "Options specific to the family of " + family.configName + "." );
            
            familyStormChargeChance = SPEC.define( new DoubleField( "family_storm_charge_chance", 0.01, DoubleField.Range.PERCENT,
                    "Chance for " + family.configName + " to spawn charged during thunderstorms. " +
                            "By default, this applies to all " + family.configName + "; but species configs can override it." ) );
            
            SPEC.newLine();
            
            superchargeChance = SPEC.define( new DoubleField( "supercharge_chance", 0.1, DoubleField.Range.PERCENT,
                    "Chance for " + family.configName + " to become supercharged when charged in any way." ) );
        }
    }
}