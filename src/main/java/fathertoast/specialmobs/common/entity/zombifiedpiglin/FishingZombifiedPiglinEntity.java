package fathertoast.specialmobs.common.entity.zombifiedpiglin;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.config.species.ZombieSpeciesConfig;
import fathertoast.specialmobs.common.entity.ai.IAngler;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootEntryItemBuilder;
import fathertoast.specialmobs.datagen.loot.LootHelper;
import fathertoast.specialmobs.datagen.loot.LootPoolBuilder;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.IDyeableArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;

@SpecialMob
public class FishingZombifiedPiglinEntity extends _SpecialZombifiedPiglinEntity implements IAngler {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<FishingZombifiedPiglinEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x2D41F4 ).weight( BestiaryInfo.DefaultWeight.LOW ).theme( BestiaryInfo.Theme.FISHING )
                .addExperience( 2 ).drownImmune().fluidPushImmune()
                .bowAttack( 0.0, 1.0, 1.0, 40, 10.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 0.8 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( MobFamily.Species<?> species ) {
        return new ZombieSpeciesConfig( species, 0.0, 0.0 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Zombified Fisher Piglin",
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
    public static EntityType.IFactory<FishingZombifiedPiglinEntity> getVariantFactory() { return FishingZombifiedPiglinEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends FishingZombifiedPiglinEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public FishingZombifiedPiglinEntity( EntityType<? extends _SpecialZombifiedPiglinEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        //TODO add angler AI @ attack priority
    }
    
    /** Override to change starting equipment or stats. */
    @Override
    public void finalizeVariantSpawn( IServerWorld world, DifficultyInstance difficulty, @Nullable SpawnReason spawnReason,
                                      @Nullable ILivingEntityData groupData ) {
        setItemSlot( EquipmentSlotType.MAINHAND, new ItemStack( Items.FISHING_ROD ) );
        if( getItemBySlot( EquipmentSlotType.FEET ).isEmpty() ) {
            ItemStack booties = new ItemStack( Items.LEATHER_BOOTS );
            ((IDyeableArmorItem) booties.getItem()).setColor( booties, 0xFFFF00 );
            setItemSlot( EquipmentSlotType.FEET, booties );
        }
        setCanPickUpLoot( false );
    }
    
    
    //--------------- IAngler Implementations ----------------
    
    /** The parameter for baby status. */
    private static final DataParameter<Boolean> IS_LINE_OUT = EntityDataManager.defineId( FishingZombifiedPiglinEntity.class, DataSerializers.BOOLEAN );
    
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
    public ItemStack getItemBySlot( EquipmentSlotType slot ) {
        // Display a stick in place of the "cast fishing rod" when the fancy render is disabled
        if( level.isClientSide() && !Config.MAIN.GENERAL.fancyFishingMobs.get() && EquipmentSlotType.MAINHAND.equals( slot ) ) {
            final ItemStack held = super.getItemBySlot( slot );
            if( !held.isEmpty() && held.getItem() instanceof FishingRodItem && isLineOut() ) {
                return new ItemStack( Items.STICK );
            }
            return held;
        }
        return super.getItemBySlot( slot );
    }
}