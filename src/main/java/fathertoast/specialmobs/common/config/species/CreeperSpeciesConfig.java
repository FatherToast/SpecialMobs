package fathertoast.specialmobs.common.config.species;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.field.BooleanField;
import fathertoast.specialmobs.common.config.field.DoubleField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.util.ConfigUtil;

/**
 * This is the base species config for creepers.
 */
public class CreeperSpeciesConfig extends SpeciesConfig {
    
    public final Creepers CREEPERS;
    
    /** Builds the config spec that should be used for this config. */
    public CreeperSpeciesConfig( MobFamily.Species<?> species,
                                 boolean cannotExplodeWhileWet, boolean explodeWhileBurning, boolean explodeWhenShot ) {
        super( species );
        
        CREEPERS = new Creepers( SPEC, species, speciesName, cannotExplodeWhileWet, explodeWhileBurning, explodeWhenShot );
    }
    
    public static class Creepers extends Config.AbstractCategory {
        
        public final DoubleField stormChargeChance;
        
        /** Note that this is inverted from how it is normally seen and used elsewhere. This is to avoid double-negatives in the config. */
        public final BooleanField canExplodeWhileWet;
        public final BooleanField explodesWhileBurning;
        public final BooleanField explodesWhenShot;
        
        Creepers( ToastConfigSpec parent, MobFamily.Species<?> species, String speciesName,
                  boolean cannotExplodeWhileWet, boolean explodeWhileBurning, boolean explodeWhenShot ) {
            super( parent, ConfigUtil.noSpaces( species.family.configName ),
                    "Options standard to all " + species.family.configName + "." );
            
            stormChargeChance = SPEC.define( new DoubleField( "storm_charge_chance", -1.0, DoubleField.Range.SIGNED_PERCENT,
                    "Chance for " + speciesName + " to spawn charged during thunderstorms.",
                    "If this is set to a non-negative value, it overrides the value set for \"family_storm_charge_chance\"." ) );
            
            SPEC.newLine();
            
            canExplodeWhileWet = SPEC.define( new BooleanField( "can_explode_while_wet", !cannotExplodeWhileWet,
                    "If true, " + speciesName + " can explode while wet (normal creeper behavior)." ) );
            explodesWhileBurning = SPEC.define( new BooleanField( "explodes_while_burning", explodeWhileBurning,
                    "If true, " + speciesName + " will explode while burning. If extinguished before the fuse runs",
                    "out, they will resume normal behavior." ) );
            explodesWhenShot = SPEC.define( new BooleanField( "explodes_when_shot", explodeWhenShot,
                    "If true, " + speciesName + " will explode when hit by an indirect attack (e.g. an arrow)." ) );
        }
    }
}