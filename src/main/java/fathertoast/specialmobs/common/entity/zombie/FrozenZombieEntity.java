package fathertoast.specialmobs.common.entity.zombie;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.entity.drowned.FrozenDrownedEntity;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.UUID;

@SpecialMob
public class FrozenZombieEntity extends _SpecialZombieEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<FrozenZombieEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xDDEAEA ).theme( BestiaryInfo.Theme.ICE )
                .uniqueTextureBaseOnly()
                .addExperience( 1 )
                .addToAttribute( Attributes.ARMOR, 10.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 0.8 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Frozen Zombie",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addClusterDrop( "common", Items.SNOWBALL );
        loot.addUncommonDrop( "uncommon", Blocks.ICE );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<FrozenZombieEntity> getVariantFactory() { return FrozenZombieEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends FrozenZombieEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    private static final AttributeModifier BURNING_SPEED_BOOST = new AttributeModifier( UUID.fromString( "B4704571-9566-4402-BC1F-2EE2A276D836" ),
            "AHHHHHHHHHHHHHHH", 0.75, AttributeModifier.Operation.MULTIPLY_BASE );
    
    private boolean wasBurning;
    
    public FrozenZombieEntity( EntityType<? extends _SpecialZombieEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Override to change this entity's AI goals. */
    protected void registerVariantGoals() {
        // Make it look more creepy by removing all idle behaviors
        AIHelper.removeGoals( goalSelector, WaterAvoidingRandomStrollGoal.class );
        AIHelper.removeGoals( goalSelector, LookAtPlayerGoal.class );
        AIHelper.removeGoals( goalSelector, RandomLookAroundGoal.class );
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        MobHelper.applyEffect( target, MobEffects.MOVEMENT_SLOWDOWN, 2 );
    }
    
    /** Override to modify this entity's ranged attack projectile. */
    @Override
    protected AbstractArrow getVariantArrow( AbstractArrow arrow, ItemStack arrowItem, float damageMulti ) {
        return MobHelper.tipArrow( arrow, MobEffects.MOVEMENT_SLOWDOWN );
    }
    
    /** Override to change the entity this converts to when drowned. */
    @Override
    protected EntityType<? extends Zombie> getVariantConversionType() { return FrozenDrownedEntity.SPECIES.entityType.get(); }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        if( !level.isClientSide() && wasBurning != (getRemainingFireTicks() > 0) ) {
            wasBurning = !wasBurning;
            final AttributeInstance attributeInstance = getAttribute( Attributes.MOVEMENT_SPEED );
            //noinspection ConstantConditions
            attributeInstance.removeModifier( BURNING_SPEED_BOOST );
            if( wasBurning ) {
                attributeInstance.addTransientModifier( BURNING_SPEED_BOOST );
            }
        }
        super.aiStep();
    }
}