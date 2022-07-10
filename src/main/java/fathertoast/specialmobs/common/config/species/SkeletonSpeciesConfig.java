package fathertoast.specialmobs.common.config.species;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.field.DoubleField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.util.ConfigUtil;

/**
 * This is the base species config for skeletons and wither skeletons.
 */
public class SkeletonSpeciesConfig extends SpeciesConfig {
    
    public final Skeletons SKELETONS;
    
    /** Builds the config spec that should be used for this config. */
    public SkeletonSpeciesConfig( MobFamily.Species<?> species, double bowChance, double shieldChance ) {
        super( species );
        
        SKELETONS = new Skeletons( SPEC, species, speciesName, bowChance, shieldChance );
    }
    
    public static class Skeletons extends Config.AbstractCategory {
        
        public final DoubleField bowEquipChance;
        
        public final DoubleField shieldEquipChance;
        
        Skeletons( ToastConfigSpec parent, MobFamily.Species<?> species, String speciesName, double bowChance, double shieldChance ) {
            super( parent, ConfigUtil.noSpaces( species.family.configName ),
                    "Options standard to all " + species.family.configName + "." );
            
            // Automatically set the default bow chance to 0% if the mob has ranged attacks disabled by default
            final double effectiveDefault = species.bestiaryInfo.rangedAttackMaxRange > 0.0F ? bowChance : 0.0;
            bowEquipChance = SPEC.define( new DoubleField( "bow_chance", effectiveDefault, DoubleField.Range.PERCENT,
                    "Chance for " + speciesName + " to spawn with a bow, which enables their ranged attack (if max range > 0)." ) );
            
            SPEC.newLine();
            
            shieldEquipChance = SPEC.define( new DoubleField( "shield_chance", shieldChance, DoubleField.Range.PERCENT,
                    "Chance for " + speciesName + " to spawn with a shield if they did not spawn with a bow.",
                    "Shield users have a 33% chance to block frontal attacks (100% vs. long range attacks) and can be broken by axes." ) );
        }
    }
}