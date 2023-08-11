package fathertoast.specialmobs.common.entity.zombifiedpiglin;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;


@SpecialMob
public class PlagueZombifiedPiglinEntity extends _SpecialZombifiedPiglinEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<PlagueZombifiedPiglinEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x8AA838 ).theme( BestiaryInfo.Theme.FOREST )
                .uniqueTextureBaseOnly()
                .addExperience( 1 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 1.1 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Plagued Piglin",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addUncommonDrop( "uncommon", Items.POISONOUS_POTATO, Items.SPIDER_EYE, Items.FERMENTED_SPIDER_EYE,
                Blocks.RED_MUSHROOM, Blocks.BROWN_MUSHROOM );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<PlagueZombifiedPiglinEntity> getVariantFactory() { return PlagueZombifiedPiglinEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends PlagueZombifiedPiglinEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public PlagueZombifiedPiglinEntity( EntityType<? extends _SpecialZombifiedPiglinEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        MobHelper.applyPlagueEffect( target, random );
    }
    
    /** Override to modify this entity's ranged attack projectile. */
    @Override
    protected AbstractArrow getVariantArrow( AbstractArrow arrow, ItemStack arrowItem, float damageMulti ) {
        return MobHelper.tipPlagueArrow( arrow, random );
    }
}