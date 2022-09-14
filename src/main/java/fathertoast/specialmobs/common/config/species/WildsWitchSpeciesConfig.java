package fathertoast.specialmobs.common.config.species;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.field.IntField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.util.ConfigUtil;

public class WildsWitchSpeciesConfig extends SpeciesConfig {
    
    public final Wilds WILDS;
    
    /** Builds the config spec that should be used for this config. */
    public WildsWitchSpeciesConfig( MobFamily.Species<?> species, int minMounts, int maxMounts,
                                    int minSwarms, int maxSwarms, int minSwarmSize, int maxSwarmSize ) {
        super( species );
        
        WILDS = new Wilds( SPEC, species, species.getConfigName(), minMounts, maxMounts, minSwarms, maxSwarms, minSwarmSize, maxSwarmSize );
    }
    
    public static class Wilds extends Config.AbstractCategory {
        
        public final IntField.RandomRange mounts;
        
        public final IntField.RandomRange swarms;
        
        public final IntField.RandomRange swarmSize;
        
        Wilds( ToastConfigSpec parent, MobFamily.Species<?> species, String speciesName,
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