package fathertoast.specialmobs.client;

import fathertoast.specialmobs.client.renderer.entity.SpecialCreeperRenderer;
import fathertoast.specialmobs.client.renderer.entity.SpecialSkeletonRenderer;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.core.SpecialMobs;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Mod.EventBusSubscriber( value = Dist.CLIENT, modid = SpecialMobs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD )
public class ClientRegister {
    
    @SubscribeEvent
    public static void onClientSetup( FMLClientSetupEvent event ) {
        registerEntityRenderers();
    }
    
    private static void registerEntityRenderers() {
        // Family-based renderers
        registerFamilyRenderers( MobFamily.CREEPER, SpecialCreeperRenderer::new );
        //registerFamilyRenderers( MobFamily.SKELETON, SpecialSkeletonRenderer::new );
    }
    
    private static <T extends LivingEntity> void registerFamilyRenderers( MobFamily<T> family, IRenderFactory<? super T> renderFactory ) {
        RenderingRegistry.registerEntityRenderingHandler( family.vanillaReplacement.entityType.get(), renderFactory );
        for( MobFamily.Species<? extends T> species : family.variants )
            RenderingRegistry.registerEntityRenderingHandler( species.entityType.get(), renderFactory );
    }
}