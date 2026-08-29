package fathertoast.specialmobs.common.config.species;

import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.collection.RegistryWeightedListField;
import fathertoast.crust.api.config.common.value.collection.RegistryWeightedList;
import fathertoast.crust.api.lib.CrustObjects;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.registries.ForgeRegistries;

public class PotionSlimeSpeciesConfig extends SpeciesConfig {
    
    public final Potion POTION;
    
    /** Builds the config spec that should be used for this config. */
    public PotionSlimeSpeciesConfig( ConfigManager manager, MobFamily.Species<?> species ) {
        super( manager, species );
        
        POTION = new Potion( this, species, species.getConfigName() );
    }
    
    public static class Potion extends AbstractConfigCategory<PotionSlimeSpeciesConfig> {
        
        public final RegistryWeightedListField<MobEffect> potionChoices;
        
        Potion( PotionSlimeSpeciesConfig parent, MobFamily.Species<?> species, String speciesName ) {
            super( parent, ConfigUtil.camelCaseToLowerUnderscore( species.specialVariantName ),
                    "Options specific to " + speciesName + "." );
            
            potionChoices = SPEC.define( new RegistryWeightedListField<>( "potion_choices",
                    new RegistryWeightedList.Builder<>( ForgeRegistries.MOB_EFFECTS )
                            .add( 5, MobEffects.MOVEMENT_SPEED ).add( 10, MobEffects.MOVEMENT_SLOWDOWN )
                            .add( 5, MobEffects.DIG_SPEED ).add( 10, MobEffects.DIG_SLOWDOWN )
                            .add( 5, MobEffects.DAMAGE_BOOST ).add( 10, MobEffects.WEAKNESS )
                            .add( 5, MobEffects.HEAL ).add( 10, MobEffects.HARM ).add( 10, MobEffects.HUNGER )
                            .add( 5, MobEffects.REGENERATION ).add( 10, MobEffects.POISON ).add( 10, MobEffects.WITHER )
                            .add( 5, MobEffects.JUMP ).add( 10, MobEffects.LEVITATION )
                            .add( 5, MobEffects.SLOW_FALLING ).add( 10, CrustObjects.Effects.WEIGHT )
                            .add( 5, MobEffects.DAMAGE_RESISTANCE ).add( 10, CrustObjects.Effects.VULNERABILITY )
                            .add( 5, MobEffects.FIRE_RESISTANCE ).add( 5, MobEffects.WATER_BREATHING )
                            .add( 10, MobEffects.BLINDNESS ).add( 5, MobEffects.NIGHT_VISION ).add( 10, MobEffects.CONFUSION )
                            .add( 5, MobEffects.HEALTH_BOOST ).add( 5, MobEffects.ABSORPTION )
                            .build(),
                    "List of potions that " + speciesName + " can be 'filled' with on spawn (they will apply it on hit). " +
                            "Each effect in the list has an equal chance to be selected." ) );
        }
    }
}