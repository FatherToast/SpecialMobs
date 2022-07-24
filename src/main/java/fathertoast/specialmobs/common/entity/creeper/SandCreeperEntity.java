package fathertoast.specialmobs.common.entity.creeper;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.ExplosionHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.storage.IServerWorldInfo;

import java.util.List;

@SpecialMob
public class SandCreeperEntity extends _SpecialCreeperEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<SandCreeperEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xE6DDAC ).theme( BestiaryInfo.Theme.DESERT )
                .uniqueTextureBaseOnly()
                .addExperience( 1 )
                .addToAttribute( Attributes.ARMOR, 2.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 1.2 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Sand Creeper",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addClusterDrop( "common", Blocks.SAND );
        loot.addUncommonDrop( "uncommon", Blocks.CHISELED_SANDSTONE );
        loot.addRareDrop( "rare", Items.GOLD_INGOT );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<SandCreeperEntity> getVariantFactory() { return SandCreeperEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends SandCreeperEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public SandCreeperEntity( EntityType<? extends _SpecialCreeperEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to change this creeper's explosion. */
    @Override
    protected void makeVariantExplosion( float explosionPower ) {
        final Explosion.Mode explosionMode = ExplosionHelper.getMode( this );
        
        // Clear water before running the explosion so it can't shield blocks from damage
        if( explosionMode != Explosion.Mode.NONE ) {
            final int radius = (int) Math.floor( explosionPower ) + 2;
            final BlockPos center = new BlockPos( position() );
            
            for( int y = -radius; y <= radius; y++ ) {
                for( int x = -radius; x <= radius; x++ ) {
                    for( int z = -radius; z <= radius; z++ ) {
                        if( x * x + y * y + z * z <= radius * radius ) {
                            clearWater( center.offset( x, y, z ) );
                        }
                    }
                }
            }
        }
        
        final ExplosionHelper explosion = new ExplosionHelper( this, explosionPower, explosionMode, false );
        if( !explosion.initializeExplosion() ) return;
        explosion.finalizeExplosion();
        
        // Clear weather
        if( isPowered() && level.getLevelData() instanceof IServerWorldInfo ) {
            final IServerWorldInfo serverInfo = (IServerWorldInfo) level.getLevelData();
            
            serverInfo.setClearWeatherTime( random.nextInt( 12000 ) + 3600 );
            serverInfo.setRainTime( 0 );
            serverInfo.setRaining( false );
            serverInfo.setThunderTime( 0 );
            serverInfo.setThundering( false );
        }
    }
    
    private void clearWater( BlockPos pos ) {
        if( !level.getFluidState( pos ).is( FluidTags.WATER ) ) return;
        
        final BlockState block = level.getBlockState( pos );
        
        if( block.getBlock() instanceof IBucketPickupHandler &&
                ((IBucketPickupHandler) block.getBlock()).takeLiquid( level, pos, block ) != Fluids.EMPTY ) {
            // Removed through bucket pickup handler
            return;
        }
        
        if( block.getBlock() instanceof FlowingFluidBlock ) {
            level.setBlock( pos, Blocks.AIR.defaultBlockState(), References.SetBlockFlags.DEFAULTS );
            return;
        }
        
        final Material material = block.getMaterial();
        if( material == Material.WATER_PLANT || material == Material.REPLACEABLE_WATER_PLANT ) {
            final TileEntity tileEntity = block.hasTileEntity() ? level.getBlockEntity( pos ) : null;
            Block.dropResources( block, level, pos, tileEntity );
            level.setBlock( pos, Blocks.AIR.defaultBlockState(), References.SetBlockFlags.DEFAULTS );
        }
    }
    
    /**
     * Override to change effects applied by the lingering cloud left by this creeper's explosion.
     * If this list is empty, the lingering cloud is not created.
     */
    @Override
    protected void modifyVariantLingeringCloudEffects( List<EffectInstance> potions ) {
        potions.add( new EffectInstance( Effects.HUNGER, 600 ) );
    }
    
    /** Override to change stats of the lingering cloud left by this creeper's explosion. */
    @Override
    protected void modifyVariantLingeringCloud( AreaEffectCloudEntity potionCloud ) {
        final int duration = 40;
        final float minRadius = 0.5F;
        final float maxRadius = (getVariantExplosionPower( explosionRadius ) + 1.0F) * 3.0F;
        
        potionCloud.setDuration( duration );
        potionCloud.setRadius( minRadius );
        potionCloud.setRadiusPerTick( (maxRadius - minRadius) / (float) duration ); // Growing cloud
        potionCloud.setRadiusOnUse( 0.0F );
    }
}