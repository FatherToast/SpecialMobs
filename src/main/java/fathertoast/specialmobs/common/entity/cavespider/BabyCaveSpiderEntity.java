package fathertoast.specialmobs.common.entity.cavespider;

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
import net.minecraft.item.Items;
import net.minecraft.world.World;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class BabyCaveSpiderEntity extends _SpecialCaveSpiderEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.TypeHolder
    public static RegistryObject<EntityType<BabyCaveSpiderEntity>> ENTITY_TYPE;
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        entityType.sized( 0.6F, 0.4F );
        return new BestiaryInfo( 0xFFC0CB, BestiaryInfo.BaseWeight.DISABLED );
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return AttributeHelper.of( _SpecialCaveSpiderEntity.createAttributes() )
                .multAttribute( Attributes.MAX_HEALTH, 1.0 / 3.0 )
                .addAttribute( Attributes.ATTACK_DAMAGE, -1.0 )
                .multAttribute( Attributes.MOVEMENT_SPEED, 1.3 )
                .build();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Baby Cave Spider",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        loot.addCommonDrop( "common", Items.STRING, 1 );
    }
    
    @SpecialMob.Constructor
    public BabyCaveSpiderEntity( EntityType<? extends _SpecialCaveSpiderEntity> entityType, World world ) {
        super( entityType, world );
        getSpecialData().setBaseScale( 0.4F );
        getSpecialData().rangedAttackDamage -= 1.0F;
        getSpecialData().rangedAttackMaxRange = 0.0F;
        xpReward = 1;
    }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        getSpecialData().rangedAttackDamage -= 1.0F;
        getSpecialData().rangedAttackMaxRange = 0.0F;
    }
}