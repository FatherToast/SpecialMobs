package fathertoast.specialmobs.common.entity.zombifiedpiglin;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

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
    
    @SpecialMob.Factory
    public static EntityType.IFactory<VampireZombifiedPiglinEntity> getVariantFactory() { return VampireZombifiedPiglinEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends VampireZombifiedPiglinEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public VampireZombifiedPiglinEntity( EntityType<? extends _SpecialZombifiedPiglinEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( Entity target ) {
        if( target instanceof LivingEntity ) {
            MobHelper.stealLife( this, (LivingEntity) target, 2.0F );
        }
    }
    
    /** Override to modify this entity's ranged attack projectile. */
    @Override
    protected AbstractArrowEntity getVariantArrow( AbstractArrowEntity arrow, ItemStack arrowItem, float damageMulti ) {
        if( arrow instanceof ArrowEntity ) {
            ((ArrowEntity) arrow).addEffect( new EffectInstance( Effects.HARM, 0, 1 ) );
        }
        return arrow;
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