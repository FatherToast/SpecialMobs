package fathertoast.specialmobs.common.config.species;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.field.DoubleField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.util.ConfigUtil;
import fathertoast.specialmobs.common.config.util.EnvironmentEntry;
import fathertoast.specialmobs.common.config.util.EnvironmentList;

public class HuskZombieSpeciesConfig extends ZombieSpeciesConfig {
    
    public final Husk HUSK;
    
    /** Builds the config spec that should be used for this config. */
    public HuskZombieSpeciesConfig( MobFamily.Species<?> species, double bowChance, double shieldChance ) {
        super( species, bowChance, shieldChance );
        
        HUSK = new Husk( SPEC, species, speciesName );
    }
    
    public static class Husk extends Config.AbstractCategory {
        
        public final DoubleField.EnvironmentSensitive convertVariantChance;
        
        Husk( ToastConfigSpec parent, MobFamily.Species<?> species, String speciesName ) {
            super( parent, ConfigUtil.camelCaseToLowerUnderscore( species.specialVariantName ),
                    "Options specific to " + speciesName + "." );
            
            convertVariantChance = new DoubleField.EnvironmentSensitive(
                    SPEC.define( new DoubleField( "special_variant_chance.base", 0.33, DoubleField.Range.PERCENT,
                            "The chance for " + speciesName + " to convert to a special zombie variant when drowned." ) ),
                    SPEC.define( new EnvironmentListField( "special_variant_chance.exceptions", new EnvironmentList(
                            EnvironmentEntry.builder( 0.66F ).atMaxMoonLight().build() ).setRange( DoubleField.Range.PERCENT ),
                            "The chance for " + speciesName + " to convert to a special zombie variant when drowned while specific environmental conditions are met." ) )
            );
        }
    }
}