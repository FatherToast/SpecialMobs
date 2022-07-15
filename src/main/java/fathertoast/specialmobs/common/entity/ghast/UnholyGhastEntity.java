package fathertoast.specialmobs.common.entity.ghast;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

@SpecialMob
public class UnholyGhastEntity extends _SpecialGhastEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<UnholyGhastEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x7AC754 ).weight( BestiaryInfo.DefaultWeight.LOW )
                .uniqueTextureWithAnimation()
                .size( 0.5F, 2.0F, 2.0F )
                .addExperience( 4 ).undead().disableRangedAttack()
                .addToAttribute( Attributes.MAX_HEALTH, 10.0 )
                .addToAttribute( Attributes.ATTACK_DAMAGE, 2.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 0.7 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Unholy Ghast",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.BONE );
        loot.addSemicommonDrop( "semicommon", Items.QUARTZ );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<UnholyGhastEntity> getVariantFactory() { return UnholyGhastEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends UnholyGhastEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public UnholyGhastEntity( EntityType<? extends _SpecialGhastEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    protected void onVariantAttack( Entity target ) {
        if( target instanceof LivingEntity ) {
            MobHelper.stealLife( this, (LivingEntity) target, 2.0F );
        }
    }
    
    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt( DamageSource source, float amount ) {
        if( MobHelper.isDamageSourceIneffectiveAgainstVampires( source ) ) {
            amount = Math.min( 2.0F, amount );
        }
        return super.hurt( source, amount );
    }
    
    /** @return This entity's creature type. */
    @Override
    public CreatureAttribute getMobType() { return CreatureAttribute.UNDEAD; }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        if( isSunBurnTick() ) {
            final ItemStack hat = getItemBySlot( EquipmentSlotType.HEAD );
            if( !hat.isEmpty() ) {
                if( hat.isDamageableItem() ) {
                    hat.setDamageValue( hat.getDamageValue() + random.nextInt( 2 ) );
                    if( hat.getDamageValue() >= hat.getMaxDamage() ) {
                        broadcastBreakEvent( EquipmentSlotType.HEAD );
                        setItemSlot( EquipmentSlotType.HEAD, ItemStack.EMPTY );
                    }
                }
            }
            else {
                setSecondsOnFire( 8 );
            }
        }
        super.aiStep();
    }
}