package fathertoast.specialmobs.common.entity.creeper;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.ExplosionHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.IServerWorldInfo;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class LightningCreeperEntity extends _SpecialCreeperEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<LightningCreeperEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        entityType.fireImmune();
        return new BestiaryInfo( 0x499CAE );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Lightning Creeper",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.REDSTONE );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<LightningCreeperEntity> getVariantFactory() { return LightningCreeperEntity::new; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public LightningCreeperEntity( EntityType<? extends _SpecialCreeperEntity> entityType, World world ) {
        super( entityType, world );
        xpReward += 1;
    }
    
    /** Override to change this creeper's explosion power multiplier. */
    @Override
    protected float getVariantExplosionPower( float radius ) { return radius * (isPowered() ? 2.0F : 1.0F / 3.0F); }
    
    /** Override to change this creeper's explosion. */
    @Override
    protected void makeVariantExplosion( float explosionPower ) {
        super.makeVariantExplosion( explosionPower );
        
        // Spawn lightning
        if( !level.isClientSide() ) {
            ExplosionHelper.spawnLightning( level, getX(), getY(), getZ() );
            if( explosionPower >= 2.0F ) {
                final int radius = (int) Math.floor( explosionPower );
                for( int x = -radius; x <= radius; x++ ) {
                    for( int z = -radius; z <= radius; z++ ) {
                        if( (x != 0 || z != 0) && x * x + z * z <= radius * radius && random.nextFloat() < 0.3F ) {
                            ExplosionHelper.spawnLightning( level, getX() + x, getY(), getZ() + z );
                        }
                    }
                }
            }
        }
        
        // Start a thunderstorm
        if( isPowered() && level.getLevelData() instanceof IServerWorldInfo ) {
            final IServerWorldInfo serverInfo = (IServerWorldInfo) level.getLevelData();
            
            int duration = random.nextInt( 12000 ) + 3600;
            if( !serverInfo.isThundering() || serverInfo.getThunderTime() < duration ) {
                serverInfo.setThunderTime( duration );
                serverInfo.setThundering( true );
            }
            duration += 1200;
            if( !serverInfo.isRaining() || serverInfo.getRainTime() < duration ) {
                serverInfo.setRainTime( duration );
                serverInfo.setRaining( true );
            }
        }
    }
    
    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "lightning" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
}