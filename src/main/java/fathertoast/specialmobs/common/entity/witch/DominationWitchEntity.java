package fathertoast.specialmobs.common.entity.witch;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.util.AttributeHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.Potions;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Collections;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class DominationWitchEntity extends _SpecialWitchEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<DominationWitchEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0xFFF87E, BestiaryInfo.BaseWeight.LOW );
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return AttributeHelper.of( _SpecialWitchEntity.createAttributes() )
                .multAttribute( Attributes.MOVEMENT_SPEED, 0.8 )
                .build();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Witch of Domination",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.EXPERIENCE_BOTTLE );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<DominationWitchEntity> getVariantFactory() { return DominationWitchEntity::new; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    private static final Collection<EffectInstance> LEVITATION_EFFECTS = Collections.singletonList(
            new EffectInstance( Effects.LEVITATION, 140, 0 ) );
    
    /** Ticks before this witch can use its pull ability. */
    private int pullDelay;
    
    public DominationWitchEntity( EntityType<? extends _SpecialWitchEntity> entityType, World world ) {
        super( entityType, world );
        xpReward += 2;
    }
    
    /** Override to modify potion attacks. Return an empty item stack to cancel the potion throw. */
    @Override
    protected ItemStack pickVariantThrownPotion( ItemStack originalPotion, LivingEntity target, float damageMulti, float distance ) {
        if( !target.hasEffect( Effects.WEAKNESS ) ) {
            return makeSplashPotion( Potions.WEAKNESS );
        }
        else if( distance > 5.0F && !target.hasEffect( Effects.LEVITATION ) && random.nextFloat() < 0.5F ) {
            return makeSplashPotion( LEVITATION_EFFECTS );
        }
        return originalPotion;
    }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        final LivingEntity target = getTarget();
        if( !level.isClientSide() && isAlive() && pullDelay-- <= 0 && target != null && random.nextInt( 20 ) == 0 ) {
            
            // Pull the player toward this entity if they are vulnerable
            final double distanceSq = target.distanceToSqr( this );
            if( distanceSq > 100.0 && distanceSq < 196.0 &&
                    (target.hasEffect( Effects.WEAKNESS ) || target.hasEffect( Effects.LEVITATION )) && canSee( target ) ) {
                pullDelay = 100;
                
                target.setDeltaMovement( new Vector3d(
                        getX() - target.getX(),
                        getY() - target.getY(),
                        getZ() - target.getZ() )
                        .scale( 0.32 )
                        .add( 0.0, Math.sqrt( Math.sqrt( distanceSq ) ) * 0.1, 0.0 ) );
                target.hurtMarked = true;
            }
        }
        super.aiStep();
    }
    
    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "domination" ),
            GET_TEXTURE_PATH( "domination_eyes" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
}