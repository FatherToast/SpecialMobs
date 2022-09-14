package fathertoast.specialmobs.common.entity.silverfish;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.ai.goal.SpecialLeapAtTargetGoal;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.Items;
import net.minecraft.world.World;

@SpecialMob
public class FlyingSilverfishEntity extends _SpecialSilverfishEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<FlyingSilverfishEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x6388B2 ).theme( BestiaryInfo.Theme.MOUNTAIN )
                .uniqueTextureBaseOnly()
                .addExperience( 2 ).fallImmune().disableRangedAttack()
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 1.2 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Flying Silverfish",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.FEATHER, 1 );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<FlyingSilverfishEntity> getVariantFactory() { return FlyingSilverfishEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends FlyingSilverfishEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public FlyingSilverfishEntity( EntityType<? extends _SpecialSilverfishEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        goalSelector.addGoal( 3, new SpecialLeapAtTargetGoal(
                this, 10, 6.0F, 12.0F, 2.0F, 2.0F ) );
    }
}