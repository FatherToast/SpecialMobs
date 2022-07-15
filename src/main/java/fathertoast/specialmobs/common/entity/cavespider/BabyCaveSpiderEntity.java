package fathertoast.specialmobs.common.entity.cavespider;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.Items;
import net.minecraft.world.World;

@SpecialMob
public class BabyCaveSpiderEntity extends _SpecialCaveSpiderEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<BabyCaveSpiderEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xFFC0CB ).weight( BestiaryInfo.DefaultWeight.DISABLED )
                .size( 0.4F, 0.6F, 0.4F )
                .experience( 1 ).disableRangedAttack()
                .multiplyAttribute( Attributes.MAX_HEALTH, 0.33 )
                .addToAttribute( Attributes.ATTACK_DAMAGE, -1.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 1.3 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Baby Cave Spider",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        loot.addCommonDrop( "common", Items.STRING, 1 );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<BabyCaveSpiderEntity> getVariantFactory() { return BabyCaveSpiderEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends BabyCaveSpiderEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public BabyCaveSpiderEntity( EntityType<? extends _SpecialCaveSpiderEntity> entityType, World world ) { super( entityType, world ); }
}