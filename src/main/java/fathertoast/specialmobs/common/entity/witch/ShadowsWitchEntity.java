package fathertoast.specialmobs.common.entity.witch;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.world.World;

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
                .addExperience( 2 ).effectImmune( Effects.BLINDNESS, Effects.WITHER );
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
    public static EntityType.IFactory<ShadowsWitchEntity> getVariantFactory() { return ShadowsWitchEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends ShadowsWitchEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    private static final Collection<EffectInstance> POTION_SHADOWS = Arrays.asList(
            new EffectInstance( Effects.BLINDNESS, 300, 0 ),
            new EffectInstance( Effects.WITHER, 200, 0 )
    );
    
    public ShadowsWitchEntity( EntityType<? extends _SpecialWitchEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to modify potion attacks. Return an empty item stack to cancel the potion throw. */
    @Override
    protected ItemStack pickVariantThrownPotion( ItemStack originalPotion, LivingEntity target, float damageMulti, float distance ) {
        if( target.getHealth() >= 4.0F && (!target.hasEffect( Effects.BLINDNESS ) || !target.hasEffect( Effects.WITHER )) ) {
            return makeSplashPotion( POTION_SHADOWS );
        }
        return originalPotion;
    }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        final LivingEntity target = getTarget();
        if( !level.isClientSide() && isAlive() && target != null && target.hasEffect( Effects.BLINDNESS ) && random.nextInt( 10 ) == 0 ) {
            target.removeEffect( Effects.NIGHT_VISION ); // Prevent blind + night vision combo (black screen)
        }
        super.aiStep();
    }
}