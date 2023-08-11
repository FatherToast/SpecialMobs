package fathertoast.specialmobs.common.entity.slime;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.entity.ai.FluidPathNavigator;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;


@SpecialMob
public class FrozenSlimeEntity extends _SpecialSlimeEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<FrozenSlimeEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x766BBC ).theme( BestiaryInfo.Theme.ICE )
                .uniqueTextureBaseOnly()
                .addExperience( 1 ).effectImmune( MobEffects.MOVEMENT_SLOWDOWN )
                .addToAttribute( Attributes.ARMOR, 10.0 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Frozen Slime",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Blocks.ICE );
        loot.addRareDrop( "rare", Blocks.BLUE_ICE );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<FrozenSlimeEntity> getVariantFactory() { return FrozenSlimeEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends FrozenSlimeEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    private byte frostWalking;
    
    public FrozenSlimeEntity( EntityType<? extends _SpecialSlimeEntity> entityType, Level level ) {
        super( entityType, level );
        setPathfindingMalus( BlockPathTypes.WATER, BlockPathTypes.WALKABLE.getMalus() );
    }
    
    /** Sets this slime's size, optionally resetting its health to max. */
    @Override
    public void setSize( int size, boolean resetHealth ) {
        super.setSize( size, resetHealth );
        
        // Set frost walker level to -1 plus log base 2 of size (typically 0, 1, or 2)
        frostWalking = -1;
        while( size > 0 ) {
            size >>= 1;
            frostWalking++;
        }
    }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        AIHelper.removeGoals( goalSelector, 1 ); // SlimeEntity.FloatGoal
    }
    
    /** @return A new path navigator for this entity to use. */
    @Override
    protected PathNavigation createNavigation(Level level ) {
        return new FluidPathNavigator( this, level, true, false );
    }
    
    /** @return Whether this entity can stand on a particular type of fluid. */
    @Override
    public boolean canStandOnFluid( FluidState fluid ) { return fluid.is( FluidTags.WATER ); }
    
    /** Called each tick to update this entity. */
    @Override
    public void tick() {
        super.tick();
        MobHelper.floatInFluid( this, 0.06, FluidTags.WATER );
        MobHelper.hopOnFluid( this );
    }
    
    /** Called whenever this entity's block position changes. */
    @Override
    protected void onChangedBlock( BlockPos pos ) {
        super.onChangedBlock( pos );
        MobHelper.updateFrostWalker( this, pos, frostWalking );
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        MobHelper.applyEffect( target, MobEffects.MOVEMENT_SLOWDOWN, 4, 0.5F );
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundTag saveTag ) {
        setPathfindingMalus( BlockPathTypes.WATER, BlockPathTypes.WALKABLE.getMalus() );
    }
    
    /** @return This slime's particle type for jump effects. */
    @Override
    protected ParticleOptions getParticleType() { return ParticleTypes.ITEM_SNOWBALL; }
}