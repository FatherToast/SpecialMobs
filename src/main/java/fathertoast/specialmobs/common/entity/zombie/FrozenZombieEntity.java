package fathertoast.specialmobs.common.entity.zombie;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

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
    public static EntityType.IFactory<FrozenZombieEntity> getVariantFactory() { return FrozenZombieEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends FrozenZombieEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    private static final AttributeModifier BURNING_SPEED_BOOST = new AttributeModifier( UUID.fromString( "B4704571-9566-4402-BC1F-2EE2A276D836" ),
            "AHHHHHHHHHHHHHHH", 0.75, AttributeModifier.Operation.MULTIPLY_BASE );
    
    private boolean wasBurning;
    
    public FrozenZombieEntity( EntityType<? extends _SpecialZombieEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to change this entity's AI goals. */
    protected void registerVariantGoals() {
        // Make it look more creepy by removing all idle behaviors
        AIHelper.removeGoals( goalSelector, WaterAvoidingRandomWalkingGoal.class );
        AIHelper.removeGoals( goalSelector, LookAtGoal.class );
        AIHelper.removeGoals( goalSelector, LookRandomlyGoal.class );
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        MobHelper.applyEffect( target, Effects.MOVEMENT_SLOWDOWN, 2 );
    }
    
    /** Override to modify this entity's ranged attack projectile. */
    @Override
    protected AbstractArrowEntity getVariantArrow( AbstractArrowEntity arrow, ItemStack arrowItem, float damageMulti ) {
        return MobHelper.tipArrow( arrow, Effects.MOVEMENT_SLOWDOWN );
    }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        if( !level.isClientSide() && wasBurning != (getRemainingFireTicks() > 0) ) {
            wasBurning = !wasBurning;
            final ModifiableAttributeInstance attributeInstance = getAttribute( Attributes.MOVEMENT_SPEED );
            //noinspection ConstantConditions
            attributeInstance.removeModifier( BURNING_SPEED_BOOST );
            if( wasBurning ) {
                attributeInstance.addTransientModifier( BURNING_SPEED_BOOST );
            }
        }
        super.aiStep();
    }
}