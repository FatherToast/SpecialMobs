package fathertoast.specialmobs.common.entity.silverfish;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.ai.IAngler;
import fathertoast.specialmobs.common.util.AttributeHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootEntryItemBuilder;
import fathertoast.specialmobs.datagen.loot.LootPoolBuilder;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class FishingSilverfishEntity extends _SpecialSilverfishEntity implements IAngler {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<FishingSilverfishEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        entityType.sized( 0.5F, 0.4F );
        return new BestiaryInfo( 0x2D41F4 );
        //TODO theme - fishing
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return AttributeHelper.of( _SpecialSilverfishEntity.createAttributes() )
                .addAttribute( Attributes.MAX_HEALTH, 4.0 )
                .multAttribute( Attributes.MOVEMENT_SPEED, 0.9 )
                .build();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Fishing Silverfish",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addPool( new LootPoolBuilder( "common" )
                .addEntry( new LootEntryItemBuilder( Items.COD ).setCount( 0, 2 ).addLootingBonus( 0, 1 ).smeltIfBurning().toLootEntry() )
                .toLootPool() );
    }
    
    @SpecialMob.Constructor
    public FishingSilverfishEntity( EntityType<? extends _SpecialSilverfishEntity> entityType, World world ) {
        super( entityType, world );
        getSpecialData().setBaseScale( 1.2F );
        getSpecialData().setCanBreatheInWater( true );
        getSpecialData().setIgnoreWaterPush( true );
        xpReward += 2;
    }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        getSpecialData().rangedAttackSpread = 10.0F;
        getSpecialData().rangedAttackCooldown = 32;
        getSpecialData().rangedAttackMaxCooldown = 48;
        getSpecialData().rangedAttackMaxRange = 10.0F;
        
        //TODO add angler AI @ 4
    }
    
    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "fishing" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
    
    
    //--------------- IAngler Implementations ----------------
    
    /** Sets this angler's line as out (or in). */
    @Override
    public void setLineOut( boolean value ) { }
    
    /** @return Whether this angler's line is out. */
    @Override
    public boolean isLineOut() { return false; }
}