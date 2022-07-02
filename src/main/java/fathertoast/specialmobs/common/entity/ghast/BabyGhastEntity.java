package fathertoast.specialmobs.common.entity.ghast;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.spider._SpecialSpiderEntity;
import fathertoast.specialmobs.common.util.AttributeHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.Items;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class BabyGhastEntity extends _SpecialGhastEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<BabyGhastEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        entityType.sized( 1.0F, 1.0F );
        return new BestiaryInfo( 0xFFC0CB, BestiaryInfo.BaseWeight.DISABLED );
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return AttributeHelper.of( _SpecialSpiderEntity.createAttributes() )
                .addAttribute( Attributes.ATTACK_DAMAGE, -1.0 )
                .multAttribute( Attributes.MOVEMENT_SPEED, 1.3 )
                .build();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Baby Ghast",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        loot.addCommonDrop( "common", Items.GUNPOWDER, 1 );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<BabyGhastEntity> getVariantFactory() { return BabyGhastEntity::new; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public BabyGhastEntity( EntityType<? extends _SpecialGhastEntity> entityType, World world ) {
        super( entityType, world );
        getSpecialData().setBaseScale( 0.25F );
        xpReward = 1;
    }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        getSpecialData().rangedAttackDamage -= 1.0F;
        disableRangedAI();
    }
    
    /** @return The sound this entity makes idly. */
    @Override
    protected SoundEvent getAmbientSound() { return null; } // There could be a lot of these, need to be less annoying
}