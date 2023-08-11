package fathertoast.specialmobs.common.entity.magmacube;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.entity.ai.FluidPathNavigator;
import fathertoast.specialmobs.common.entity.ai.goal.SpecialLeapAtTargetGoal;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

@SpecialMob
public class BouncingMagmaCubeEntity extends _SpecialMagmaCubeEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<BouncingMagmaCubeEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xB333B3 )
                .uniqueTextureBaseOnly()
                .addExperience( 1 ).fallImmune()
                .addToAttribute( Attributes.MAX_HEALTH, 4.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 1.2 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Bouncing Magma Cube",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "base", Items.FEATHER );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<BouncingMagmaCubeEntity> getVariantFactory() { return BouncingMagmaCubeEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends BouncingMagmaCubeEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public BouncingMagmaCubeEntity( EntityType<? extends _SpecialMagmaCubeEntity> entityType, Level level ) {
        super( entityType, level );
        setPathfindingMalus( BlockPathTypes.LAVA, BlockPathTypes.WALKABLE.getMalus() );
    }
    
    /** Override to change this entity's AI goals. */
    protected void registerVariantGoals() {
        AIHelper.removeGoals( goalSelector, 1 ); // SlimeEntity.FloatGoal
        
        goalSelector.addGoal( 0, new SpecialLeapAtTargetGoal(
                this, 10, 6.0F, 12.0F, 1.1F, 2.6F ) );
    }
    
    /** @return A new path navigator for this entity to use. */
    @Override
    protected PathNavigation createNavigation(Level level ) {
        return new FluidPathNavigator( this, level, false, true );
    }
    
    /** @return Whether this entity can stand on a particular type of fluid. */
    @Override
    public boolean canStandOnFluid( FluidState fluid ) { return fluid.is( FluidTags.LAVA ); }
    
    /** Called each tick to update this entity. */
    @Override
    public void tick() {
        super.tick();
        MobHelper.floatInFluid( this, 0.05, FluidTags.LAVA );
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundTag saveTag ) {
        setPathfindingMalus( BlockPathTypes.LAVA, BlockPathTypes.WALKABLE.getMalus() );
    }
}