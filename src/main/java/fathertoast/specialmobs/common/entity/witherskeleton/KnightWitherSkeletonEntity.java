package fathertoast.specialmobs.common.entity.witherskeleton;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.AttributeHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class KnightWitherSkeletonEntity extends _SpecialWitherSkeletonEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0xDDDDDD );
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return AttributeHelper.of( _SpecialWitherSkeletonEntity.createAttributes() )
                .addAttribute( Attributes.MAX_HEALTH, 10.0 )
                .addAttribute( Attributes.ARMOR, 10.0 )
                .addAttribute( Attributes.ATTACK_DAMAGE, 4.0 )
                .multAttribute( Attributes.MOVEMENT_SPEED, 0.8 )
                .build();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Wither Skeleton Knight",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.IRON_NUGGET );
    }
    
    @SpecialMob.Constructor
    public KnightWitherSkeletonEntity( EntityType<? extends _SpecialWitherSkeletonEntity> entityType, World world ) {
        super( entityType, world );
        xpReward += 2;
    }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        getSpecialData().rangedAttackDamage += 4.0F;
        getSpecialData().rangedAttackSpread *= 1.2F;
    }
    
    /** Called during spawn finalization to set starting equipment. */
    @Override
    protected void populateDefaultEquipmentSlots( DifficultyInstance difficulty ) {
        super.populateDefaultEquipmentSlots( difficulty );
        if( random.nextDouble() < 0.95 ) {
            setItemSlot( EquipmentSlotType.MAINHAND, new ItemStack( Items.IRON_SWORD ) );
            setItemSlot( EquipmentSlotType.OFFHAND, new ItemStack( Items.SHIELD ) );
        }
        setItemSlot( EquipmentSlotType.HEAD, new ItemStack( Items.CHAINMAIL_HELMET ) );
        setItemSlot( EquipmentSlotType.CHEST, new ItemStack( Items.CHAINMAIL_CHESTPLATE ) );
        setItemSlot( EquipmentSlotType.LEGS, new ItemStack( Items.CHAINMAIL_LEGGINGS ) );
        setItemSlot( EquipmentSlotType.FEET, new ItemStack( Items.CHAINMAIL_BOOTS ) );
    }
    
    /** Override to change this entity's chance to spawn with a bow. */
    @Override
    protected double getVariantBowChance() { return 1.0; }
}