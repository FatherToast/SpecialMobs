package fathertoast.specialmobs.common.entity.creeper;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.ExplosionHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;

@SpecialMob
public class LightningCreeperEntity extends _SpecialCreeperEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<LightningCreeperEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x499CAE ).theme( BestiaryInfo.Theme.STORM )
                .uniqueTextureBaseOnly()
                .addExperience( 1 ).fireImmune();
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
    public static EntityType.EntityFactory<LightningCreeperEntity> getVariantFactory() { return LightningCreeperEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends LightningCreeperEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public LightningCreeperEntity( EntityType<? extends _SpecialCreeperEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Override to change this creeper's explosion power multiplier. */
    @Override
    protected float getVariantExplosionPower( float radius ) {
        return isSupercharged() || isPowered() ? super.getVariantExplosionPower( radius ) : radius / 3.0F;
    }
    
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
        if( isPowered() && level.getLevelData() instanceof ServerLevelData serverData ) {
            int duration = random.nextInt( 12000 ) + 3600;
            if( !serverData.isThundering() || serverData.getThunderTime() < duration ) {
                serverData.setThunderTime( duration );
                serverData.setThundering( true );
            }
            duration += 1200;
            if( !serverData.isRaining() || serverData.getRainTime() < duration ) {
                serverData.setRainTime( duration );
                serverData.setRaining( true );
            }
        }
    }
}