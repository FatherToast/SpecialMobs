package fathertoast.specialmobs.common.entity.witherskeleton;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.SkeletonSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

@SpecialMob
public class GatlingWitherSkeletonEntity extends _SpecialWitherSkeletonEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<GatlingWitherSkeletonEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xFFFF0B )
                .uniqueOverlayTexture()
                .addExperience( 2 )
                .multiplyRangedSpread( 2.0 ).multiplyRangedWalkSpeed( 0.3 ).rangedCooldown( 1 )
                .addToAttribute( Attributes.MAX_HEALTH, 10.0 )
                .addToAttribute( Attributes.ATTACK_DAMAGE, 2.0 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( MobFamily.Species<?> species ) {
        return new SkeletonSpeciesConfig( species, 1.0F, DEFAULT_SHIELD_CHANCE );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Gatling Wither Skeleton",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addGuaranteedDrop( "base", Items.ARROW, 1 );
        loot.addCommonDrop( "common", Items.GUNPOWDER );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<GatlingWitherSkeletonEntity> getVariantFactory() { return GatlingWitherSkeletonEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends GatlingWitherSkeletonEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public GatlingWitherSkeletonEntity( EntityType<? extends _SpecialWitherSkeletonEntity> entityType, Level level ) { super( entityType, level ); }
}