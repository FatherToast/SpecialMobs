package fathertoast.specialmobs.common.event;

import net.minecraft.entity.EntityType;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraftforge.common.world.MobSpawnInfoBuilder;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BiomeEvents {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBiomeLoad(BiomeLoadingEvent event) {
        // Vanilla biome weirdness. Registry name can be null
        if (event.getName()  == null)
            return;

        // TODO - This is the place to add spawns to biomes if needed, rather than replacing

        //MobSpawnInfoBuilder spawnInfoBuilder = event.getSpawns();

        //for (EntityType<?> entityType : spawnInfoBuilder.getEntityTypes()) {
        //    if (Thing.hasMobVariant(entityType)) {
        //        spawnInfoBuilder.addSpawn(entityType.getCategory(), new MobSpawnInfo.Spawners(woah, ohMan, aaaugh, HAAAAUGGHHH));
        //    }
        //}
    }
}
