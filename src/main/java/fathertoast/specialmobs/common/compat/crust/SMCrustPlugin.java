package fathertoast.specialmobs.common.compat.crust;

import fathertoast.crust.api.CrustPlugin;
import fathertoast.crust.api.ICrustApi;
import fathertoast.crust.api.ICrustPlugin;
import fathertoast.crust.api.entity.IPlayerVelocityWatcher;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.resources.ResourceLocation;

@CrustPlugin
public class SMCrustPlugin implements ICrustPlugin {
    
    private static ICrustApi crustApi;
    
    private static final ResourceLocation ID = SpecialMobs.rl("specialmobs_crust");
    
    
    @Override
    public void onLoad( ICrustApi apiInstance ) {
        crustApi = apiInstance;
    }
    
    @Override
    public ResourceLocation getId() {
        return ID;
    }
    
    /** @return Crust's player velocity watcher instance. */
    public static IPlayerVelocityWatcher getVelocityWatcher() {
        if ( crustApi == null )
            throw new IllegalStateException( "Crust API has not been obtained yet!" );
        return crustApi.getPlayerVelocityWatcher();
    }
}
