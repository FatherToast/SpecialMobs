package fathertoast.specialmobs.common.entity.silverfish;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;

@SpecialMob
public class FireSilverfishEntity extends _SpecialSilverfishEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<FireSilverfishEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xDFA21B ).weight( BestiaryInfo.DefaultWeight.LOW ).theme( BestiaryInfo.Theme.FIRE )
                .uniqueTextureWithEyes()
                .addExperience( 2 ).fireImmune();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Firebrat",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.FIRE_CHARGE );
        loot.addUncommonDrop( "uncommon", Items.COAL );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<FireSilverfishEntity> getVariantFactory() { return FireSilverfishEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends FireSilverfishEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    private byte auraCooldown = 20;
    
    public FireSilverfishEntity( EntityType<? extends _SpecialSilverfishEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Override to change the color of this entity's spit attack. */
    @Override
    protected int getVariantSpitColor() { return 0xDFA21B; } // Fire orange
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        target.setSecondsOnFire( 5 );
    }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        super.aiStep();
        
        if( auraCooldown-- <= 0 ) {
            auraCooldown = 30;
            pulseAura();
        }
    }
    
    /** Applies this entity's aura effect. */
    private void pulseAura() {
        final List<Silverfish> friends = level.getEntitiesOfClass( Silverfish.class, getBoundingBox().inflate( 7.0 ) );
        for( Silverfish cutie : friends ) {
            if( cutie.isAlive() && !cutie.fireImmune() ) {
                cutie.addEffect( new MobEffectInstance( MobEffects.FIRE_RESISTANCE, 35 + random.nextInt( 16 ),
                        0, true, true ) );
            }
        }
    }
}