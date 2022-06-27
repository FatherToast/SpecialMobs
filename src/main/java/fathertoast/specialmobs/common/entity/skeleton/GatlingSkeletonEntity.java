package fathertoast.specialmobs.common.entity.skeleton;

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
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class GatlingSkeletonEntity extends _SpecialSkeletonEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<GatlingSkeletonEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0xFFFF0B );
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return AttributeHelper.of( _SpecialSkeletonEntity.createAttributes() )
                .addAttribute( Attributes.MAX_HEALTH, 10.0 )
                .addAttribute( Attributes.ATTACK_DAMAGE, 2.0 )
                .build();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Gatling Skeleton",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addGuaranteedDrop( "base", Items.ARROW, 1 );
        loot.addCommonDrop( "common", Items.GUNPOWDER );
    }
    
    @SpecialMob.Constructor
    public GatlingSkeletonEntity( EntityType<? extends _SpecialSkeletonEntity> entityType, World world ) {
        super( entityType, world );
        xpReward += 2;
    }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        setRangedAI( 0.3, 1 );
        getSpecialData().rangedAttackSpread *= 2.0F;
    }
    
    /** Override to change this entity's chance to spawn with a melee weapon. */
    @Override
    protected double getVariantMeleeChance() { return 0.0; }
    
    private static final ResourceLocation[] TEXTURES = {
            new ResourceLocation( "textures/entity/skeleton/skeleton.png" ),
            null,
            GET_TEXTURE_PATH( "gatling_overlay" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
}