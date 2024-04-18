package fathertoast.specialmobs.common.entity.witch;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.Collections;

@SpecialMob
public class DominationWitchEntity extends _SpecialWitchEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<DominationWitchEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xFFF87E ).weight( BestiaryInfo.DefaultWeight.LOW )
                .uniqueTextureBaseOnly()
                .addExperience( 2 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 0.8 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Witch of Domination",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.EXPERIENCE_BOTTLE );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<DominationWitchEntity> getVariantFactory() { return DominationWitchEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends DominationWitchEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    private static final Collection<MobEffectInstance> LEVITATION_EFFECTS = Collections.singletonList(
            new MobEffectInstance( MobEffects.LEVITATION, 140, 0 ) );
    
    /** Ticks before this witch can use its pull ability. */
    private int pullDelay;
    
    public DominationWitchEntity( EntityType<? extends _SpecialWitchEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Override to modify potion attacks. Return an empty item stack to cancel the potion throw. */
    @Override
    protected ItemStack pickVariantThrownPotion( ItemStack originalPotion, LivingEntity target, float damageMulti, float distance ) {
        if( !target.hasEffect( MobEffects.WEAKNESS ) ) {
            return makeSplashPotion( Potions.WEAKNESS );
        }
        else if( distance > 5.0F && !target.hasEffect( MobEffects.LEVITATION ) && random.nextFloat() < 0.5F ) {
            return makeSplashPotion( LEVITATION_EFFECTS );
        }
        return originalPotion;
    }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        final LivingEntity target = getTarget();
        if( !level().isClientSide() && isAlive() && pullDelay-- <= 0 && target != null && random.nextInt( 20 ) == 0 ) {
            
            // Pull the player toward this entity if they are vulnerable
            final double distanceSq = target.distanceToSqr( this );
            if( distanceSq > 100.0 && distanceSq < 196.0 &&
                    (target.hasEffect( MobEffects.WEAKNESS ) || target.hasEffect( MobEffects.LEVITATION )) && hasLineOfSight( target ) ) {
                pullDelay = 100;
                
                playSound( SoundEvents.FISHING_BOBBER_RETRIEVE, 1.0F, 0.4F / (random.nextFloat() * 0.4F + 0.8F) );
                MobHelper.pull( this, target, 0.32 );
            }
        }
        super.aiStep();
    }
}