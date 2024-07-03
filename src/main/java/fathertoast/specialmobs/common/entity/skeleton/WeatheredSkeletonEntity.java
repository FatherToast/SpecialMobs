package fathertoast.specialmobs.common.entity.skeleton;

import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.SkeletonSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;

@SpecialMob
public class WeatheredSkeletonEntity extends _SpecialSkeletonEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<WeatheredSkeletonEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xE6CC94 ).theme( BestiaryInfo.Theme.DESERT )
                .uniqueTextureWithOverlay()
                .addExperience( 1 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 1.2 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( ConfigManager manager, MobFamily.Species<?> species ) {
        return new SkeletonSpeciesConfig( manager, species, 0.5, 0.0 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Weathered Skeleton",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addClusterDrop( "uncommon", Items.GOLD_NUGGET, 4 );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<WeatheredSkeletonEntity> getVariantFactory() { return WeatheredSkeletonEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends WeatheredSkeletonEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public WeatheredSkeletonEntity( EntityType<? extends _SpecialSkeletonEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Override to change starting equipment or stats. */
    public void finalizeVariantSpawn( ServerLevelAccessor level, DifficultyInstance difficulty, @Nullable MobSpawnType spawnType,
                                     @Nullable SpawnGroupData groupData ) {
        if( getItemBySlot( EquipmentSlot.MAINHAND ).getItem() == Items.IRON_SWORD ) {
            setItemSlot( EquipmentSlot.MAINHAND, new ItemStack( Items.GOLDEN_SWORD ) );
            
            if( getItemBySlot( EquipmentSlot.OFFHAND ).isEmpty() ) {
                setItemSlot( EquipmentSlot.OFFHAND, new ItemStack( Items.GOLDEN_SWORD ) );
            }
        }
        
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        MobHelper.applyEffect( target, MobEffects.HUNGER );
    }
    
    /** Override to modify this entity's ranged attack projectile. */
    @Override
    protected AbstractArrow getVariantArrow( AbstractArrow arrow, ItemStack arrowItem, float damageMulti ) {
        return MobHelper.tipArrow( arrow, MobEffects.HUNGER );
    }
    
    /** @return Called each tick to check if the sun should set this entity on fire. */
    @Override
    protected boolean isSunBurnTick() { return false; }
}