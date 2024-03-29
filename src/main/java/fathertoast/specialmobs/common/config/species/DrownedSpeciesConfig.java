package fathertoast.specialmobs.common.config.species;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.field.DoubleField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.util.ConfigUtil;

/**
 * This is the base species config for zombies, drowned, and zombified piglins.
 */
public class DrownedSpeciesConfig extends SpeciesConfig {
    
    public final Drowned DROWNED;
    
    /** Builds the config spec that should be used for this config. */
    public DrownedSpeciesConfig( MobFamily.Species<?> species, double tridentChance, double shieldChance ) {
        super( species );
        
        DROWNED = new Drowned( SPEC, species, species.getConfigName(), tridentChance, shieldChance );
    }
    
    public static class Drowned extends Config.AbstractCategory {
        
        public final DoubleField tridentEquipChance;
        
        public final DoubleField shieldEquipChance;
        
        Drowned( ToastConfigSpec parent, MobFamily.Species<?> species, String speciesName, double tridentChance, double shieldChance ) {
            super( parent, ConfigUtil.noSpaces( species.family.configName ),
                    "Options standard to all " + species.family.configName + "." );
            
            // Automatically set the default trident chance to 0% if the mob has ranged attacks disabled by default
            final double effectiveDefault = species.bestiaryInfo.rangedAttackMaxRange > 0.0F ? tridentChance : 0.0;
            tridentEquipChance = SPEC.define( new DoubleField( "trident_chance", effectiveDefault, DoubleField.Range.PERCENT,
                    "Chance for " + speciesName + " to spawn with a trident, which enables their ranged attack (if max range > 0)." ) );
            
            SPEC.newLine();
            
            shieldEquipChance = SPEC.define( new DoubleField( "shield_chance", shieldChance, DoubleField.Range.PERCENT,
                    "Chance for " + speciesName + " to spawn with a shield.",
                    "Shield users have a 33% chance to block frontal attacks (100% vs. long range attacks) and can be broken by axes." ) );
        }
    }
}