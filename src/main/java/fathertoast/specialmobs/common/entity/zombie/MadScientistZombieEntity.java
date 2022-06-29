package fathertoast.specialmobs.common.entity.zombie;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.core.register.SMItems;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.entity.ai.SpecialInjectCreeperGoal;
import fathertoast.specialmobs.common.util.AttributeHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class MadScientistZombieEntity extends _SpecialZombieEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<MadScientistZombieEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0xDED4C6 ); // TODO - Temp color
        //TODO theme - madness
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return AttributeHelper.of( _SpecialZombieEntity.createAttributes() )
                .build();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Mad Scientist Zombie",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addRareDrop( "rare", SMItems.SYRINGE.get() );
    }
    
    @SpecialMob.Constructor
    public MadScientistZombieEntity( EntityType<? extends _SpecialZombieEntity> entityType, World world ) {
        super( entityType, world );
        xpReward += 2;
    }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        disableRangedAI();
        
        AIHelper.insertGoal( goalSelector, 2, new SpecialInjectCreeperGoal<>(
                this, getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.25D, 20.0D,
                ( madman, creeper ) -> creeper.isAlive() && !creeper.isPowered() && madman.getSensing().canSee( creeper ) ) );
    }
    
    /** Override to change this entity's attack goal priority. */
    @Override
    protected int getVariantAttackPriority() { return super.getVariantAttackPriority() + 1; }
    
    /** Called during spawn finalization to set starting equipment. */
    @Override
    protected void populateDefaultEquipmentSlots( DifficultyInstance difficulty ) {
        super.populateDefaultEquipmentSlots( difficulty );
        
        setItemSlot( EquipmentSlotType.MAINHAND, new ItemStack( SMItems.SYRINGE.get() ) );
        setDropChance( EquipmentSlotType.MAINHAND, 0.0F );
    }
    
    /** Override to change this entity's chance to spawn with a bow. */
    @Override
    protected double getVariantBowChance() { return 0.0; }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( Entity target ) {
        if( target instanceof LivingEntity && random.nextFloat() < 0.3F ) {
            final LivingEntity livingTarget = (LivingEntity) target;
            final int duration = MobHelper.getDebuffDuration( level.getDifficulty() );
            
            livingTarget.addEffect( new EffectInstance( Effects.POISON, duration, 1 ) );
        }
    }
    
    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "madscientist" ),
            null,
            GET_TEXTURE_PATH( "madscientist_overlay" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
}