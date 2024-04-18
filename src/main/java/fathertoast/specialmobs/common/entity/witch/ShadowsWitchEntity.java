package fathertoast.specialmobs.common.entity.witch;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.Collection;

@SpecialMob
public class ShadowsWitchEntity extends _SpecialWitchEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<ShadowsWitchEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x000000 ).theme( BestiaryInfo.Theme.FOREST )
                .uniqueTextureWithEyes()
                .addExperience( 2 ).effectImmune( MobEffects.BLINDNESS, MobEffects.WITHER );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Witch of Shadows",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.INK_SAC );
        loot.addRareDrop( "rare", PotionUtils.setPotion( new ItemStack( Items.POTION ), Potions.NIGHT_VISION ) );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<ShadowsWitchEntity> getVariantFactory() { return ShadowsWitchEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends ShadowsWitchEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    private static final Collection<MobEffectInstance> POTION_SHADOWS = Arrays.asList(
            new MobEffectInstance( MobEffects.BLINDNESS, 300, 0 ),
            new MobEffectInstance( MobEffects.WITHER, 200, 0 )
    );
    
    public ShadowsWitchEntity( EntityType<? extends _SpecialWitchEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Override to modify potion attacks. Return an empty item stack to cancel the potion throw. */
    @Override
    protected ItemStack pickVariantThrownPotion( ItemStack originalPotion, LivingEntity target, float damageMulti, float distance ) {
        if( target.getHealth() >= 4.0F && (!target.hasEffect( MobEffects.BLINDNESS ) || !target.hasEffect( MobEffects.WITHER )) ) {
            return makeSplashPotion( POTION_SHADOWS );
        }
        return originalPotion;
    }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        final LivingEntity target = getTarget();
        if( !level().isClientSide() && isAlive() && target != null && target.hasEffect( MobEffects.BLINDNESS ) && random.nextInt( 10 ) == 0 ) {
            MobHelper.removeNightVision( target );
        }
        super.aiStep();
    }
}