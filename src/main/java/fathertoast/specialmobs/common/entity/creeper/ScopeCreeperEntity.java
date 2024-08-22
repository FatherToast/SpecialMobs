package fathertoast.specialmobs.common.entity.creeper;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

@SpecialMob
public class ScopeCreeperEntity extends _SpecialCreeperEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<ScopeCreeperEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xFDAF40 )
                .uniqueTextureBaseOnly()
                .addExperience( 1 )
                .effectImmune( MobEffects.BLINDNESS )
                //.effectImmune( MobEffects.BAD_EYE_SIGHT )
                .addToAttribute( Attributes.FOLLOW_RANGE, 60.0D );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Scope Creeper",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addLootTable( "common", EntityType.CREEPER.getDefaultLootTable() );
        loot.addSemicommonDrop( "semicommon", Items.SPYGLASS );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<ScopeCreeperEntity> getVariantFactory() { return ScopeCreeperEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends ScopeCreeperEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public ScopeCreeperEntity( EntityType<? extends _SpecialCreeperEntity> entityType, Level level ) { super( entityType, level ); }
}