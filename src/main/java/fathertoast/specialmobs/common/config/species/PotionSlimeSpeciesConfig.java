package fathertoast.specialmobs.common.config.species;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.field.LazyRegistryEntryListField;
import fathertoast.specialmobs.common.config.field.RegistryEntryListField;
import fathertoast.specialmobs.common.config.file.ToastConfigSpec;
import fathertoast.specialmobs.common.config.util.ConfigUtil;
import fathertoast.specialmobs.common.config.util.LazyRegistryEntryList;
import fathertoast.specialmobs.common.core.register.SMEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.registries.ForgeRegistries;

public class PotionSlimeSpeciesConfig extends SpeciesConfig {
    
    public final Potion POTION;
    
    /** Builds the config spec that should be used for this config. */
    public PotionSlimeSpeciesConfig( MobFamily.Species<?> species ) {
        super( species );
        
        POTION = new Potion( SPEC, species, species.getConfigName() );
    }
    
    public static class Potion extends Config.AbstractCategory {
        
        public final RegistryEntryListField<MobEffect> allowedPotions;
        
        Potion( ToastConfigSpec parent, MobFamily.Species<?> species, String speciesName ) {
            super( parent, ConfigUtil.camelCaseToLowerUnderscore( species.specialVariantName ),
                    "Options specific to " + speciesName + "." );
            
            allowedPotions = SPEC.define( new LazyRegistryEntryListField<>( "allowed_effects",
                    new LazyRegistryEntryList<>( ForgeRegistries.MOB_EFFECTS, false, new Object[] {
                            MobEffects.MOVEMENT_SPEED, MobEffects.MOVEMENT_SLOWDOWN,
                            MobEffects.DIG_SPEED, MobEffects.DIG_SLOWDOWN,
                            MobEffects.DAMAGE_BOOST, MobEffects.WEAKNESS,
                            MobEffects.HEAL, MobEffects.HARM, MobEffects.HUNGER,
                            MobEffects.REGENERATION, MobEffects.POISON, MobEffects.WITHER,
                            MobEffects.JUMP, MobEffects.LEVITATION, MobEffects.SLOW_FALLING, SMEffects.WEIGHT,
                            MobEffects.DAMAGE_RESISTANCE, SMEffects.VULNERABILITY,
                            MobEffects.FIRE_RESISTANCE, MobEffects.WATER_BREATHING,
                            MobEffects.BLINDNESS, MobEffects.NIGHT_VISION, MobEffects.CONFUSION,
                            MobEffects.HEALTH_BOOST, MobEffects.ABSORPTION,
                            MobEffects.GLOWING
                        }
                    ),
                    "List of potions that " + speciesName + " can be 'filled' with on spawn (they will apply it on hit).",
                    "Each effect in the list has an equal chance to be selected." ) );
        }
    }
}