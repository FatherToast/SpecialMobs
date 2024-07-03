package fathertoast.specialmobs.common.config.species;

import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.IntField;
import fathertoast.specialmobs.common.bestiary.MobFamily;

/**
 * This is the base species config for blazes.
 */
public class BlazeSpeciesConfig extends SpeciesConfig {
    
    public final Blazes BLAZES;
    
    /** Builds the config spec that should be used for this config. */
    public BlazeSpeciesConfig( ConfigManager manager, MobFamily.Species<?> species, int fireballBurstCount, int fireballBurstDelay ) {
        super( manager, species );
        
        BLAZES = new Blazes( this, species, species.getConfigName(), fireballBurstCount, fireballBurstDelay );
    }
    
    public static class Blazes extends AbstractConfigCategory<BlazeSpeciesConfig> {
        
        public final IntField fireballBurstCount;
        public final IntField fireballBurstDelay;
        
        Blazes( BlazeSpeciesConfig parent, MobFamily.Species<?> species, String speciesName, int burstCount, int burstDelay ) {
            super( parent, ConfigUtil.noSpaces( species.family.configName ),
                    "Options standard to all " + species.family.configName + "." );
            
            fireballBurstCount = SPEC.define( new IntField( "fireball_attack.burst_count", burstCount, IntField.Range.NON_NEGATIVE,
                    "The number of fireballs " + speciesName + " launch with each burst." ) );
            fireballBurstDelay = SPEC.define( new IntField( "fireball_attack.burst_delay", burstDelay, IntField.Range.NON_NEGATIVE,
                    "The time (in ticks) " + speciesName + " wait between each fireball in a burst. (20 ticks = 1 second)" ) );
        }
    }
}