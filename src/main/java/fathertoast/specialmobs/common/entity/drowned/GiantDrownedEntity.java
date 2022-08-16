package fathertoast.specialmobs.common.entity.drowned;

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
import net.minecraft.world.World;

@SpecialMob
public class GiantDrownedEntity extends _SpecialDrownedEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<GiantDrownedEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x799C65 )
                .size( 1.5F, 0.9F, 2.95F )
                .addExperience( 1 )
                .addToAttribute( Attributes.MAX_HEALTH, 20.0 )
                .addToAttribute( Attributes.ATTACK_DAMAGE, 2.0 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Drowned Giant",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addGuaranteedDrop( "base", Items.ROTTEN_FLESH, 2 );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<GiantDrownedEntity> getVariantFactory() { return GiantDrownedEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends GiantDrownedEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public GiantDrownedEntity( EntityType<? extends _SpecialDrownedEntity> entityType, World world ) {
        super( entityType, world );
        maxUpStep = 1.0F;
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        MobHelper.knockback( this, target, 4.0F, 0.5F );
    }
    
    /** Sets this entity as a baby. */
    @Override
    public void setBaby( boolean value ) { }
    
    /** @return True if this entity is a baby. */
    @Override
    public boolean isBaby() { return false; }
}