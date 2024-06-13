package fathertoast.specialmobs.common.compat.jade;

import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.entity.ai.INinja;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import snownee.jade.api.*;
import snownee.jade.api.config.IWailaConfig;

/**
 * Our Jade plugin implementation. Here we can do funny stuff!
 */
@WailaPlugin
public class SMJadePlugin implements IWailaPlugin {

    private static final ResourceLocation displayNinjaDisguises = SpecialMobs.resourceLoc("display_ninja_disguises");


    @Override
    public void register(IWailaCommonRegistration registration) {

    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addConfig(displayNinjaDisguises, true);
        registration.markAsClientFeature(displayNinjaDisguises);

        // Make Jade display disguised ninjas as their disguise
        registration.addRayTraceCallback((hitResult, accessor, originalAccessor) -> {
            if (accessor instanceof EntityAccessor entityAccessor) {
                if (entityAccessor.getEntity() instanceof INinja ninja) {
                    BlockState disguise = ninja.getHiddenDragon();

                    if (IWailaConfig.get().getPlugin().get(displayNinjaDisguises) && disguise != null) {
                        Vec3 vec3 = entityAccessor.getHitResult().getLocation();

                        return registration.blockAccessor()
                                .blockEntity(() -> null)
                                .blockState(disguise)
                                .level(entityAccessor.getLevel())
                                .player(entityAccessor.getPlayer())
                                .serverConnected(entityAccessor.isServerConnected())
                                .showDetails(false)
                                .hit(new BlockHitResult(
                                        vec3,
                                        Direction.getNearest(vec3.x, vec3.y, vec3.z),
                                        entityAccessor.getHitResult().getEntity().blockPosition(),
                                        false
                                ))
                                .build();
                    }
                }
            }
            return accessor;
        });
    }
}
