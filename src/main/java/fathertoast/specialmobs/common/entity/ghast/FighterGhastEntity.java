package fathertoast.specialmobs.common.entity.ghast;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.Items;
import net.minecraft.world.World;

@SpecialMob
public class FighterGhastEntity extends _SpecialGhastEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<FighterGhastEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x7A1300 )
                .uniqueTextureWithAnimation()
                .size( 0.5F, .0F, 2.0F )
                .addExperience( 1 ).disableRangedAttack()
                .addToAttribute( Attributes.MAX_HEALTH, 20.0 ).addToAttribute( Attributes.ARMOR, 10.0 )
                .addToAttribute( Attributes.ATTACK_DAMAGE, 2.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 0.8 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Fighter Ghast",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addUncommonDrop( "uncommon", Items.IRON_INGOT );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<FighterGhastEntity> getVariantFactory() { return FighterGhastEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends FighterGhastEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public FighterGhastEntity( EntityType<? extends _SpecialGhastEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    protected void onVariantAttack( Entity target ) {
        if( target instanceof LivingEntity ) {
            MobHelper.causeLifeLoss( (LivingEntity) target, 2.0F );
        }
    }
}