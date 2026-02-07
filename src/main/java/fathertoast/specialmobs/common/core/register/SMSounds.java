package fathertoast.specialmobs.common.core.register;

import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SMSounds {
    
    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create( ForgeRegistries.SOUND_EVENTS, SpecialMobs.MOD_ID );
    
    
    public static final RegistryObject<SoundEvent> IMPLODING_CREEPER_IMPLODE = register( "entity.imploding_creeper.implode" );
    
    
    private static RegistryObject<SoundEvent> register( String name ) {
        return REGISTRY.register( name, () -> SoundEvent.createVariableRangeEvent( SpecialMobs.rl( name ) ) );
    }
}
