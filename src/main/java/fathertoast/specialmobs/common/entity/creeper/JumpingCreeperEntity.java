package fathertoast.specialmobs.common.entity.creeper;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.entity.ai.goal.SpecialLeapAtTargetGoal;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.Items;
import net.minecraft.world.World;

@SpecialMob
public class JumpingCreeperEntity extends _SpecialCreeperEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<JumpingCreeperEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x7D6097 ).theme( BestiaryInfo.Theme.MOUNTAIN )
                .uniqueTextureBaseOnly()
                .addExperience( 2 ).fallImmune()
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 1.2 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Jumping Creeper",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addSemicommonDrop( "semicommon", Items.SLIME_BALL );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<JumpingCreeperEntity> getVariantFactory() { return JumpingCreeperEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends JumpingCreeperEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public JumpingCreeperEntity( EntityType<? extends _SpecialCreeperEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        AIHelper.insertGoal( goalSelector, 4, new SpecialLeapAtTargetGoal(
                this, 10, 6.0F, 10.0F, 1.3F, 2.0F ) );
    }
}