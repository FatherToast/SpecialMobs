package fathertoast.specialmobs.common.entity.skeleton;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.monster.StrayEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class StraySkeletonEntity extends _SpecialSkeletonEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<StraySkeletonEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0xDDEAEA, BestiaryInfo.BaseWeight.LOW );
        //TODO theme - ice
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return StrayEntity.createAttributes();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Stray",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        loot.addLootTable( "main", EntityType.STRAY.getDefaultLootTable() );
    }
    
    @SpecialMob.Constructor
    public StraySkeletonEntity( EntityType<? extends _SpecialSkeletonEntity> entityType, World world ) {
        super( entityType, world );
        xpReward += 1;
    }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( Entity target ) {
        if( target instanceof LivingEntity ) {
            final LivingEntity livingTarget = (LivingEntity) target;
            final int duration = MobHelper.getDebuffDuration( level.getDifficulty() );
            
            livingTarget.addEffect( new EffectInstance( Effects.MOVEMENT_SLOWDOWN, duration * 2 ) );
        }
    }
    
    /** Override to modify this entity's ranged attack projectile. */
    @Override
    protected AbstractArrowEntity getVariantArrow( AbstractArrowEntity arrow, ItemStack arrowItem, float damageMulti ) {
        if( arrow instanceof ArrowEntity ) {
            final int duration = MobHelper.getDebuffDuration( level.getDifficulty() );
            
            ((ArrowEntity) arrow).addEffect( new EffectInstance( Effects.MOVEMENT_SLOWDOWN, duration * 2 ) );
        }
        return arrow;
    }
    
    private static final ResourceLocation[] TEXTURES = {
            new ResourceLocation( "textures/entity/skeleton/stray.png" ),
            null,
            new ResourceLocation( "textures/entity/skeleton/stray_overlay.png" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
    
    
    //--------------- Stray Implementations ----------------
    
    /** @return The sound this entity makes idly. */
    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.STRAY_AMBIENT; }
    
    /** @return The sound this entity makes when damaged. */
    @Override
    protected SoundEvent getHurtSound( DamageSource source ) { return SoundEvents.STRAY_HURT; }
    
    /** @return The sound this entity makes when killed. */
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.STRAY_DEATH; }
    
    /** @return The sound this entity makes while walking. */
    @Override
    protected SoundEvent getStepSound() { return SoundEvents.STRAY_STEP; }
}