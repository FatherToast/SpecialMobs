package fathertoast.specialmobs.common.config.species;

import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.DoubleField;
import fathertoast.crust.api.config.common.field.EnvironmentListField;
import fathertoast.crust.api.config.common.value.EnvironmentEntry;
import fathertoast.crust.api.config.common.value.EnvironmentList;
import fathertoast.specialmobs.common.bestiary.MobFamily;


public class HuskZombieSpeciesConfig extends ZombieSpeciesConfig {
    
    public final Husk HUSK;
    
    /** Builds the config spec that should be used for this config. */
    public HuskZombieSpeciesConfig( ConfigManager manager, MobFamily.Species<?> species, double bowChance, double shieldChance ) {
        super( manager, species, bowChance, shieldChance );
        
        HUSK = new Husk( this, species, species.getConfigName() );
    }
    
    public static class Husk extends AbstractConfigCategory<HuskZombieSpeciesConfig> {
        
        public final DoubleField.EnvironmentSensitive convertVariantChance;
        
        Husk( HuskZombieSpeciesConfig parent, MobFamily.Species<?> species, String speciesName ) {
            super( parent, ConfigUtil.camelCaseToLowerUnderscore( species.specialVariantName ),
                    "Options specific to " + speciesName + "." );
            
            convertVariantChance = new DoubleField.EnvironmentSensitive(
                    SPEC.define( new DoubleField( "special_variant_chance.base", 0.33, DoubleField.Range.PERCENT,
                            "The chance for " + speciesName + " to convert to a special zombie variant when drowned." ) ),
                    SPEC.define( new EnvironmentListField( "special_variant_chance.exceptions", new EnvironmentList(
                            EnvironmentEntry.builder( SPEC, 0.66F ).atMaxMoonLight().build() ).setRange( DoubleField.Range.PERCENT ),
                            "The chance for " + speciesName + " to convert to a special zombie variant when drowned while specific environmental conditions are met." ) )
            );
        }
    }
}