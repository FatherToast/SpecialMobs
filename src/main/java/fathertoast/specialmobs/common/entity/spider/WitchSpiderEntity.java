package fathertoast.specialmobs.common.entity.spider;

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
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.world.World;

@SpecialMob
public class WitchSpiderEntity extends _SpecialSpiderEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<WitchSpiderEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xDD0E0E ).weight( BestiaryInfo.DefaultWeight.LOW ).theme( BestiaryInfo.Theme.FOREST )
                .uniqueTextureWithEyes()
                .addExperience( 2 )
                .spitAttackMultiplied( 0.1, 1.0, 2.0F, 1.0 )
                .addToAttribute( Attributes.ARMOR, 15.0 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Witch Spider",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addLootTable( "common", EntityType.WITCH.getDefaultLootTable() );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<WitchSpiderEntity> getVariantFactory() { return WitchSpiderEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends WitchSpiderEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public WitchSpiderEntity( EntityType<? extends _SpecialSpiderEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( Entity target ) {
        if( target instanceof LivingEntity ) {
            ((LivingEntity) target).addEffect( MobHelper.nextWitchSpiderEffect( random, level, true ) );
            MobHelper.stealBuffEffect( this, (LivingEntity) target );
        }
    }
    
    /** @return True if the effect can be applied to this entity. */
    @Override
    public boolean canBeAffected( EffectInstance effect ) {
        // Witch spider is immune to debuffs
        return effect.getEffect().getCategory() != EffectType.HARMFUL && super.canBeAffected( effect );
    }
}