package fathertoast.specialmobs.common.config.species;

import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.DoubleField;
import fathertoast.crust.api.config.common.field.IntField;
import fathertoast.specialmobs.common.bestiary.MobFamily;


public class DrowningCreeperSpeciesConfig extends CreeperSpeciesConfig {
    
    public final Drowning DROWNING;
    
    /** Builds the config spec that should be used for this config. */
    public DrowningCreeperSpeciesConfig( ConfigManager manager, MobFamily.Species<?> species,
                                        boolean cannotExplodeWhileWet, boolean explodeWhileBurning, boolean explodeWhenShot,
                                        double infestedChance, int minPuffPuffs, int maxPuffPuffs ) {
        super( manager, species, cannotExplodeWhileWet, explodeWhileBurning, explodeWhenShot );
        
        DROWNING = new Drowning( this, species, species.getConfigName(), infestedChance, minPuffPuffs, maxPuffPuffs );
    }
    
    public static class Drowning extends AbstractConfigCategory<DrowningCreeperSpeciesConfig> {
        
        public final DoubleField infestedBlockChance;
        
        public final IntField.RandomRange puffPuffs;

        Drowning( DrowningCreeperSpeciesConfig parent, MobFamily.Species<?> species, String speciesName,
                  double infestedChance, int minPuffPuffs, int maxPuffPuffs ) {
            super( parent, ConfigUtil.camelCaseToLowerUnderscore( species.specialVariantName ),
                    "Options specific to " + speciesName + "." );
            
            infestedBlockChance = SPEC.define( new DoubleField( "infested_chance", infestedChance, DoubleField.Range.PERCENT,
                    "Chance for explosion's coral shell blocks to be infested with aquatic silverfish.",
                    "Rolled for each coral block generated." ) );
            
            SPEC.newLine();
            
            puffPuffs = new IntField.RandomRange(
                    SPEC.define( new IntField( "pufferfish.min", minPuffPuffs, IntField.Range.NON_NEGATIVE,
                            "The minimum and maximum (inclusive) limit on the number of pufferfish that " + speciesName + " spawn with their explosion." ) ),
                    SPEC.define( new IntField( "pufferfish.max", maxPuffPuffs, IntField.Range.NON_NEGATIVE ) )
            );
        }
    }
}