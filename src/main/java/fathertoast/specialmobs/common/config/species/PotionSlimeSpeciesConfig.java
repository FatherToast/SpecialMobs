package fathertoast.specialmobs.common.config.species;

import fathertoast.crust.api.ICrustApi;
import fathertoast.crust.api.config.common.AbstractConfigCategory;
import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.crust.api.config.common.ConfigUtil;
import fathertoast.crust.api.config.common.field.LazyRegistryEntryListField;
import fathertoast.crust.api.config.common.field.RegistryEntryListField;
import fathertoast.crust.api.config.common.value.LazyRegistryEntryList;
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
        
        public final RegistryEntryListField<MobEffect> allowedPotions;
        
        Potion( PotionSlimeSpeciesConfig parent, MobFamily.Species<?> species, String speciesName ) {
            super( parent, ConfigUtil.camelCaseToLowerUnderscore( species.specialVariantName ),
                    "Options specific to " + speciesName + "." );
            
            allowedPotions = SPEC.define( new LazyRegistryEntryListField<>( "allowed_effects",
                    new LazyRegistryEntryList<>( ForgeRegistries.MOB_EFFECTS, false,
                            MobEffects.MOVEMENT_SPEED, MobEffects.MOVEMENT_SLOWDOWN,
                            MobEffects.DIG_SPEED, MobEffects.DIG_SLOWDOWN,
                            MobEffects.DAMAGE_BOOST, MobEffects.WEAKNESS,
                            MobEffects.HEAL, MobEffects.HARM, MobEffects.HUNGER,
                            MobEffects.REGENERATION, MobEffects.POISON, MobEffects.WITHER,
                            MobEffects.JUMP, MobEffects.LEVITATION, MobEffects.SLOW_FALLING, ICrustApi.MOD_ID + ":" + CrustObjects.ID.WEIGHT,
                            MobEffects.DAMAGE_RESISTANCE, ICrustApi.MOD_ID + ":" + CrustObjects.ID.VULNERABILITY,
                            MobEffects.FIRE_RESISTANCE, MobEffects.WATER_BREATHING,
                            MobEffects.BLINDNESS, MobEffects.NIGHT_VISION, MobEffects.CONFUSION,
                            MobEffects.HEALTH_BOOST, MobEffects.ABSORPTION,
                            MobEffects.GLOWING
                    ),
                    "List of potions that " + speciesName + " can be 'filled' with on spawn (they will apply it on hit). " +
                            "Each effect in the list has an equal chance to be selected." ) );
        }
    }
}