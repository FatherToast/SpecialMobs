package fathertoast.specialmobs.client;

import fathertoast.specialmobs.client.renderer.entity.*;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.entity.skeleton.NinjaSkeletonEntity;
import fathertoast.specialmobs.common.entity.witherskeleton.NinjaWitherSkeletonEntity;
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
        ClientEventHandler.registerConfigGUIFactory();
        registerEntityRenderers();
    }
    
    private static void registerEntityRenderers() {
        // Family-based renderers
        registerFamilyRenderers( MobFamily.CREEPER, SpecialCreeperRenderer::new );
        registerFamilyRenderers( MobFamily.ZOMBIE, SpecialZombieRenderer::new );
        registerFamilyRenderers( MobFamily.ZOMBIFIED_PIGLIN, SpecialPiglinRenderer::newMissingRightEar );
        registerFamilyRenderers( MobFamily.SKELETON, SpecialSkeletonRenderer::new );
        registerFamilyRenderers( MobFamily.WITHER_SKELETON, SpecialSkeletonRenderer::new );
        registerFamilyRenderers( MobFamily.SLIME, SpecialSlimeRenderer::new );
        registerFamilyRenderers( MobFamily.MAGMA_CUBE, SpecialMagmaCubeRenderer::new );
        registerFamilyRenderers( MobFamily.SPIDER, SpecialSpiderRenderer::new );
        registerFamilyRenderers( MobFamily.CAVE_SPIDER, SpecialSpiderRenderer::new );
        registerFamilyRenderers( MobFamily.SILVERFISH, SpecialSilverfishRenderer::new );
        registerFamilyRenderers( MobFamily.ENDERMAN, SpecialEndermanRenderer::new );
        registerFamilyRenderers( MobFamily.WITCH, SpecialWitchRenderer::new );
        registerFamilyRenderers( MobFamily.GHAST, SpecialGhastRenderer::new );
        registerFamilyRenderers( MobFamily.BLAZE, SpecialBlazeRenderer::new );
        
        // Species overrides
        registerSpeciesRenderer( NinjaSkeletonEntity.SPECIES, NinjaSkeletonRenderer::new );
        registerSpeciesRenderer( NinjaWitherSkeletonEntity.SPECIES, NinjaSkeletonRenderer::new );
    }
    
    private static <T extends LivingEntity> void registerFamilyRenderers( MobFamily<T> family, IRenderFactory<? super T> renderFactory ) {
        RenderingRegistry.registerEntityRenderingHandler( family.vanillaReplacement.entityType.get(), renderFactory );
        for( MobFamily.Species<? extends T> species : family.variants )
            registerSpeciesRenderer( species, renderFactory );
    }
    
    private static <T extends LivingEntity> void registerSpeciesRenderer( MobFamily.Species<T> species, IRenderFactory<? super T> renderFactory ) {
        RenderingRegistry.registerEntityRenderingHandler( species.entityType.get(), renderFactory );
    }
}