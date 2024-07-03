package fathertoast.specialmobs.common.entity.cavespider;

import fathertoast.crust.api.ICrustApi;
import fathertoast.crust.api.lib.CrustObjects;
import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

@SpecialMob
public class DesertCaveSpiderEntity extends _SpecialCaveSpiderEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<DesertCaveSpiderEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xE6DDAC ).theme( BestiaryInfo.Theme.DESERT )
                .uniqueTextureWithEyes()
                .size( 0.6F, 0.7F, 0.5F )
                .addExperience( 2 ).effectImmune( MobEffects.MOVEMENT_SLOWDOWN, ICrustApi.MOD_ID + ":" + CrustObjects.ID.VULNERABILITY )
                .addToAttribute( Attributes.MAX_HEALTH, 4.0 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Desert Cave Spider",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.LEATHER );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<DesertCaveSpiderEntity> getVariantFactory() { return DesertCaveSpiderEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends DesertCaveSpiderEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public DesertCaveSpiderEntity( EntityType<? extends _SpecialCaveSpiderEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Override to change the color of this entity's spit attack. */
    @Override
    protected int getVariantSpitColor() { return MobEffects.BLINDNESS.getColor(); }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        if( Config.MAIN.GENERAL.enableNausea.get() ) MobHelper.applyEffect( target, MobEffects.CONFUSION );
        MobHelper.applyEffect( target, MobEffects.BLINDNESS );
        MobHelper.removeNightVision( target );
        MobHelper.applyEffect( target, MobEffects.MOVEMENT_SLOWDOWN, 2 );
        MobHelper.applyEffect( target, CrustObjects.vulnerability() );
    }
}