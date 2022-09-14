package fathertoast.specialmobs.common.entity.drowned;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.DrownedSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

@SpecialMob
public class TropicalDrownedEntity extends _SpecialDrownedEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<TropicalDrownedEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xD962A3 ).weight( BestiaryInfo.DefaultWeight.LOWEST ).theme( BestiaryInfo.Theme.TROPICAL )
                .uniqueTextureWithOverlay()
                .addExperience( 1 )
                .multiplyRangedCooldown( 0.75F ).rangedMaxRange( 15.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 1.2 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( MobFamily.Species<?> species ) {
        return new DrownedSpeciesConfig( species, 1.0, DEFAULT_SHIELD_CHANCE );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Tropical Drowned",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Blocks.SEA_PICKLE );
        loot.addUncommonDrop( "uncommon",
                Blocks.TUBE_CORAL, Blocks.TUBE_CORAL_FAN, Blocks.TUBE_CORAL_BLOCK,
                Blocks.BRAIN_CORAL, Blocks.BRAIN_CORAL_FAN, Blocks.BRAIN_CORAL_BLOCK,
                Blocks.BUBBLE_CORAL, Blocks.BUBBLE_CORAL_FAN, Blocks.BUBBLE_CORAL_BLOCK,
                Blocks.FIRE_CORAL, Blocks.FIRE_CORAL_FAN, Blocks.FIRE_CORAL_BLOCK,
                Blocks.HORN_CORAL, Blocks.HORN_CORAL_FAN, Blocks.HORN_CORAL_BLOCK );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<TropicalDrownedEntity> getVariantFactory() { return TropicalDrownedEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends TropicalDrownedEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public TropicalDrownedEntity( EntityType<? extends _SpecialDrownedEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        MobHelper.applyEffect( target, Effects.POISON );
    }
}