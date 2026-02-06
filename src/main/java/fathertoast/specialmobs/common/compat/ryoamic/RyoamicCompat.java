package fathertoast.specialmobs.common.compat.ryoamic;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.entity.blaze.CinderBlazeEntity;
import fathertoast.specialmobs.common.entity.blaze.EmberBlazeEntity;
import fathertoast.specialmobs.common.entity.cavespider.FireCaveSpiderEntity;
import fathertoast.specialmobs.common.entity.creeper.DarkCreeperEntity;
import fathertoast.specialmobs.common.entity.drowned.AbyssalDrownedEntity;
import fathertoast.specialmobs.common.entity.enderman.FlameEndermanEntity;
import fathertoast.specialmobs.common.entity.enderman.RunicEndermanEntity;
import fathertoast.specialmobs.common.entity.magmacube.HardenedMagmaCubeEntity;
import fathertoast.specialmobs.common.entity.silverfish.FireSilverfishEntity;
import fathertoast.specialmobs.common.entity.skeleton.FireSkeletonEntity;
import fathertoast.specialmobs.common.entity.skeleton.SpitfireSkeletonEntity;
import fathertoast.specialmobs.common.entity.spider.FireSpiderEntity;
import fathertoast.specialmobs.common.entity.witherskeleton.SpitfireWitherSkeletonEntity;
import fathertoast.specialmobs.common.entity.zombie.FireZombieEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;
import org.thinkingstudio.ryoamiclights.RyoamicLights;
import org.thinkingstudio.ryoamiclights.api.DynamicLightHandler;
import org.thinkingstudio.ryoamiclights.api.DynamicLightHandlers;

import java.util.List;

/** Contains compat setup and helper methods for the RyoamicLights mod. */
public class RyoamicCompat {
    
    private static final String RYOAMIC = "ryoamiclights";
    
    
    /** Called from {@link fathertoast.specialmobs.client.ClientRegister#onClientSetup(FMLClientSetupEvent)}. */
    public static void init() {
        if( FMLEnvironment.dist != Dist.CLIENT ) return;
        
        if( ModList.get().isLoaded( RYOAMIC ) ) {
            registerHandlers();
        }
    }
    
    /** Here we register our dynamic light handlers. */
    public static void registerHandlers() {
        // CREEPERS
        forAllSpeciesExcept( MobFamily.CREEPER, DynamicLightHandler.makeCreeperEntityHandler( null ), List.of(
                DarkCreeperEntity.SPECIES
        ) );
        DynamicLightHandlers.registerDynamicLightHandler( DarkCreeperEntity.SPECIES.entityType.get(),
                DynamicLightHandler.makeCreeperEntityHandler( ( creeper ) -> 7 ) );
        
        // BLAZES
        forAllSpeciesExcept( MobFamily.BLAZE, DynamicLightHandler.makeHandler( blaze -> 10, blaze -> true ), List.of(
                EmberBlazeEntity.SPECIES,
                CinderBlazeEntity.SPECIES
        ) );
        DynamicLightHandlers.registerDynamicLightHandler( EmberBlazeEntity.SPECIES.entityType.get(),
                DynamicLightHandler.makeHandler( blaze -> 3, blaze -> true ) );
        DynamicLightHandlers.registerDynamicLightHandler( CinderBlazeEntity.SPECIES.entityType.get(),
                DynamicLightHandler.makeHandler( blaze -> 6, blaze -> true ) );
        
        // ENDERMEN
        forAllSpeciesExcept( MobFamily.ENDERMAN, makeEnderManHandler( null ), List.of(
                RunicEndermanEntity.SPECIES,
                FlameEndermanEntity.SPECIES
        ) );
        DynamicLightHandlers.registerDynamicLightHandler( RunicEndermanEntity.SPECIES.entityType.get(),
                makeEnderManHandler( ( enderMan ) -> 4 ) );
        DynamicLightHandlers.registerDynamicLightHandler( FlameEndermanEntity.SPECIES.entityType.get(),
                makeEnderManHandler( ( enderMan ) -> 4 ) );
        
        // MAGMA CUBES
        forAllSpeciesExcept( MobFamily.MAGMA_CUBE, magmaCube -> (magmaCube.squish > 0.6) ? 11 : 8, List.of(
                HardenedMagmaCubeEntity.SPECIES
        ) );
        DynamicLightHandlers.registerDynamicLightHandler( HardenedMagmaCubeEntity.SPECIES.entityType.get(),
                magmaCube -> (magmaCube.squish > 0.6) ? 14 : 11 );
        
        // ZOMBIES (Uses living entity handler by default)
        DynamicLightHandlers.registerDynamicLightHandler( FireZombieEntity.SPECIES.entityType.get(),
                DynamicLightHandler.makeHandler( ( fireZombie ) -> 5, ( fireZombie ) -> true ) );
        
        // DROWNED
        DynamicLightHandlers.registerDynamicLightHandler( AbyssalDrownedEntity.SPECIES.entityType.get(), DynamicLightHandler.makeLivingEntityHandler( ( abyssalDrowned ) -> 4 ) );
        
        // SKELETONS
        DynamicLightHandlers.registerDynamicLightHandler( FireSkeletonEntity.SPECIES.entityType.get(), DynamicLightHandler.makeLivingEntityHandler(
                DynamicLightHandler.makeHandler( ( fireSkeleton ) -> 5, ( fireSkeleton ) -> true ) ) );
        DynamicLightHandlers.registerDynamicLightHandler( SpitfireSkeletonEntity.SPECIES.entityType.get(), DynamicLightHandler.makeLivingEntityHandler(
                DynamicLightHandler.makeHandler( ( spitfireSkeleton ) -> 5, ( spitfireSkeleton ) -> true ) ) );
        
        // WITHER SKELETONS
        DynamicLightHandlers.registerDynamicLightHandler( SpitfireWitherSkeletonEntity.SPECIES.entityType.get(), DynamicLightHandler.makeLivingEntityHandler(
                DynamicLightHandler.makeHandler( ( spitfireWitherSkeleton ) -> 5, ( spitfireWitherSkeleton ) -> true ) ) );
        
        // SPIDERS
        DynamicLightHandlers.registerDynamicLightHandler( FireSpiderEntity.SPECIES.entityType.get(),
                DynamicLightHandler.makeHandler( ( fireSpider ) -> 5, ( fireSpider ) -> true ) );
        
        // CAVE SPIDERS
        DynamicLightHandlers.registerDynamicLightHandler( FireCaveSpiderEntity.SPECIES.entityType.get(),
                DynamicLightHandler.makeHandler( ( fireCaveSpider ) -> 5, ( fireCaveSpider ) -> true ) );
        
        // SILVERFISH
        DynamicLightHandlers.registerDynamicLightHandler( FireSilverfishEntity.SPECIES.entityType.get(),
                DynamicLightHandler.makeHandler( ( fireSilverfish ) -> 5, ( fireSilverfish ) -> true ) );
    }
    
    private static <T extends EnderMan> DynamicLightHandler<T> makeEnderManHandler( @Nullable DynamicLightHandler<T> handler ) {
        return entity -> {
            int luminance = 0;
            
            if( entity.getCarriedBlock() != null )
                // noinspection deprecation
                luminance = entity.getCarriedBlock().getLightEmission();
            
            if( handler != null )
                luminance = Math.max( luminance, handler.getLuminance( entity ) );
            
            return luminance;
        };
    }
    
    /**
     * Registers a dynamic light handler for every mob species
     * within a family, aside from those listed as exceptions.
     */
    @SuppressWarnings( "unchecked" )
    private static <T extends Mob> void forAllSpeciesExcept( MobFamily<T, ?> family, DynamicLightHandler<T> handler,
                                                             List<MobFamily.Species<? extends T>> exceptions ) {
        // Register for each species that are not in the exception list.
        for( MobFamily.Species<? extends T> species : family.variants ) {
            if( exceptions.contains( species ) )
                continue;
            
            DynamicLightHandlers.registerDynamicLightHandler( (EntityType<T>) species.entityType.get(), handler );
        }
        // Register for the vanilla replacement.
        DynamicLightHandlers.registerDynamicLightHandler( (EntityType<T>) family.vanillaReplacement.entityType.get(), handler );
    }
    
    /**
     * @return Whichever value is greater if RyoamicLights is installed; block light or dynamic light.
     * If the mod is not installed, this just returns the block light at the given position.
     * <br><br>
     * If RyoamicLights is installed, but we fail to look up the dynamic light
     * for whatever reason, we print a warning and return the block light.
     */
    public static int getBlockOrDynamicLightAt( Level level, BlockPos pos ) {
        int blockLight = level.getBrightness( LightLayer.BLOCK, pos );
        
        if( ModList.get().isLoaded( RYOAMIC ) ) {
            try {
                RyoamicLights instance = RyoamicLights.get();
                int dynamicLight = (int) instance.getDynamicLightLevel( pos );
                return Math.max( dynamicLight, blockLight );
            }
            catch( Exception e ) {
                warn( "Failed to look up dynamic light at position: {}", pos.toString() );
                return blockLight;
            }
        }
        return blockLight;
    }
    
    @SuppressWarnings( { "StringConcatenationArgumentToLogCall", "SameParameterValue" } )
    private static void warn( String message, Object... args ) {
        SpecialMobs.LOG.warn( "[RyoamicLights compat] " + message, args );
    }
}
