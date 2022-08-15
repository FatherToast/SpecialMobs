package fathertoast.specialmobs.common.core.register;

import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.potion.WeightEffect;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class SMEffects {
    
    public static final DeferredRegister<Effect> REGISTRY = DeferredRegister.create( ForgeRegistries.POTIONS, SpecialMobs.MOD_ID );
    
    public static final RegistryObject<Effect> VULNERABILITY = register( "vulnerability", EffectType.HARMFUL, 0x96848D );
    public static final RegistryObject<Effect> WEIGHT = register( "weight", () -> new WeightEffect( EffectType.HARMFUL, 0x353A6B ) );
    
    /** Registers a simple effect to the deferred register. */
    public static RegistryObject<Effect> register( String name, EffectType type, int color ) {
        return register( name, () -> new SimpleEffect( type, color ) );
    }
    
    /** Registers a custom effect to the deferred register. */
    public static <T extends Effect> RegistryObject<T> register( String name, Supplier<T> effect ) { return REGISTRY.register( name, effect ); }
    
    /** Really just here to allow access to the Effect::new. */
    private static class SimpleEffect extends Effect {
        SimpleEffect( EffectType type, int color ) { super( type, color ); }
    }
}