package fathertoast.specialmobs.common.entity.ghast;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

@SpecialMob
public class KingGhastEntity extends _SpecialGhastEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<KingGhastEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xE8C51A )
                .weight( BestiaryInfo.DefaultWeight.LOW )
                .uniqueTextureWithAnimation()
                .size( 1.5F, 6.0F, 6.0F )
                .addExperience( 4 )
                .regen( 30 )
                .addToAttribute( Attributes.MAX_HEALTH, 20.0 )
                .addToAttribute( Attributes.ARMOR, 10.0 )
                .addToAttribute( Attributes.ATTACK_DAMAGE, 4.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 0.6 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "King Ghast",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addSemicommonDrop( "semicommon", Items.GOLD_INGOT );
        loot.addUncommonDrop( "uncommon", Items.EMERALD );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<KingGhastEntity> getVariantFactory() { return KingGhastEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends KingGhastEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public KingGhastEntity( EntityType<? extends _SpecialGhastEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Override to change this ghast's explosion power multiplier. */
    @Override
    protected int getVariantExplosionPower( int radius ) { return Math.round( radius * 2.5F ); }
}