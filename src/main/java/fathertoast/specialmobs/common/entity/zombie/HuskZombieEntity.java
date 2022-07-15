package fathertoast.specialmobs.common.entity.zombie;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.monster.HuskEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

@SpecialMob
public class HuskZombieEntity extends _SpecialZombieEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<HuskZombieEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xE6CC94 ).weight( BestiaryInfo.DefaultWeight.LOW ).theme( BestiaryInfo.Theme.DESERT )
                .vanillaTextureBaseOnly( "textures/entity/zombie/husk.png" )
                .size( 1.0625F, 0.6F, 1.95F )
                .addExperience( 1 );
    }
    
    @SpecialMob.AttributeSupplier
    public static AttributeModifierMap.MutableAttribute createAttributes() { return HuskEntity.createAttributes(); }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Husk",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        loot.addLootTable( "main", EntityType.HUSK.getDefaultLootTable() );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<HuskZombieEntity> getVariantFactory() { return HuskZombieEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends HuskZombieEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public HuskZombieEntity( EntityType<? extends _SpecialZombieEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( Entity target ) {
        if( target instanceof LivingEntity ) {
            final LivingEntity livingTarget = (LivingEntity) target;
            final int duration = MobHelper.getDebuffDuration( level.getDifficulty() );
            
            livingTarget.addEffect( new EffectInstance( Effects.HUNGER, duration ) );
        }
    }
    
    /** Override to modify this entity's ranged attack projectile. */
    @Override
    protected AbstractArrowEntity getVariantArrow( AbstractArrowEntity arrow, ItemStack arrowItem, float damageMulti ) {
        if( arrow instanceof ArrowEntity ) {
            final int duration = MobHelper.getDebuffDuration( level.getDifficulty() );
            
            ((ArrowEntity) arrow).addEffect( new EffectInstance( Effects.HUNGER, duration ) );
        }
        return arrow;
    }
    
    
    //--------------- Husk Implementations ----------------
    
    /** @return True if this zombie burns in sunlight. */
    @Override
    protected boolean isSunSensitive() { return false; }
    
    /** @return The sound this entity makes idly. */
    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.HUSK_AMBIENT; }
    
    /** @return The sound this entity makes when damaged. */
    @Override
    protected SoundEvent getHurtSound( DamageSource source ) { return SoundEvents.HUSK_HURT; }
    
    /** @return The sound this entity makes when killed. */
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.HUSK_DEATH; }
    
    /** @return The sound this entity makes while walking. */
    @Override
    protected SoundEvent getStepSound() { return SoundEvents.HUSK_STEP; }
}