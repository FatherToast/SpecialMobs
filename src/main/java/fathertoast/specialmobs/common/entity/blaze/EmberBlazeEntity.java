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
public class EmberBlazeEntity extends _SpecialBlazeEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<EmberBlazeEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0x000000 );
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return AttributeHelper.of( _SpecialBlazeEntity.createAttributes() )
                .addAttribute( Attributes.MAX_HEALTH, 10.0 )
                .addAttribute( Attributes.ARMOR, 10.0 )
                .multAttribute( Attributes.MOVEMENT_SPEED, 1.3 )
                .build();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Ember",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.COAL, 1 );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<EmberBlazeEntity> getVariantFactory() { return EmberBlazeEntity::new; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public EmberBlazeEntity( EntityType<? extends _SpecialBlazeEntity> entityType, World world ) {
        super( entityType, world );
        xpReward += 2;
    }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        disableRangedAI();
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    protected void onVariantAttack( Entity target ) {
        if( target instanceof LivingEntity ) {
            MobHelper.causeLifeLoss( (LivingEntity) target, 2.0F );
        }
    }
    
    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "ember" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
}