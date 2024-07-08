package fathertoast.specialmobs.common.entity.zombifiedpiglin;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.List;

@SpecialMob
public class VampireZombifiedPiglinEntity extends _SpecialZombifiedPiglinEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<VampireZombifiedPiglinEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x000000 ).weight( BestiaryInfo.DefaultWeight.LOW )
                .uniqueTextureBaseOnly()
                .addExperience( 4 )
                .addToAttribute( Attributes.MAX_HEALTH, 10.0 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Vampire Piglin",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addSemicommonDrop( "semicommon", Items.QUARTZ );
        
        final ItemStack stake = new ItemStack( Items.WOODEN_SWORD );
        stake.enchant( Enchantments.SMITE, Enchantments.SMITE.getMaxLevel() * 2 );
        loot.addRareDrop( "rare", stake );
    }

    // Override default - vampire is not really zombified
    @SpecialMob.EntityTagProvider
    public static List<TagKey<EntityType<?>>> getEntityTags() {
        return List.of();
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<VampireZombifiedPiglinEntity> getVariantFactory() { return VampireZombifiedPiglinEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends VampireZombifiedPiglinEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public VampireZombifiedPiglinEntity( EntityType<? extends _SpecialZombifiedPiglinEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        MobHelper.stealLife( this, target, 2.0F );
    }
    
    /** Override to modify this entity's ranged attack projectile. */
    @Override
    protected AbstractArrow getVariantArrow( AbstractArrow arrow, ItemStack arrowItem, float damageMulti ) {
        return MobHelper.tipArrow( arrow, MobEffects.HARM, 2 );
    }
    
    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt( DamageSource source, float amount ) {
        if( MobHelper.isDamageSourceIneffectiveAgainstVampires( source ) ) {
            amount = Math.min( 2.0F, amount );
        }
        return super.hurt( source, amount );
    }
}