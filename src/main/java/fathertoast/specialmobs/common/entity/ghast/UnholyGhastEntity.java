package fathertoast.specialmobs.common.entity.ghast;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

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
    public static EntityType.EntityFactory<UnholyGhastEntity> getVariantFactory() { return UnholyGhastEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends UnholyGhastEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public UnholyGhastEntity( EntityType<? extends _SpecialGhastEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        MobHelper.stealLife( this, target, 2.0F );
    }
    
    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt( DamageSource source, float amount ) {
        if( MobHelper.isDamageSourceIneffectiveAgainstVampires( source ) ) {
            amount = Math.min( 2.0F, amount );
        }
        amount += MobHelper.getVampireDamageBonus( source );
        return super.hurt( source, amount );
    }
    
    /** @return This entity's creature type. */
    @Override
    public MobType getMobType() { return MobType.UNDEAD; }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        if( isSunBurnTick() ) {
            final ItemStack hat = getItemBySlot( EquipmentSlot.HEAD );
            if( !hat.isEmpty() ) {
                if( hat.isDamageableItem() ) {
                    hat.setDamageValue( hat.getDamageValue() + random.nextInt( 2 ) );
                    if( hat.getDamageValue() >= hat.getMaxDamage() ) {
                        broadcastBreakEvent( EquipmentSlot.HEAD );
                        setItemSlot( EquipmentSlot.HEAD, ItemStack.EMPTY );
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