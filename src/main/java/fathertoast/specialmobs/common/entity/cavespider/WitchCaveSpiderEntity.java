package fathertoast.specialmobs.common.entity.cavespider;

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
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class WitchCaveSpiderEntity extends _SpecialCaveSpiderEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<WitchCaveSpiderEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0xDD0E0E, BestiaryInfo.BaseWeight.LOW );
        //TODO theme - forest
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return AttributeHelper.of( _SpecialCaveSpiderEntity.createAttributes() )
                .addAttribute( Attributes.ARMOR, 15.0 )
                .build();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Witch Cave Spider",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addLootTable( "common", EntityType.WITCH.getDefaultLootTable() );
    }
    
    @SpecialMob.Constructor
    public WitchCaveSpiderEntity( EntityType<? extends _SpecialCaveSpiderEntity> entityType, World world ) {
        super( entityType, world );
        xpReward += 2;
    }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( Entity target ) {
        if( target instanceof LivingEntity ) {
            ((LivingEntity) target).addEffect( MobHelper.nextWitchSpiderEffect( random, level, false ) );
            MobHelper.stealBuffEffect( this, (LivingEntity) target );
        }
    }
    
    /** @return True if the effect can be applied to this entity. */
    @Override
    public boolean canBeAffected( EffectInstance effect ) {
        // Witch spider is immune to debuffs
        return effect.getEffect().getCategory() != EffectType.HARMFUL && super.canBeAffected( effect );
    }
    
    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "witch" ),
            GET_TEXTURE_PATH( "witch_eyes" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
}