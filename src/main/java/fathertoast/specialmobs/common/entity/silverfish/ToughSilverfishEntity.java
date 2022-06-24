package fathertoast.specialmobs.common.entity.silverfish;

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
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class ToughSilverfishEntity extends _SpecialSilverfishEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        entityType.sized( 0.6F, 0.9F );
        return new BestiaryInfo( 0xDD0E0E, BestiaryInfo.BaseWeight.LOW );
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return AttributeHelper.of( _SpecialSilverfishEntity.createAttributes() )
                .addAttribute( Attributes.MAX_HEALTH, 16.0 )
                .addAttribute( Attributes.ARMOR, 15.0 )
                .addAttribute( Attributes.ATTACK_DAMAGE, 2.0 )
                .multAttribute( Attributes.MOVEMENT_SPEED, 0.7 )
                .build();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Tough Silverfish",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.FLINT, 1 );
        loot.addRareDrop( "rare", Items.IRON_INGOT );
    }
    
    @SpecialMob.Constructor
    public ToughSilverfishEntity( EntityType<? extends _SpecialSilverfishEntity> entityType, World world ) {
        super( entityType, world );
        getSpecialData().setBaseScale( 1.5F );
        xpReward += 2;
    }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "tough" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
}