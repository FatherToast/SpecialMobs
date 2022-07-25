package fathertoast.specialmobs.common.entity.witherskeleton;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.SkeletonSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

@SpecialMob
public class SniperWitherSkeletonEntity extends _SpecialWitherSkeletonEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<SniperWitherSkeletonEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x486720 )
                .uniqueTextureWithOverlay()
                .addExperience( 1 )
                .multiplyRangedSpread( 0.05 ).multiplyRangedWalkSpeed( 0.3 ).multiplyRangedCooldown( 1.5F ).rangedMaxRange( 25.0 )
                .addToAttribute( Attributes.ATTACK_DAMAGE, 2.0 ).addToRangedDamage( 4.0 )
                .addToAttribute( Attributes.FOLLOW_RANGE, 16.0 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( MobFamily.Species<?> species ) {
        return new SkeletonSpeciesConfig( species, 1.0, DEFAULT_SHIELD_CHANCE );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Sniper Wither Skeleton",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addGuaranteedDrop( "base", Items.ARROW, 1 );
        loot.addClusterDrop( "uncommon", Items.SPECTRAL_ARROW, 4 );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<SniperWitherSkeletonEntity> getVariantFactory() { return SniperWitherSkeletonEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends SniperWitherSkeletonEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public SniperWitherSkeletonEntity( EntityType<? extends _SpecialWitherSkeletonEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Called to attack the target with a ranged attack. */
    @Override
    public void performRangedAttack( LivingEntity target, float damageMulti ) {
        // Would be nice if we could lead the target, maybe someday when we can more easily read player velocity
        final double dX = target.getX() - getX();
        final double dY = target.getY( 0.5 ) - getEyeY() + 0.1;
        final double dZ = target.getZ() - getZ();
        final double dH = MathHelper.sqrt( dX * dX + dZ * dZ );
        
        final double g = 0.05; // Gravitational acceleration for AbstractArrowEntity
        final float v = 1.6F;
        final double radical = v * v * v * v - g * (g * dH * dH + 2 * dY * v * v);
        if( radical < 0.0 ) {
            // No firing solution, just fall back to the default
            super.performRangedAttack( target, damageMulti );
            return;
        }
        final double angle = Math.atan( (v * v - Math.sqrt( radical )) / (g * dH) ); // Use the flatter trajectory (-sqrt)
        final double vY = Math.sin( angle );
        final double vH = Math.cos( angle );
        
        final ItemStack arrowItem = getProjectile( getItemInHand( ProjectileHelper.getWeaponHoldingHand(
                this, item -> item instanceof BowItem ) ) );
        AbstractArrowEntity arrow = getArrow( arrowItem, damageMulti );
        if( getMainHandItem().getItem() instanceof BowItem )
            arrow = ((BowItem) getMainHandItem().getItem()).customArrow( arrow );
        
        arrow.shoot( dX / dH * vH, vY, dZ / dH * vH, v,
                getSpecialData().getRangedAttackSpread() * (14 - 4 * level.getDifficulty().getId()) );
        
        playSound( SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F) );
        level.addFreshEntity( arrow );
    }
    
    /** Override to modify this entity's ranged attack projectile. */
    @Override
    protected AbstractArrowEntity getVariantArrow( AbstractArrowEntity arrow, ItemStack arrowItem, float damageMulti ) {
        arrow.setKnockback( arrow.knockback + 2 );
        return arrow;
    }
}