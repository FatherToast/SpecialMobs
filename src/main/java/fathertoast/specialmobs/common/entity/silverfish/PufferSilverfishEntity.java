package fathertoast.specialmobs.common.entity.silverfish;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.entity.ai.goal.AmphibiousGoToWaterGoal;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.item.Items;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

@SpecialMob
public class PufferSilverfishEntity extends AmphibiousSilverfishEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<PufferSilverfishEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xE6E861 ).weight( BestiaryInfo.DefaultWeight.LOW ).theme( BestiaryInfo.Theme.WATER )
                .uniqueTextureBaseOnly()
                .addExperience( 1 ).drownImmune().effectImmune( Effects.POISON );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "\"Pufferfish\"",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addUncommonDrop( "uncommon", Items.PUFFERFISH );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<PufferSilverfishEntity> getVariantFactory() { return PufferSilverfishEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends PufferSilverfishEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public PufferSilverfishEntity( EntityType<? extends _SpecialSilverfishEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        AIHelper.removeGoals( goalSelector, SwimGoal.class );
        AIHelper.insertGoal( goalSelector, 5, new AmphibiousGoToWaterGoal<>( this, 1.0 ).alwaysEnabled() );
    }
    
    /** Override to change the color of this entity's spit attack. */
    @Override
    protected int getVariantSpitColor() { return 0x385DC6; } // Water blue
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        MobHelper.applyEffect( target, Effects.POISON );
    }
    
    /** @return Water drag coefficient. */
    @Override
    protected float getWaterSlowDown() { return 0.95F; }
}