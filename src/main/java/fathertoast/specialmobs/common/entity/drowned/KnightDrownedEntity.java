package fathertoast.specialmobs.common.entity.drowned;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.DrownedSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

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
    public static EntityType.EntityFactory<KnightDrownedEntity> getVariantFactory() { return KnightDrownedEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends KnightDrownedEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public KnightDrownedEntity( EntityType<? extends _SpecialDrownedEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Override to change starting equipment or stats. */
    @Override
    public void finalizeVariantSpawn( ServerLevelAccessor level, DifficultyInstance difficulty, @Nullable MobSpawnType spawnType,
                                     @Nullable SpawnGroupData groupData ) {
        final ItemStack heldItem = getItemBySlot( EquipmentSlot.MAINHAND );
        if( heldItem.isEmpty() || heldItem.getItem() == Items.FISHING_ROD ) {
            setItemSlot( EquipmentSlot.MAINHAND, new ItemStack( Items.GOLDEN_SWORD ) );
        }
        setItemSlot( EquipmentSlot.HEAD, new ItemStack( Items.CHAINMAIL_HELMET ) );
        setItemSlot( EquipmentSlot.CHEST, new ItemStack( Items.CHAINMAIL_CHESTPLATE ) );
        setItemSlot( EquipmentSlot.LEGS, new ItemStack( Items.CHAINMAIL_LEGGINGS ) );
        setItemSlot( EquipmentSlot.FEET, new ItemStack( Items.CHAINMAIL_BOOTS ) );
    }
}