package fathertoast.specialmobs.common.entity.silverfish;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.ai.AmphibiousMovementController;
import fathertoast.specialmobs.common.entity.ai.IAmphibiousMob;
import fathertoast.specialmobs.common.event.NaturalSpawnManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

/**
 * A bare-bones implementation of an amphibious silverfish. Just fix the AI and it's good to go.
 */
public abstract class AmphibiousSilverfishEntity extends _SpecialSilverfishEntity implements IAmphibiousMob {
    
    @SpecialMob.SpawnPlacementRegistrar
    public static void registerSpeciesSpawnPlacement( MobFamily.Species<? extends AmphibiousSilverfishEntity> species ) {
        NaturalSpawnManager.registerSpawnPlacement( species, SpawnPlacements.Type.IN_WATER,
                _SpecialSilverfishEntity::checkFamilySpawnRules );
    }
    
    /** @return True if this entity's position is currently obstructed. */
    @Override
    public boolean checkSpawnObstruction( LevelReader level ) { return level.isUnobstructed( this ); }
    
    
    private final WaterBoundPathNavigation waterNavigation;
    private final GroundPathNavigation groundNavigation;
    
    private boolean swimmingUp;
    
    public AmphibiousSilverfishEntity( EntityType<? extends _SpecialSilverfishEntity> entityType, Level level ) {
        super( entityType, level );
        moveControl = new AmphibiousMovementController<>( this );
        waterNavigation = new WaterBoundPathNavigation( this, level );
        groundNavigation = new GroundPathNavigation( this, level );
        maxUpStep = 1.0F;
        setPathfindingMalus( BlockPathTypes.WATER, BlockPathTypes.WALKABLE.getMalus() );
    }
    
    /** Loads data from this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void readAdditionalSaveData( CompoundTag tag ) {
        super.readAdditionalSaveData( tag );
        setPathfindingMalus( BlockPathTypes.WATER, BlockPathTypes.WALKABLE.getMalus() );
    }
    
    
    //--------------- IAmphibiousMob Implementation ----------------
    
    /** Called each tick to update this entity's swimming state. */
    @Override
    public void updateSwimming() {
        if( !level.isClientSide ) {
            if( isEffectiveAi() && isUnderWater() && shouldSwim() ) {
                setNavigatorToSwim();
                setSwimming( true );
            }
            else {
                setNavigatorToGround();
                setSwimming( false );
            }
        }
    }
    
    /** Moves this entity in the desired direction. Input magnitude of < 1 scales down movement speed. */
    @Override
    public void travel( Vec3 input ) {
        if( isEffectiveAi() && isUnderWater() && shouldSwim() ) {
            moveRelative( 0.01F, input );
            move( MoverType.SELF, getDeltaMovement() );
            setDeltaMovement( getDeltaMovement().scale( 0.9 ) );
        }
        else super.travel( input );
    }
    
    /** @return Water drag coefficient. */
    @Override
    protected float getWaterSlowDown() { return 0.9F; }
    
    /** @return True if this mob should use its swimming navigator for its current goal. */
    @Override
    public boolean shouldSwim() {
        if( swimmingUp ) return true;
        final LivingEntity target = getTarget();
        return target != null && target.isInWater();
    }
    
    /** Sets whether this mob should swim upward. */
    @Override
    public void setSwimmingUp( boolean value ) { swimmingUp = value; }
    
    /** @return True if this mob should swim upward. */
    @Override
    public boolean isSwimmingUp() { return swimmingUp; }
    
    /** Sets this mob's current navigator to swimming mode. */
    @Override
    public void setNavigatorToSwim() { navigation = waterNavigation; }
    
    /** Sets this mob's current navigator to ground mode. */
    @Override
    public void setNavigatorToGround() { navigation = groundNavigation; }
}