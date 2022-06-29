package fathertoast.specialmobs.client;

import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Here we cache model data for ninja block disguises
 * in order to avoid refreshing model data each frame.
 */
public class NinjaModelDataHolder {

    private static final Map<Integer, IModelData> MODEL_DATA = new ConcurrentHashMap<>();


    public static void putModelData(int entityId, @Nullable IModelData modelData) {
        MODEL_DATA.put(entityId, modelData == null ? EmptyModelData.INSTANCE : modelData);
    }

    @Nonnull
    public static IModelData getModelData(int entityId) {
        return MODEL_DATA.getOrDefault(entityId, EmptyModelData.INSTANCE);
    }
}
