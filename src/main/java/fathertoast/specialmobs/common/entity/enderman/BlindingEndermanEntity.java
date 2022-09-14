package fathertoast.specialmobs.common.entity.enderman;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

@SpecialMob
public class BlindingEndermanEntity extends _SpecialEndermanEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<BlindingEndermanEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xFFFFFF ).theme( BestiaryInfo.Theme.FOREST )
                .uniqueTextureWithEyes()
                .addExperience( 1 ).effectImmune( Effects.BLINDNESS );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Blinding Enderman",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.INK_SAC );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<BlindingEndermanEntity> getVariantFactory() { return BlindingEndermanEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends BlindingEndermanEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public BlindingEndermanEntity( EntityType<? extends _SpecialEndermanEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        super.aiStep();
        
        // Apply blinding effect while near target
        final LivingEntity target = getTarget();
        if( target != null && distanceToSqr( target ) < 100.0 ) {
            MobHelper.applyDurationEffect( target, Effects.BLINDNESS, 50 );
            MobHelper.removeNightVision( target );
        }
    }
}