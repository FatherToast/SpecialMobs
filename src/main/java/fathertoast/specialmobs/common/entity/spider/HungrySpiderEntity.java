package fathertoast.specialmobs.common.entity.spider;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Food;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.UUID;

@SpecialMob
public class HungrySpiderEntity extends _SpecialSpiderEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<HungrySpiderEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x799C65 ).theme( BestiaryInfo.Theme.MOUNTAIN )
                .uniqueTextureWithEyes()
                .size( 1.5F, 1.9F, 1.3F )
                .addExperience( 2 ).regen( 40 ).disableRangedAttack()
                .addToAttribute( Attributes.MAX_HEALTH, 4.0 )
                .addToAttribute( Attributes.ATTACK_DAMAGE, -1.0 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Hungry Spider",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.BONE );
        loot.addUncommonDrop( "uncommon", Items.APPLE, Items.BEETROOT, Items.ROTTEN_FLESH, Items.CHICKEN, Items.RABBIT, Items.COOKIE );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<HungrySpiderEntity> getVariantFactory() { return HungrySpiderEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends HungrySpiderEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** The damage boost to apply from growth level. */
    private static final AttributeModifier DAMAGE_BOOST = new AttributeModifier( UUID.fromString( "70457CAB-AA09-4E1C-B44B-99DD4A2A836D" ),
            "Feeding damage boost", 1.0, AttributeModifier.Operation.ADDITION );
    /** The health boost to apply from max health stacks. */
    private static final AttributeModifier HEALTH_BOOST = new AttributeModifier( UUID.fromString( "D22A70EF-7C71-4BC5-8B23-7045728FD84F" ),
            "Feeding health boost", 2.0, AttributeModifier.Operation.ADDITION );
    
    /** The level of increased attack damage gained. */
    private int growthLevel;
    /** The level of increased max health gained. */
    private int maxHealthStacks;
    
    public HungrySpiderEntity( EntityType<? extends _SpecialSpiderEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( Entity target ) {
        if( level.isClientSide() ) return;
        
        if( target instanceof PlayerEntity && ForgeEventFactory.getMobGriefingEvent( level, this ) ) {
            final ItemStack food = MobHelper.stealRandomFood( (PlayerEntity) target );
            if( !food.isEmpty() ) {
                final float previousHealth = getMaxHealth();
                if( maxHealthStacks < 32 ) maxHealthStacks++;
                if( growthLevel < 7 ) growthLevel++;
                updateFeedingLevels();
                setHealth( getHealth() + getMaxHealth() - previousHealth );
                
                final Food foodStats = food.getItem().getFoodProperties();
                heal( Math.max( foodStats == null ? 0.0F : foodStats.getNutrition(), 1.0F ) );
                playSound( SoundEvents.PLAYER_BURP, 0.5F, random.nextFloat() * 0.1F + 0.9F );
            }
        }
        else if( target instanceof LivingEntity ) {
            MobHelper.stealLife( this, (LivingEntity) target, 2.0F );
        }
    }
    
    /** Recalculates the modifiers associated with this entity's feeding level counters. */
    private void updateFeedingLevels() {
        if( level != null && !level.isClientSide ) {
            final ModifiableAttributeInstance health = getAttribute( Attributes.MAX_HEALTH );
            final ModifiableAttributeInstance damage = getAttribute( Attributes.ATTACK_DAMAGE );
            //noinspection ConstantConditions
            health.removeModifier( HEALTH_BOOST.getId() );
            //noinspection ConstantConditions
            damage.removeModifier( DAMAGE_BOOST.getId() );
            if( maxHealthStacks > 0 ) {
                // Health, in particular, must be permanent to avoid health getting throttled on reload
                health.addPermanentModifier( new AttributeModifier( HEALTH_BOOST.getId(), HEALTH_BOOST.getName(),
                        HEALTH_BOOST.getAmount() * maxHealthStacks, HEALTH_BOOST.getOperation() ) );
            }
            if( growthLevel > 0 ) {
                damage.addPermanentModifier( new AttributeModifier( DAMAGE_BOOST.getId(), DAMAGE_BOOST.getName(),
                        DAMAGE_BOOST.getAmount() * growthLevel, DAMAGE_BOOST.getOperation() ) );
            }
        }
    }
    
    /** Override to save data to this entity's NBT data. */
    @Override
    public void addVariantSaveData( CompoundNBT saveTag ) {
        saveTag.putByte( References.TAG_HEALTH_STACKS, (byte) maxHealthStacks );
        saveTag.putByte( References.TAG_GROWTH_LEVEL, (byte) growthLevel );
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundNBT saveTag ) {
        if( saveTag.contains( References.TAG_HEALTH_STACKS, References.NBT_TYPE_NUMERICAL ) )
            maxHealthStacks = saveTag.getByte( References.TAG_HEALTH_STACKS );
        if( saveTag.contains( References.TAG_GROWTH_LEVEL, References.NBT_TYPE_NUMERICAL ) )
            growthLevel = saveTag.getByte( References.TAG_GROWTH_LEVEL );
        updateFeedingLevels();
    }
}