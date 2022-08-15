package fathertoast.specialmobs.common.entity.cavespider;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.Items;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

@SpecialMob
public class PaleCaveSpiderEntity extends _SpecialCaveSpiderEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<PaleCaveSpiderEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xDED4C6 ).theme( BestiaryInfo.Theme.ICE )
                .uniqueTextureWithEyes()
                .addExperience( 1 ).effectImmune( Effects.WEAKNESS )
                .addToAttribute( Attributes.ARMOR, 15.0 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Pale Cave Spider",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addSemicommonDrop( "semicommon", Items.FERMENTED_SPIDER_EYE );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<PaleCaveSpiderEntity> getVariantFactory() { return PaleCaveSpiderEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends PaleCaveSpiderEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public PaleCaveSpiderEntity( EntityType<? extends _SpecialCaveSpiderEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to change the color of this entity's spit attack. */
    @Override
    protected int getVariantSpitColor() { return Effects.WEAKNESS.getColor(); }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        MobHelper.applyEffect( target, Effects.WEAKNESS );
        MobHelper.applyEffect( target, Effects.DIG_SLOWDOWN, 3 );
    }
}