package fathertoast.specialmobs.common.entity.zombie;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.MadScientistZombieSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.core.register.SMItems;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.entity.ai.IAmmoUser;
import fathertoast.specialmobs.common.entity.ai.goal.ChargeCreeperGoal;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.BiPredicate;

@SpecialMob
public class MadScientistZombieEntity extends _SpecialZombieEntity implements IAmmoUser {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<MadScientistZombieEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xDED4C6 ).weight( BestiaryInfo.DefaultWeight.LOW )
                .uniqueTextureBaseOnly()
                .addExperience( 2 ).disableRangedAttack();
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( MobFamily.Species<?> species ) {
        return new MadScientistZombieSpeciesConfig( species, 0.0, 0.0, 1, 3 );
    }
    
    /** @return This entity's species config. */
    @Override
    public MadScientistZombieSpeciesConfig getConfig() { return (MadScientistZombieSpeciesConfig) getSpecies().config; }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Mad Scientist Zombie",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addRareDrop( "rare", SMItems.SYRINGE.get() );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<MadScientistZombieEntity> getVariantFactory() { return MadScientistZombieEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends MadScientistZombieEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    private static final BiPredicate<MadScientistZombieEntity, ? super CreeperEntity> CHARGE_CREEPER_TARGET = ( madman, creeper ) ->
            creeper.isAlive() && !creeper.isPowered() && madman.getSensing().canSee( creeper );
    
    /** The number of creepers this madman can charge. */
    private int chargeCount;
    
    public MadScientistZombieEntity( EntityType<? extends _SpecialZombieEntity> entityType, World world ) {
        super( entityType, world );
        chargeCount = getConfig().MAD_SCIENTIST.chargeCount.next( random );
    }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        AIHelper.insertGoal( goalSelector, 2, new ChargeCreeperGoal<>( this, 1.25, 20.0, CHARGE_CREEPER_TARGET ) );
    }
    
    /** Override to change this entity's attack goal priority. */
    @Override
    protected int getVariantAttackPriority() { return super.getVariantAttackPriority() + 1; }
    
    /** Override to change starting equipment or stats. */
    @Override
    public void finalizeVariantSpawn( IServerWorld world, DifficultyInstance difficulty, @Nullable SpawnReason spawnReason,
                                      @Nullable ILivingEntityData groupData ) {
        setItemSlot( EquipmentSlotType.MAINHAND, new ItemStack( SMItems.SYRINGE.get() ) );
        setDropChance( EquipmentSlotType.MAINHAND, 0.0F );
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( Entity target ) {
        if( target instanceof LivingEntity && hasAmmo() ) {
            final LivingEntity livingTarget = (LivingEntity) target;
            final int duration = MobHelper.getDebuffDuration( level.getDifficulty() );
            
            livingTarget.addEffect( new EffectInstance( Effects.POISON, duration ) );
        }
    }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        super.aiStep();
        
        // We can't do this when the last charge is used, since it would change the AI during the AI loop
        if( chargeCount <= 0 && getItemBySlot( EquipmentSlotType.MAINHAND ).getItem() == SMItems.SYRINGE.get() ) {
            broadcastBreakEvent( EquipmentSlotType.MAINHAND );
            setItemSlot( EquipmentSlotType.MAINHAND, ItemStack.EMPTY );
        }
    }
    
    /** @return True if this entity has ammo to use. */
    @Override
    public boolean hasAmmo() { return chargeCount > 0; }
    
    /** Consumes ammo for a single use. */
    @Override
    public void consumeAmmo() { chargeCount--; }
    
    /** Override to save data to this entity's NBT data. */
    @Override
    public void addVariantSaveData( CompoundNBT saveTag ) {
        saveTag.putByte( References.TAG_AMMO, (byte) chargeCount );
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundNBT saveTag ) {
        if( saveTag.contains( References.TAG_AMMO, References.NBT_TYPE_NUMERICAL ) )
            chargeCount = saveTag.getByte( References.TAG_AMMO );
    }
}