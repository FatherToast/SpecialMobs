package fathertoast.specialmobs.common.config.species;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.field.LazyRegistryEntryListField;
import fathertoast.specialmobs.common.config.field.RegistryEntryListField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.util.ConfigUtil;
import fathertoast.specialmobs.common.config.util.LazyRegistryEntryList;
import fathertoast.specialmobs.common.core.register.SMEffects;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Effects;
import net.minecraftforge.registries.ForgeRegistries;

public class PotionSlimeSpeciesConfig extends SpeciesConfig {
    
    public final Potion POTION;
    
    /** Builds the config spec that should be used for this config. */
    public PotionSlimeSpeciesConfig( MobFamily.Species<?> species ) {
        super( species );
        
        POTION = new Potion( SPEC, species, speciesName );
    }
    
    public static class Potion extends Config.AbstractCategory {
        
        public final RegistryEntryListField<Effect> allowedPotions;
        
        Potion( ToastConfigSpec parent, MobFamily.Species<?> species, String speciesName ) {
            super( parent, ConfigUtil.camelCaseToLowerUnderscore( species.specialVariantName ),
                    "Options specific to " + speciesName + "." );
            
            allowedPotions = SPEC.define( new LazyRegistryEntryListField<>( "allowed_effects",
                    new LazyRegistryEntryList<>( ForgeRegistries.POTIONS,
                            Effects.MOVEMENT_SPEED, Effects.MOVEMENT_SLOWDOWN,
                            Effects.DIG_SPEED, Effects.DIG_SLOWDOWN,
                            Effects.DAMAGE_BOOST, Effects.WEAKNESS,
                            Effects.HEAL, Effects.HARM, Effects.HUNGER,
                            Effects.REGENERATION, Effects.POISON, Effects.WITHER,
                            Effects.JUMP, Effects.SLOW_FALLING, Effects.LEVITATION,
                            Effects.DAMAGE_RESISTANCE, SMEffects.VULNERABILITY,
                            Effects.FIRE_RESISTANCE, Effects.WATER_BREATHING,
                            Effects.BLINDNESS, Effects.NIGHT_VISION, Effects.CONFUSION,
                            Effects.HEALTH_BOOST, Effects.ABSORPTION,
                            Effects.GLOWING
                    ),
                    "List of potions that " + speciesName + " can be 'filled' with on spawn (they will apply it on hit).",
                    "Each effect in the list has an equal chance to be selected." ) );
        }
    }
}