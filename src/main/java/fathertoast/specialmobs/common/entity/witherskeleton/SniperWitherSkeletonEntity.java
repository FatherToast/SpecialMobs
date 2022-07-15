package fathertoast.specialmobs.common.entity.witherskeleton;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.SkeletonSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.Items;
import net.minecraft.world.World;

@SpecialMob
public class SniperWitherSkeletonEntity extends _SpecialWitherSkeletonEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<SniperWitherSkeletonEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x486720 )
                .uniqueTextureWithOverlay()
                .addExperience( 1 )
                .multiplyRangedSpread( 0.05 ).multiplyRangedWalkSpeed( 0.3 ).multiplyRangedCooldown( 1.5F ).rangedMaxRange( 25.0 )
                .addToAttribute( Attributes.ATTACK_DAMAGE, 2.0 ).addToRangedDamage( 4.0 )
                .addToAttribute( Attributes.FOLLOW_RANGE, 16.0 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( MobFamily.Species<?> species ) {
        return new SkeletonSpeciesConfig( species, 1.0, DEFAULT_SHIELD_CHANCE );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Sniper Wither Skeleton",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addGuaranteedDrop( "base", Items.ARROW, 1 );
        loot.addClusterDrop( "uncommon", Items.SPECTRAL_ARROW, 4 );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<SniperWitherSkeletonEntity> getVariantFactory() { return SniperWitherSkeletonEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends SniperWitherSkeletonEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public SniperWitherSkeletonEntity( EntityType<? extends _SpecialWitherSkeletonEntity> entityType, World world ) { super( entityType, world ); }
}