package fathertoast.specialmobs.common.entity.drowned;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.core.register.SMEffects;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.entity.ai.goal.AmphibiousGoToWaterGoal;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

@SpecialMob
public class AbyssalDrownedEntity extends _SpecialDrownedEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<AbyssalDrownedEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x223844 ).weight( BestiaryInfo.DefaultWeight.LOW )
                .uniqueTexturesAll()
                .addExperience( 2 ).effectImmune( SMEffects.WEIGHT.get(), MobEffects.LEVITATION )
                .addToAttribute( Attributes.MAX_HEALTH, 20.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 1.2 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Abyssal Drowned",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addUncommonDrop( "uncommon", Items.GOLD_NUGGET, Items.PRISMARINE_SHARD, Items.PRISMARINE_CRYSTALS );
        loot.addRareDrop( "rare", Items.GOLD_INGOT );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<AbyssalDrownedEntity> getVariantFactory() { return AbyssalDrownedEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends AbyssalDrownedEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public AbyssalDrownedEntity( EntityType<? extends _SpecialDrownedEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        // Disable all 'night time' behavior changes except for targeting
        AIHelper.removeGoals( goalSelector, 1 ); // DrownedEntity.GoToWaterGoal
        goalSelector.addGoal( 1, new AmphibiousGoToWaterGoal( this, 1.0 ).alwaysEnabled() );
        AIHelper.removeGoals( goalSelector, 5 ); // DrownedEntity.GoToBeachGoal
        AIHelper.removeGoals( goalSelector, 6 ); // DrownedEntity.SwimUpGoal
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        MobHelper.applyEffect( target, SMEffects.WEIGHT.get(), 2 );
    }
}