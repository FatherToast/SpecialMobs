package fathertoast.specialmobs.common.network.work;

import fathertoast.specialmobs.client.NinjaModelDataHolder;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.entity.ai.INinja;
import fathertoast.specialmobs.common.network.message.S2CUpdateNinjaModelData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;

public class ClientWork {

    public static void updateNinjaModelData(S2CUpdateNinjaModelData message) {
        ClientWorld world = Minecraft.getInstance().level;

        if (world == null)
            return;

        Entity entity = world.getEntity(message.entityId);

        if (entity == null)
            return;

        if (entity instanceof INinja) {
            IModelData modelData = ModelDataManager.getModelData(world, message.pos);
            NinjaModelDataHolder.putModelData(message.entityId, modelData);
        }
        else {
            SpecialMobs.LOG.warn("Attempted to update block model data for entity with id \"{}\" and of type \"{}\", but the entity was not a ninja!", message.entityId, entity.getType().getRegistryName());
        }
    }
}
