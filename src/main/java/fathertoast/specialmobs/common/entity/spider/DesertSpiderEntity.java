package fathertoast.specialmobs.common.entity.spider;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.core.register.SMEffects;
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
public class DesertSpiderEntity extends _SpecialSpiderEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<DesertSpiderEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xE6DDAC ).theme( BestiaryInfo.Theme.DESERT )
                .uniqueTextureWithEyes()
                .size( 0.8F, 0.95F, 0.7F )
                .addExperience( 2 ).effectImmune( Effects.MOVEMENT_SLOWDOWN, SMEffects.VULNERABILITY )
                .addToAttribute( Attributes.MAX_HEALTH, 4.0 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Desert Spider",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.LEATHER );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<DesertSpiderEntity> getVariantFactory() { return DesertSpiderEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends DesertSpiderEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public DesertSpiderEntity( EntityType<? extends _SpecialSpiderEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to change the color of this entity's spit attack. */
    @Override
    protected int getVariantSpitColor() { return Effects.BLINDNESS.getColor(); }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        if( Config.MAIN.GENERAL.enableNausea.get() ) MobHelper.applyEffect( target, Effects.CONFUSION );
        MobHelper.applyEffect( target, Effects.BLINDNESS );
        MobHelper.removeNightVision( target );
        MobHelper.applyEffect( target, Effects.MOVEMENT_SLOWDOWN, 2 );
        MobHelper.applyEffect( target, SMEffects.VULNERABILITY.get(), 2 );
    }
}