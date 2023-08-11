package fathertoast.specialmobs.common.entity.silverfish;

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
public class ToughSilverfishEntity extends _SpecialSilverfishEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<ToughSilverfishEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xDD0E0E ).weight( BestiaryInfo.DefaultWeight.LOW )
                .uniqueTextureWithEyes()
                .size( 1.5F, 0.6F, 0.45F )
                .addExperience( 2 )
                .addToAttribute( Attributes.MAX_HEALTH, 16.0 ).addToAttribute( Attributes.ARMOR, 15.0 )
                .addToAttribute( Attributes.ATTACK_DAMAGE, 2.0 ).addToRangedDamage( 1.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 0.7 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Tough Silverfish",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.FLINT, 1 );
        loot.addRareDrop( "rare", Items.IRON_INGOT );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<ToughSilverfishEntity> getVariantFactory() { return ToughSilverfishEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends ToughSilverfishEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public ToughSilverfishEntity( EntityType<? extends _SpecialSilverfishEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Override to change the color of this entity's spit attack. */
    @Override
    protected int getVariantSpitColor() { return 0x271E3D; } // Obsidian purple
}