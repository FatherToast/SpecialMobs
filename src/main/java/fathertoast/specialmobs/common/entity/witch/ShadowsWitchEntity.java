package fathertoast.specialmobs.common.entity.witch;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collection;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class ShadowsWitchEntity extends _SpecialWitchEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<ShadowsWitchEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0x000000 );
        //TODO theme - forest
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Witch of Shadows",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.INK_SAC );
        loot.addRareDrop( "rare", PotionUtils.setPotion( new ItemStack( Items.POTION ), Potions.NIGHT_VISION ) );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<ShadowsWitchEntity> getVariantFactory() { return ShadowsWitchEntity::new; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    private static final Collection<EffectInstance> POTION_SHADOWS = Arrays.asList(
            new EffectInstance( Effects.BLINDNESS, 300, 0 ),
            new EffectInstance( Effects.WITHER, 200, 0 )
    );
    
    public ShadowsWitchEntity( EntityType<? extends _SpecialWitchEntity> entityType, World world ) {
        super( entityType, world );
        getSpecialData().addPotionImmunity( Effects.BLINDNESS, Effects.WITHER );
        xpReward += 2;
    }
    
    /** Override to modify potion attacks. Return an empty item stack to cancel the potion throw. */
    @Override
    protected ItemStack pickVariantThrownPotion( ItemStack originalPotion, LivingEntity target, float damageMulti, float distance ) {
        if( target.getHealth() >= 4.0F && (!target.hasEffect( Effects.BLINDNESS ) || !target.hasEffect( Effects.WITHER )) ) {
            return makeSplashPotion( POTION_SHADOWS );
        }
        return originalPotion;
    }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        final LivingEntity target = getTarget();
        if( !level.isClientSide() && isAlive() && target != null && target.hasEffect( Effects.BLINDNESS ) && random.nextInt( 10 ) == 0 ) {
            target.removeEffect( Effects.NIGHT_VISION ); // Prevent blind + night vision combo (black screen)
        }
        super.aiStep();
    }
    
    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "shadows" ),
            GET_TEXTURE_PATH( "shadows_eyes" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
}