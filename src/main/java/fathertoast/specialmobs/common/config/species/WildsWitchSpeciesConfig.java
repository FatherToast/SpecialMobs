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
            
            mounts = new IntField.RandomRange( SPEC, "mounts", minMounts, maxMounts, IntField.Range.NON_NEGATIVE,
                    "The minimum and maximum (inclusive) number of times " + speciesName + " can summon a spider mount." );
            
            SPEC.newLine();
            
            swarms = new IntField.RandomRange( SPEC, "swarms", minSwarms, maxSwarms, IntField.Range.NON_NEGATIVE,
                    "The minimum and maximum (inclusive) number of times " + speciesName + " can summon a spider swarm." );
            
            SPEC.newLine();
            
            swarmSize = new IntField.RandomRange( SPEC, "swarm_size", minSwarmSize, maxSwarmSize, IntField.Range.NON_NEGATIVE,
                    "The minimum and maximum (inclusive) number of spiders " + speciesName + " spawn with each swarm.",
                    "Note that this is rolled on the summoner's spawn, not each time a swarm is summoned." );
        }
    }
}