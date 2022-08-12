package fathertoast.specialmobs.common.entity.drowned;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.DrownedSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;

@SpecialMob
public class KnightDrownedEntity extends _SpecialDrownedEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<KnightDrownedEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xDDDDDD )
                .addExperience( 2 ).multiplyRangedSpread( 1.2 )
                .addToAttribute( Attributes.MAX_HEALTH, 10.0 ).addToAttribute( Attributes.ARMOR, 10.0 )
                .addToAttribute( Attributes.ATTACK_DAMAGE, 8.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 0.8 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( MobFamily.Species<?> species ) {
        return new DrownedSpeciesConfig( species, 0.9, 1.0 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Drowned Knight",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.GOLD_NUGGET );
        loot.addUncommonDrop( "uncommon", Items.GOLD_INGOT );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<KnightDrownedEntity> getVariantFactory() { return KnightDrownedEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends KnightDrownedEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public KnightDrownedEntity( EntityType<? extends _SpecialDrownedEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to change starting equipment or stats. */
    @Override
    public void finalizeVariantSpawn( IServerWorld world, DifficultyInstance difficulty, @Nullable SpawnReason spawnReason,
                                      @Nullable ILivingEntityData groupData ) {
        final ItemStack heldItem = getItemBySlot( EquipmentSlotType.MAINHAND );
        if( heldItem.isEmpty() || heldItem.getItem() == Items.FISHING_ROD ) {
            setItemSlot( EquipmentSlotType.MAINHAND, new ItemStack( Items.GOLDEN_SWORD ) );
        }
        setItemSlot( EquipmentSlotType.HEAD, new ItemStack( Items.CHAINMAIL_HELMET ) );
        setItemSlot( EquipmentSlotType.CHEST, new ItemStack( Items.CHAINMAIL_CHESTPLATE ) );
        setItemSlot( EquipmentSlotType.LEGS, new ItemStack( Items.CHAINMAIL_LEGGINGS ) );
        setItemSlot( EquipmentSlotType.FEET, new ItemStack( Items.CHAINMAIL_BOOTS ) );
    }
}