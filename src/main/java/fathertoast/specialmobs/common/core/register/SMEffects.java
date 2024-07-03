package fathertoast.specialmobs.common.core.register;

import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class SMEffects {
    
    public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create( ForgeRegistries.MOB_EFFECTS, SpecialMobs.MOD_ID );
    
    /** Registers a simple effect to the deferred register. */
    public static RegistryObject<MobEffect> register( String name, MobEffectCategory category, int color ) {
        return register( name, () -> new SimpleEffect( category, color ) );
    }
    
    /** Registers a custom effect to the deferred register. */
    public static <T extends MobEffect> RegistryObject<T> register(String name, Supplier<T> effect ) { return REGISTRY.register( name, effect ); }
    
    /** Really just here to allow access to the Effect::new. */
    private static class SimpleEffect extends MobEffect {
        SimpleEffect(MobEffectCategory category, int color ) { super( category, color ); }
    }
}