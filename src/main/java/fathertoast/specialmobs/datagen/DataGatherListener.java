package fathertoast.specialmobs.datagen;

import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import java.util.Map;

@Mod.EventBusSubscriber( modid = SpecialMobs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD )
public class DataGatherListener {
    
    @SubscribeEvent
    public static void onGatherData( GatherDataEvent event ) {
        DataGenerator generator = event.getGenerator();
        
        if( event.includeClient() ) {
            for( Map.Entry<String, SMLanguageProvider.TranslationKey> entry : SMLanguageProvider.LANG_CODE_MAP.entrySet() ) {
                generator.addProvider( new SMLanguageProvider( generator, entry.getKey(), entry.getValue() ) );
            }
        }
        if( event.includeServer() ) {
            generator.addProvider( new SMLootTableProvider( generator ) );
        }
    }
}