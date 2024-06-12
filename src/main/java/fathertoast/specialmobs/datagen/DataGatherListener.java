package fathertoast.specialmobs.datagen;

import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.core.register.SMDamageTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber( modid = SpecialMobs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD )
public class DataGatherListener {
    
    @SubscribeEvent
    public static void onGatherData( GatherDataEvent event ) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = CompletableFuture.supplyAsync(DataGatherListener::getProvider);
        
        if( event.includeClient() ) {
            generator.addProvider( true, new SMBlockStateAndModelProvider( packOutput, fileHelper ) );
            generator.addProvider( true, new SMItemModelProvider( packOutput, fileHelper ) );
            for( Map.Entry<String, SMLanguageProvider.TranslationKey> entry : SMLanguageProvider.LANG_CODE_MAP.entrySet() ) {
                generator.addProvider( true, new SMLanguageProvider( packOutput, entry.getKey(), entry.getValue() ) );
            }
        }
        if( event.includeServer() ) {
            generator.addProvider( true, new SMLootTableProvider( packOutput ) );

            BlockTagsProvider blockTagProvider = new SMBlockTagProvider( packOutput, lookupProvider, fileHelper );

            generator.addProvider( true, blockTagProvider );
            generator.addProvider(true, new SMItemTagProvider( packOutput, lookupProvider, blockTagProvider.contentsGetter(), fileHelper ) );
            generator.addProvider( true, new SMEntityTagProvider( packOutput, lookupProvider, fileHelper ) );
            generator.addProvider( true, new SMDamageTagProvider( packOutput, lookupProvider, fileHelper ) );
            generator.addProvider( event.includeServer(), new DatapackBuiltinEntriesProvider(
                    packOutput, lookupProvider, Set.of( SpecialMobs.MOD_ID )));
        }
    }

    private static HolderLookup.Provider getProvider() {
        final RegistrySetBuilder registryBuilder = new RegistrySetBuilder();
        registryBuilder.add(Registries.DAMAGE_TYPE, SMDamageTypes::bootstrap);
        // We need the BIOME registry to be present, so we can use a biome tag, doesn't matter that it's empty
        registryBuilder.add(Registries.BIOME, context -> {
        });
        RegistryAccess.Frozen regAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        return registryBuilder.buildPatch(regAccess, VanillaRegistries.createLookup());
    }
}