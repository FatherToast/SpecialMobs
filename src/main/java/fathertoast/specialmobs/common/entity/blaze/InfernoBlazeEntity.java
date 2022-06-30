package fathertoast.specialmobs.common.entity.blaze;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.AttributeHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.Entity;
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
public class InfernoBlazeEntity extends _SpecialBlazeEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<InfernoBlazeEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0xF14F00 );
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return AttributeHelper.of( _SpecialBlazeEntity.createAttributes() )
                .addAttribute( Attributes.MAX_HEALTH, 10.0 )
                .build();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Inferno",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.BLAZE_POWDER );
        loot.addSemicommonDrop( "semicommon", Items.FIRE_CHARGE );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<InfernoBlazeEntity> getVariantFactory() { return InfernoBlazeEntity::new; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public InfernoBlazeEntity( EntityType<? extends _SpecialBlazeEntity> entityType, World world ) {
        super( entityType, world );
        xpReward += 2;
    }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        getSpecialData().rangedAttackSpread *= 3.0F;
        setRangedAI( 12, 2, 80, 100, 20.0F );
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    protected void onVariantAttack( Entity target ) {
        if( target instanceof LivingEntity ) {
            MobHelper.causeLifeLoss( (LivingEntity) target, 2.0F );
        }
    }
    
    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "inferno" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
}