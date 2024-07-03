package fathertoast.specialmobs.common.config.species;

import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.DoubleField;
import fathertoast.specialmobs.common.bestiary.MobFamily;

/**
 * This is the base species config for zombies, drowned, and zombified piglins.
 */
public class ZombieSpeciesConfig extends SpeciesConfig {
    
    public final Zombies ZOMBIES;
    
    /** Builds the config spec that should be used for this config. */
    public ZombieSpeciesConfig( ConfigManager manager, MobFamily.Species<?> species, double bowChance, double shieldChance ) {
        super( manager, species );
        
        ZOMBIES = new Zombies( this, species, species.getConfigName(), bowChance, shieldChance );
    }
    
    public static class Zombies extends AbstractConfigCategory<ZombieSpeciesConfig> {
        
        public final DoubleField bowEquipChance;
        
        public final DoubleField shieldEquipChance;
        
        Zombies( ZombieSpeciesConfig parent, MobFamily.Species<?> species, String speciesName, double bowChance, double shieldChance ) {
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