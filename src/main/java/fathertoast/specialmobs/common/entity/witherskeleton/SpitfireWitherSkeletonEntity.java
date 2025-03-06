package fathertoast.specialmobs.common.entity.witherskeleton;

import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.SkeletonSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.core.register.SMTags;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;

@SpecialMob
public class SpitfireWitherSkeletonEntity extends _SpecialWitherSkeletonEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<SpitfireWitherSkeletonEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xDC1A00 ).weight( BestiaryInfo.DefaultWeight.LOW )
                .uniqueTextureWithEyes()
                .size( 1.8F, 0.95F, 3.6F )
                .addExperience( 2 ).waterSensitive().rangedDamage( 0.0 )
                .addToAttribute( Attributes.MAX_HEALTH, 20.0 )
                .addToAttribute( Attributes.ATTACK_DAMAGE, 2.0 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( ConfigManager manager, MobFamily.Species<?> species ) {
        return new SkeletonSpeciesConfig( manager, species, 1.0F, DEFAULT_SHIELD_CHANCE );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Spitfire Wither Skeleton",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.FIRE_CHARGE );
    }

    @SpecialMob.EntityTagProvider
    public static List<TagKey<EntityType<?>>> getEntityTags() {
        return List.of( SMTags.EntityTypes.WITHER_SKELETONS, EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<SpitfireWitherSkeletonEntity> getVariantFactory() { return SpitfireWitherSkeletonEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends SpitfireWitherSkeletonEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public SpitfireWitherSkeletonEntity( EntityType<? extends _SpecialWitherSkeletonEntity> entityType, Level level ) {
        super( entityType, level );
    }

    @Override
    public float getStepHeight() {
        return 1.0F;
    }

    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        target.setSecondsOnFire( 10 );
        MobHelper.knockback( this, target, 3.0F, 1.0F );
    }
    
    /** Called to attack the target with a ranged attack. */
    @Override
    public void performRangedAttack( LivingEntity target, float damageMulti ) {
        References.LevelEvent.BLAZE_SHOOT.play( this );
        
        final float accelVariance = Mth.sqrt( distanceTo( target ) ) * 0.5F * getSpecialData().getRangedAttackSpread();
        
        for( int i = 0; i < 4; i++ ) {
            final double dX = target.getX() - getX() + getRandom().nextGaussian() * accelVariance;
            final double dY = target.getEyeY() - getEyeY();
            final double dZ = target.getZ() - getZ() + getRandom().nextGaussian() * accelVariance;
            
            final SmallFireball fireball = new SmallFireball( level(), this, dX, dY, dZ );
            fireball.setPos( fireball.getX(), getEyeY() - 0.1, fireball.getZ() );
            level().addFreshEntity( fireball );
        }
    }
    
    /** Sets this entity as a baby. */
    @Override
    public void setBaby( boolean value ) { }
    
    /** @return True if this entity is a baby. */
    @Override
    public boolean isBaby() { return false; }
}