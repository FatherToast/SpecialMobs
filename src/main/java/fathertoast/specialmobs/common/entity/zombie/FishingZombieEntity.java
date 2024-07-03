package fathertoast.specialmobs.common.entity.zombie;

import fathertoast.crust.api.config.common.ConfigManager;
import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.config.species.ZombieSpeciesConfig;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.entity.ai.IAngler;
import fathertoast.specialmobs.common.entity.ai.goal.AnglerGoal;
import fathertoast.specialmobs.common.entity.drowned.FishingDrownedEntity;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootEntryItemBuilder;
import fathertoast.specialmobs.datagen.loot.LootHelper;
import fathertoast.specialmobs.datagen.loot.LootPoolBuilder;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;

@SpecialMob
public class FishingZombieEntity extends _SpecialZombieEntity implements IAngler {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<FishingZombieEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x2D41F4 ).weight( BestiaryInfo.DefaultWeight.LOW ).theme( BestiaryInfo.Theme.FISHING )
                .addExperience( 2 ).drownImmune().fluidPushImmune()
                .convertRangedAttackToFishing( 1.0, 40, 15.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 0.8 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( ConfigManager manager, MobFamily.Species<?> species ) {
        return new ZombieSpeciesConfig( manager, species, 0.0, 0.0 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Fishing Zombie",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addPool( new LootPoolBuilder( "common" )
                .addEntry( new LootEntryItemBuilder( Items.COD ).setCount( 0, 2 ).addLootingBonus( 0, 1 ).smeltIfBurning().toLootEntry() )
                .toLootPool() );
        loot.addPool( new LootPoolBuilder( "semicommon" )
                .addEntry( new LootEntryItemBuilder( Items.SALMON ).setCount( 0, 1 ).addLootingBonus( 0, 1 ).smeltIfBurning().toLootEntry() )
                .toLootPool() );
        loot.addPool( new LootPoolBuilder( "rare" ).addConditions( LootHelper.RARE_CONDITIONS )
                .addEntry( new LootEntryItemBuilder( Items.FISHING_ROD ).enchant( 30, true ).toLootEntry() )
                .toLootPool() );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<FishingZombieEntity> getVariantFactory() { return FishingZombieEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends FishingZombieEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public FishingZombieEntity( EntityType<? extends _SpecialZombieEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        AIHelper.replaceWaterAvoidingRandomWalking( this, 1.0 );
        
        goalSelector.addGoal( getVariantAttackPriority(), new ZombieAttackGoal( this, 1.0, false ) );
        goalSelector.addGoal( getVariantAttackPriority(), new AnglerGoal<>( this ) );
    }
    
    /** Called to set this entity's attack AI based on current equipment. */
    @Override
    public void reassessWeaponGoal() { } // Disable bow use
    
    /** Override to change starting equipment or stats. */
    @Override
    public void finalizeVariantSpawn( ServerLevelAccessor level, DifficultyInstance difficulty, @Nullable MobSpawnType spawnType,
                                     @Nullable SpawnGroupData groupData ) {
        setItemSlot( EquipmentSlot.MAINHAND, new ItemStack( Items.FISHING_ROD ) );
        if( getItemBySlot( EquipmentSlot.FEET ).isEmpty() ) {
            ItemStack booties = new ItemStack( Items.LEATHER_BOOTS );
            ((DyeableArmorItem) booties.getItem()).setColor( booties, 0xFFFF00 );
            setItemSlot( EquipmentSlot.FEET, booties );
        }
        setCanPickUpLoot( false );
    }
    
    /** Override to change the entity this converts to when drowned. */
    @Override
    protected EntityType<? extends Zombie> getVariantConversionType() { return FishingDrownedEntity.SPECIES.entityType.get(); }
    
    
    //--------------- IAngler Implementations ----------------
    
    /** The parameter for baby status. */
    private static final EntityDataAccessor<Boolean> IS_LINE_OUT = SynchedEntityData.defineId( FishingZombieEntity.class, EntityDataSerializers.BOOLEAN );
    
    /** Called from the Entity.class constructor to define data watcher variables. */
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define( IS_LINE_OUT, false );
    }
    
    /** Sets this angler's line as out (or in). */
    @Override
    public void setLineOut( boolean value ) { getEntityData().set( IS_LINE_OUT, value ); }
    
    /** @return Whether this angler's line is out. */
    @Override
    public boolean isLineOut() { return getEntityData().get( IS_LINE_OUT ); }
    
    /** @return The item equipped in a particular slot. */
    @Override
    public ItemStack getItemBySlot( EquipmentSlot slot ) {
        // Display a stick in place of the "cast fishing rod" when the fancy render is disabled
        if( level().isClientSide() && !Config.MAIN.GENERAL.fancyFishingMobs.get() && EquipmentSlot.MAINHAND.equals( slot ) ) {
            final ItemStack held = super.getItemBySlot( slot );
            if( held.getItem() == Items.FISHING_ROD && isLineOut() ) {
                return new ItemStack( Items.STICK );
            }
            return held;
        }
        return super.getItemBySlot( slot );
    }
}