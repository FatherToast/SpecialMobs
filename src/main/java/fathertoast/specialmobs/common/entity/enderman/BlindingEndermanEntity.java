package fathertoast.specialmobs.common.entity.enderman;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class BlindingEndermanEntity extends _SpecialEndermanEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<BlindingEndermanEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0xFFFFFF );
        //TODO theme - forest
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Blinding Enderman",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.INK_SAC );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<BlindingEndermanEntity> getVariantFactory() { return BlindingEndermanEntity::new; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public BlindingEndermanEntity( EntityType<? extends _SpecialEndermanEntity> entityType, World world ) {
        super( entityType, world );
        getSpecialData().addPotionImmunity( Effects.BLINDNESS );
        xpReward += 1;
    }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        super.aiStep();
        
        // Apply blinding effect while near target
        final LivingEntity target = getTarget();
        if( target != null && distanceToSqr( target ) < 100.0 ) {
            target.addEffect( new EffectInstance( Effects.BLINDNESS, 50 ) );
            target.removeEffect( Effects.NIGHT_VISION ); // Prevent blind + night vision combo (black screen)
        }
    }
    
    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "blinding" ),
            GET_TEXTURE_PATH( "blinding_eyes" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
}