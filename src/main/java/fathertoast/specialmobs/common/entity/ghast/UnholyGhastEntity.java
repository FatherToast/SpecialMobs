package fathertoast.specialmobs.common.entity.ghast;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.AttributeHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class UnholyGhastEntity extends _SpecialGhastEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<UnholyGhastEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        entityType.sized( 2.0F, 2.0F );
        return new BestiaryInfo( 0x7AC754, BestiaryInfo.BaseWeight.LOW );
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return AttributeHelper.of( _SpecialGhastEntity.createAttributes() )
                .addAttribute( Attributes.MAX_HEALTH, 10.0 )
                .addAttribute( Attributes.ATTACK_DAMAGE, 2.0 )
                .multAttribute( Attributes.MOVEMENT_SPEED, 0.7 )
                .build();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Unholy Ghast",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.BONE );
        loot.addSemicommonDrop( "semicommon", Items.QUARTZ );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<UnholyGhastEntity> getVariantFactory() { return UnholyGhastEntity::new; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public UnholyGhastEntity( EntityType<? extends _SpecialGhastEntity> entityType, World world ) {
        super( entityType, world );
        getSpecialData().setBaseScale( 0.5F );
        xpReward += 4;
    }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        getSpecialData().rangedAttackDamage += 2.0F;
        disableRangedAI();
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    protected void onVariantAttack( Entity target ) {
        if( target instanceof LivingEntity ) {
            MobHelper.stealLife( this, (LivingEntity) target, 2.0F );
        }
    }
    
    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt( DamageSource source, float amount ) {
        if( MobHelper.isDamageSourceIneffectiveAgainstVampires( source ) ) {
            amount = Math.min( 2.0F, amount );
        }
        return super.hurt( source, amount );
    }
    
    /** @return This entity's creature type. */
    @Override
    public CreatureAttribute getMobType() { return CreatureAttribute.UNDEAD; }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        if( isSunBurnTick() ) {
            final ItemStack hat = getItemBySlot( EquipmentSlotType.HEAD );
            if( !hat.isEmpty() ) {
                if( hat.isDamageableItem() ) {
                    hat.setDamageValue( hat.getDamageValue() + random.nextInt( 2 ) );
                    if( hat.getDamageValue() >= hat.getMaxDamage() ) {
                        broadcastBreakEvent( EquipmentSlotType.HEAD );
                        setItemSlot( EquipmentSlotType.HEAD, ItemStack.EMPTY );
                    }
                }
            }
            else {
                setSecondsOnFire( 8 );
            }
        }
        super.aiStep();
    }
    
    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "unholy" ),
            null,
            GET_TEXTURE_PATH( "unholy_shooting" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
}