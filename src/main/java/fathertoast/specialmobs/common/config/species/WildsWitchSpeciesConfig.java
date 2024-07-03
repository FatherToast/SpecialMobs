package fathertoast.specialmobs.common.config.species;

import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.IntField;
import fathertoast.specialmobs.common.bestiary.MobFamily;


public class WildsWitchSpeciesConfig extends SpeciesConfig {
    
    public final Wilds WILDS;
    
    /** Builds the config spec that should be used for this config. */
    public WildsWitchSpeciesConfig( ConfigManager manager, MobFamily.Species<?> species, int minMounts, int maxMounts,
                                   int minSwarms, int maxSwarms, int minSwarmSize, int maxSwarmSize ) {
        super( manager, species );
        
        WILDS = new Wilds( this, species, species.getConfigName(), minMounts, maxMounts, minSwarms, maxSwarms, minSwarmSize, maxSwarmSize );
    }
    
    public static class Wilds extends AbstractConfigCategory<WildsWitchSpeciesConfig> {
        
        public final IntField.RandomRange mounts;
        
        public final IntField.RandomRange swarms;
        
        public final IntField.RandomRange swarmSize;
        
        Wilds( WildsWitchSpeciesConfig parent, MobFamily.Species<?> species, String speciesName,
               int minMounts, int maxMounts, int minSwarms, int maxSwarms, int minSwarmSize, int maxSwarmSize ) {
            super( parent, ConfigUtil.camelCaseToLowerUnderscore( species.specialVariantName ),
                    "Options specific to " + speciesName + "." );
            
            mounts = new IntField.RandomRange(
                    SPEC.define( new IntField( "mounts.min", minMounts, IntField.Range.NON_NEGATIVE,
                            "The minimum and maximum (inclusive) number of times " + speciesName + " can summon a spider mount." ) ),
                    SPEC.define( new IntField( "mounts.max", maxMounts, IntField.Range.NON_NEGATIVE ) )
            );
            
            SPEC.newLine();
            
            swarms = new IntField.RandomRange(
                    SPEC.define( new IntField( "swarms.min", minSwarms, IntField.Range.NON_NEGATIVE,
                            "The minimum and maximum (inclusive) number of times " + speciesName + " can summon a spider swarm." ) ),
                    SPEC.define( new IntField( "swarms.max", maxSwarms, IntField.Range.NON_NEGATIVE ) )
            );
            
            SPEC.newLine();
            
            swarmSize = new IntField.RandomRange(
                    SPEC.define( new IntField( "swarm_size.min", minSwarmSize, IntField.Range.NON_NEGATIVE,
                            "The minimum and maximum (inclusive) number of spiders " + speciesName + " spawn with each swarm.",
                            "Note that this is rolled on the summoner's spawn, not each time a swarm is summoned." ) ),
                    SPEC.define( new IntField( "swarm_size.max", maxSwarmSize, IntField.Range.NON_NEGATIVE ) )
            );
        }
    }
}