package fathertoast.specialmobs.client;

import fathertoast.specialmobs.client.renderer.entity.family.*;
import fathertoast.specialmobs.client.renderer.entity.projectile.BoneShrapnelRenderer;
import fathertoast.specialmobs.client.renderer.entity.projectile.BugSpitRenderer;
import fathertoast.specialmobs.client.renderer.entity.projectile.SpecialFishingBobberRenderer;
import fathertoast.specialmobs.client.renderer.entity.species.*;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.core.register.SMEntities;
import fathertoast.specialmobs.common.entity.creeper.EnderCreeperEntity;
import fathertoast.specialmobs.common.entity.enderman.RunicEndermanEntity;
import fathertoast.specialmobs.common.entity.ghast.CorporealShiftGhastEntity;
import fathertoast.specialmobs.common.entity.silverfish.PufferSilverfishEntity;
import fathertoast.specialmobs.common.entity.skeleton.NinjaSkeletonEntity;
import fathertoast.specialmobs.common.entity.slime.PotionSlimeEntity;
import fathertoast.specialmobs.common.entity.witherskeleton.NinjaWitherSkeletonEntity;
import fathertoast.specialmobs.common.entity.zombie.MadScientistZombieEntity;
import fathertoast.specialmobs.common.entity.zombifiedpiglin.VampireZombifiedPiglinEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber( value = Dist.CLIENT, modid = SpecialMobs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD )
public class ClientRegister {
    
    @SubscribeEvent
    public static void onClientSetup( FMLClientSetupEvent event ) {
        if( Config.MAIN.GENERAL.fancyFishingMobs.get() ) {
            event.enqueueWork(() -> {
                ItemProperties.register( Items.FISHING_ROD, new ResourceLocation( "cast" ), new FishingRodItemPropertyGetter() );
            });
        }
        ClientEventHandler.registerConfigGUIFactory();
    }

    @SubscribeEvent
    public static void registerEntityRenderers( EntityRenderersEvent.RegisterRenderers event ) {
        // Family-based renderers
        registerFamilyRenderers( event, MobFamily.CREEPER, SpecialCreeperRenderer::new );
        registerFamilyRenderers( event, MobFamily.ZOMBIE, SpecialZombieRenderer::new );
        registerFamilyRenderers( event, MobFamily.DROWNED, SpecialDrownedRenderer::new );
        registerFamilyRenderers( event, MobFamily.ZOMBIFIED_PIGLIN, SpecialPiglinRenderer::newMissingRightEar );
        registerFamilyRenderers( event, MobFamily.SKELETON, SpecialSkeletonRenderer::new );
        registerFamilyRenderers( event, MobFamily.WITHER_SKELETON, SpecialSkeletonRenderer::new );
        registerFamilyRenderers( event, MobFamily.SLIME, SpecialSlimeRenderer::new );
        registerFamilyRenderers( event, MobFamily.MAGMA_CUBE, SpecialMagmaCubeRenderer::new );
        registerFamilyRenderers( event, MobFamily.SPIDER, SpecialSpiderRenderer::new );
        registerFamilyRenderers( event, MobFamily.CAVE_SPIDER, SpecialSpiderRenderer::new );
        registerFamilyRenderers( event, MobFamily.SILVERFISH, SpecialSilverfishRenderer::new );
        registerFamilyRenderers( event, MobFamily.ENDERMAN, SpecialEndermanRenderer::new );
        registerFamilyRenderers( event, MobFamily.WITCH, SpecialWitchRenderer::new );
        registerFamilyRenderers( event, MobFamily.GHAST, SpecialGhastRenderer::new );
        registerFamilyRenderers( event, MobFamily.BLAZE, SpecialBlazeRenderer::new );
        
        // Species overrides
        registerSpeciesRenderer( event, EnderCreeperEntity.SPECIES, EnderCreeperRenderer::new );
        
        registerSpeciesRenderer( event, MadScientistZombieEntity.SPECIES, SpecialZombieVillagerRenderer::new );
        registerSpeciesRenderer( event, VampireZombifiedPiglinEntity.SPECIES, SpecialPiglinRenderer::newBothEars );
        
        registerSpeciesRenderer( event, NinjaSkeletonEntity.SPECIES, NinjaSkeletonRenderer::new );
        registerSpeciesRenderer( event, NinjaWitherSkeletonEntity.SPECIES, NinjaSkeletonRenderer::new );
        
        registerSpeciesRenderer( event, PotionSlimeEntity.SPECIES, PotionSlimeRenderer::new );
        
        registerSpeciesRenderer( event, PufferSilverfishEntity.SPECIES, ShortSilverfishRenderer::new );
        
        registerSpeciesRenderer( event, CorporealShiftGhastEntity.SPECIES, CorporealShiftGhastRenderer::new );

        registerSpeciesRenderer( event, RunicEndermanEntity.SPECIES, RunicEndermanRenderer::new );
        
        // Other
        registerRenderer( event, SMEntities.BONE_SHRAPNEL, BoneShrapnelRenderer::new );
        registerRenderer( event, SMEntities.BUG_SPIT, BugSpitRenderer::new );
        registerSpriteRenderer( event, SMEntities.INCORPOREAL_FIREBALL, 3.0F, true );
        registerRenderer( event, SMEntities.FISHING_BOBBER, SpecialFishingBobberRenderer::new );
    }

    @SuppressWarnings("unchecked")
    private static <T extends Mob, E extends LivingEntity> void registerFamilyRenderers( EntityRenderersEvent.RegisterRenderers event, MobFamily<T, ?> family, EntityRendererProvider<E> renderFactory ) {
        event.registerEntityRenderer( family.vanillaReplacement.entityType.get(), (EntityRendererProvider<T>) renderFactory );
        for( MobFamily.Species<? extends T> species : family.variants )
            registerSpeciesRenderer( event, species, renderFactory );
    }

    @SuppressWarnings("unchecked")
    private static <T extends Mob, E extends LivingEntity> void registerSpeciesRenderer( EntityRenderersEvent.RegisterRenderers event, MobFamily.Species<T> species, EntityRendererProvider<E> renderFactory ) {
        registerRenderer( event, species.entityType, (EntityRendererProvider<T>) renderFactory );
    }
    
    private static <T extends Entity> void registerRenderer( EntityRenderersEvent.RegisterRenderers event, RegistryObject<EntityType<T>> entityType, EntityRendererProvider<T> renderFactory ) {
        event.registerEntityRenderer( entityType.get(), renderFactory );
    }
    
    @SuppressWarnings( "SameParameterValue" )
    private static <T extends Entity & ItemSupplier>
    void registerSpriteRenderer( EntityRenderersEvent.RegisterRenderers event, RegistryObject<EntityType<T>> entityType, float scale, boolean fullBright ) {
        event.registerEntityRenderer( entityType.get(), ( context ) ->
                new ThrownItemRenderer<>( context, scale, fullBright ) );
    }
}