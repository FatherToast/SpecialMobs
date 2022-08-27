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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.function.Supplier;

@Mod.EventBusSubscriber( value = Dist.CLIENT, modid = SpecialMobs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD )
public class ClientRegister {
    
    @SubscribeEvent
    public static void onClientSetup( FMLClientSetupEvent event ) {
        if( Config.MAIN.GENERAL.fancyFishingMobs.get() ) {
            ItemModelsProperties.register( Items.FISHING_ROD, new ResourceLocation( "cast" ), new FishingRodItemPropertyGetter() );
        }
        
        ClientEventHandler.registerConfigGUIFactory();
        registerEntityRenderers( event.getMinecraftSupplier() );
    }
    
    private static void registerEntityRenderers( Supplier<Minecraft> game ) {
        // Family-based renderers
        registerFamilyRenderers( MobFamily.CREEPER, SpecialCreeperRenderer::new );
        registerFamilyRenderers( MobFamily.ZOMBIE, SpecialZombieRenderer::new );
        registerFamilyRenderers( MobFamily.DROWNED, SpecialDrownedRenderer::new );
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
        registerSpeciesRenderer( EnderCreeperEntity.SPECIES, EnderCreeperRenderer::new );
        
        registerSpeciesRenderer( MadScientistZombieEntity.SPECIES, SpecialZombieVillagerRenderer::new );
        registerSpeciesRenderer( VampireZombifiedPiglinEntity.SPECIES, SpecialPiglinRenderer::newBothEars );
        
        registerSpeciesRenderer( NinjaSkeletonEntity.SPECIES, NinjaSkeletonRenderer::new );
        registerSpeciesRenderer( NinjaWitherSkeletonEntity.SPECIES, NinjaSkeletonRenderer::new );
        
        registerSpeciesRenderer( PotionSlimeEntity.SPECIES, PotionSlimeRenderer::new );
        
        registerSpeciesRenderer( PufferSilverfishEntity.SPECIES, ShortSilverfishRenderer::new );
        
        registerSpeciesRenderer( CorporealShiftGhastEntity.SPECIES, CorporealShiftGhastRenderer::new );

        registerSpeciesRenderer( RunicEndermanEntity.SPECIES, RunicEndermanRenderer::new );
        
        // Other
        registerRenderer( SMEntities.BONE_SHRAPNEL, BoneShrapnelRenderer::new );
        registerRenderer( SMEntities.BUG_SPIT, BugSpitRenderer::new );
        registerSpriteRenderer( SMEntities.INCORPOREAL_FIREBALL, game, 3.0F, true );
        registerRenderer( SMEntities.FISHING_BOBBER, SpecialFishingBobberRenderer::new );
    }
    
    private static <T extends LivingEntity> void registerFamilyRenderers( MobFamily<T, ?> family, IRenderFactory<? super T> renderFactory ) {
        RenderingRegistry.registerEntityRenderingHandler( family.vanillaReplacement.entityType.get(), renderFactory );
        for( MobFamily.Species<? extends T> species : family.variants )
            registerSpeciesRenderer( species, renderFactory );
    }
    
    private static <T extends LivingEntity> void registerSpeciesRenderer( MobFamily.Species<T> species, IRenderFactory<? super T> renderFactory ) {
        registerRenderer( species.entityType, renderFactory );
    }
    
    private static <T extends Entity> void registerRenderer( RegistryObject<EntityType<T>> entityType, IRenderFactory<? super T> renderFactory ) {
        RenderingRegistry.registerEntityRenderingHandler( entityType.get(), renderFactory );
    }
    
    @SuppressWarnings( "SameParameterValue" )
    private static <T extends Entity & IRendersAsItem>
    void registerSpriteRenderer( RegistryObject<EntityType<T>> entityType, Supplier<Minecraft> minecraftSupplier, float scale, boolean fullBright ) {
        ItemRenderer itemRenderer = minecraftSupplier.get().getItemRenderer();
        RenderingRegistry.registerEntityRenderingHandler( entityType.get(), ( renderManager ) ->
                new SpriteRenderer<>( renderManager, itemRenderer, scale, fullBright ) );
    }
}