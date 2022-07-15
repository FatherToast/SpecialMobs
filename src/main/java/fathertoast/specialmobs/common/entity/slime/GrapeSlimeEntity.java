package fathertoast.specialmobs.common.entity.slime;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.ai.goal.SpecialLeapAtTargetGoal;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.Items;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.World;

@SpecialMob
public class GrapeSlimeEntity extends _SpecialSlimeEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<GrapeSlimeEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xB333B3 ).theme( BestiaryInfo.Theme.MOUNTAIN )
                .uniqueTextureBaseOnly()
                .addExperience( 1 ).fallImmune()
                .addToAttribute( Attributes.MAX_HEALTH, 4.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 1.2 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Grape Slime",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addGuaranteedDrop( "base", Items.SLIME_BALL, 1 );
        loot.addUncommonDrop( "uncommon", Items.PURPLE_DYE );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<GrapeSlimeEntity> getVariantFactory() { return GrapeSlimeEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends GrapeSlimeEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public GrapeSlimeEntity( EntityType<? extends _SpecialSlimeEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        goalSelector.addGoal( 0, new SpecialLeapAtTargetGoal(
                this, 10, 6.0F, 12.0F, 1.1F, 2.6F ) );
    }
    
    private static final IParticleData JUMP_PARTICLE = new ItemParticleData( ParticleTypes.ITEM, Items.PURPLE_DYE.getDefaultInstance() );
    
    /** @return This slime's particle type for jump effects. */
    @Override
    protected IParticleData getParticleType() { return JUMP_PARTICLE; }
}