package fathertoast.specialmobs.common.entity.enderman;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.ExplosionHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

@SpecialMob
public class LightningEndermanEntity extends _SpecialEndermanEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<LightningEndermanEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x4BB4B5 ).theme( BestiaryInfo.Theme.STORM )
                .uniqueTextureWithEyes()
                .addExperience( 2 ).fireImmune().waterInsensitive();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Lightning Enderman",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.REDSTONE );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<LightningEndermanEntity> getVariantFactory() { return LightningEndermanEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends LightningEndermanEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public LightningEndermanEntity( EntityType<? extends _SpecialEndermanEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        ExplosionHelper.spawnLightning( level, target.getX(), target.getY(), target.getZ() );
        for( int i = 0; i < 64; i++ ) {
            if( teleport() ) break;
        }
    }
    
    /** Called when this entity is struck by lightning. */
    @Override
    public void thunderHit( ServerLevel level, LightningBolt lightningBolt ) { }
}