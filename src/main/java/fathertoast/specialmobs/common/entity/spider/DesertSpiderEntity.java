package fathertoast.specialmobs.common.entity.spider;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
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
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class DesertSpiderEntity extends _SpecialSpiderEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        entityType.sized( 0.95F, 0.7F );
        return new BestiaryInfo( 0xE6DDAC );
        //TODO theme - desert
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return AttributeHelper.of( _SpecialSpiderEntity.createAttributes() )
                .addAttribute( Attributes.MAX_HEALTH, 4.0 )
                .build();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Desert Spider",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.LEATHER );
    }
    
    @SpecialMob.Constructor
    public DesertSpiderEntity( EntityType<? extends _SpecialSpiderEntity> entityType, World world ) {
        super( entityType, world );
        getSpecialData().setBaseScale( 0.8F );
        xpReward += 2;
    }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( Entity target ) {
        if( target instanceof LivingEntity ) {
            final LivingEntity livingTarget = (LivingEntity) target;
            final int duration = MobHelper.getDebuffDuration( level.getDifficulty() );
            
            if( true ) { //TODO config
                livingTarget.addEffect( new EffectInstance( Effects.CONFUSION, duration, 0 ) );
            }
            livingTarget.addEffect( new EffectInstance( Effects.BLINDNESS, duration, 0 ) );
            
            livingTarget.addEffect( new EffectInstance( Effects.MOVEMENT_SLOWDOWN, duration, 2 ) );
            livingTarget.addEffect( new EffectInstance( Effects.DAMAGE_RESISTANCE, duration, -3 ) ); // 40% inc damage taken
        }
    }
    
    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "desert" ),
            GET_TEXTURE_PATH( "desert_eyes" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
}