package fathertoast.specialmobs.common.entity.ghast;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

@SpecialMob
public class BabyGhastEntity extends _SpecialGhastEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<BabyGhastEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xFFC0CB )
                .weight( BestiaryInfo.DefaultWeight.DISABLED )
                .size( 0.25F, 1.0F, 1.0F )
                .experience( 1 )
                .disableRangedAttack()
                .addToAttribute( Attributes.ATTACK_DAMAGE, -1.0 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Baby Ghast",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        loot.addCommonDrop( "common", Items.GUNPOWDER, 1 );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<BabyGhastEntity> getVariantFactory() { return BabyGhastEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends BabyGhastEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public BabyGhastEntity( EntityType<? extends _SpecialGhastEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** @return The sound this entity makes idly. */
    @Override
    protected SoundEvent getAmbientSound() { return null; } // There could be a lot of these, need to be less annoying
}