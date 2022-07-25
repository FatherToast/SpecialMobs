package fathertoast.specialmobs.common.entity.blaze;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.projectile.SnowballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.UUID;

@SpecialMob
public class ConflagrationBlazeEntity extends _SpecialBlazeEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<ConflagrationBlazeEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xFFF87E ).weight( BestiaryInfo.DefaultWeight.LOW )
                .uniqueTextureBaseOnly()
                .size( 1.5F, 0.9F, 2.7F )
                .addExperience( 4 ).regen( 20 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Conflagration",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.FIRE_CHARGE );
        loot.addRareDrop( "rare", PotionUtils.setPotion( new ItemStack( Items.POTION ), Potions.FIRE_RESISTANCE ) );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<ConflagrationBlazeEntity> getVariantFactory() { return ConflagrationBlazeEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends ConflagrationBlazeEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** The damage boost to apply from growth level. */
    private static final AttributeModifier DAMAGE_BOOST = new AttributeModifier( UUID.fromString( "70457CAB-AA09-4E1C-B44B-99DD4A2A836D" ),
            "Feeding damage boost", 1.0, AttributeModifier.Operation.ADDITION );
    
    /** The level of increased attack damage gained. */
    private int growthLevel;
    
    public ConflagrationBlazeEntity( EntityType<? extends _SpecialBlazeEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        MobHelper.stealLife( this, target, 2.0F );
    }
    
    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt( DamageSource source, float amount ) {
        if( isInvulnerableTo( source ) || fireImmune() && source.isFire() ) return false;
        
        if( !source.isExplosion() && !source.isMagic() && !DamageSource.DROWN.getMsgId().equals( source.getMsgId() ) &&
                !(source.getDirectEntity() instanceof SnowballEntity) ) {
            
            if( !level.isClientSide() && growthLevel < 7 ) {
                growthLevel++;
                updateFeedingLevels();
            }
            amount /= 2.0F;
        }
        return super.hurt( source, amount );
    }
    
    /** Recalculates the modifiers associated with this entity's feeding level counters. */
    private void updateFeedingLevels() {
        if( level != null && !level.isClientSide ) {
            final int cooldownReduction = MathHelper.floor( getConfig().GENERAL.rangedAttackCooldown.get() * growthLevel * 0.1 );
            getSpecialData().setRangedAttackCooldown( getConfig().GENERAL.rangedAttackCooldown.get() - cooldownReduction );
            getSpecialData().setRangedAttackMaxCooldown( getConfig().GENERAL.rangedAttackMaxCooldown.get() - cooldownReduction );
            
            fireballBurstCount = getConfig().BLAZES.fireballBurstCount.get();
            if( growthLevel >= 3 ) fireballBurstCount++;
            if( growthLevel >= 7 ) fireballBurstCount++;
            
            final ModifiableAttributeInstance damage = getAttribute( Attributes.ATTACK_DAMAGE );
            //noinspection ConstantConditions
            damage.removeModifier( DAMAGE_BOOST.getId() );
            if( growthLevel > 0 ) {
                damage.addPermanentModifier( new AttributeModifier( DAMAGE_BOOST.getId(), DAMAGE_BOOST.getName(),
                        DAMAGE_BOOST.getAmount() * growthLevel, DAMAGE_BOOST.getOperation() ) );
            }
        }
    }
    
    /** Override to save data to this entity's NBT data. */
    @Override
    public void addVariantSaveData( CompoundNBT saveTag ) {
        saveTag.putByte( References.TAG_GROWTH_LEVEL, (byte) growthLevel );
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundNBT saveTag ) {
        if( saveTag.contains( References.TAG_GROWTH_LEVEL, References.NBT_TYPE_NUMERICAL ) )
            growthLevel = saveTag.getByte( References.TAG_GROWTH_LEVEL );
        updateFeedingLevels();
    }
}