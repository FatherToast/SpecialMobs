package fathertoast.specialmobs.common.entity.witherskeleton;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.SkeletonSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.ai.INinja;
import fathertoast.specialmobs.common.entity.ai.goal.NinjaGoal;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;

@SpecialMob
public class NinjaWitherSkeletonEntity extends _SpecialWitherSkeletonEntity implements INinja {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<NinjaWitherSkeletonEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x333366 )
                .uniqueOverlayTexture()
                .addExperience( 2 ).pressurePlateImmune()
                .multiplyRangedCooldown( 0.5F ).rangedMaxRange( 9.0 )
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 1.2 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( MobFamily.Species<?> species ) {
        return new SkeletonSpeciesConfig( species, 0.5, 0.0 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Wither Skeleton Ninja",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addUncommonDrop( "uncommon", Blocks.INFESTED_STONE, Blocks.INFESTED_COBBLESTONE, Blocks.INFESTED_STONE_BRICKS,
                Blocks.INFESTED_CRACKED_STONE_BRICKS, Blocks.INFESTED_MOSSY_STONE_BRICKS, Blocks.INFESTED_CHISELED_STONE_BRICKS );
    }
    
    @SpecialMob.Factory
    public static EntityType.IFactory<NinjaWitherSkeletonEntity> getVariantFactory() { return NinjaWitherSkeletonEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends NinjaWitherSkeletonEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public NinjaWitherSkeletonEntity( EntityType<? extends _SpecialWitherSkeletonEntity> entityType, World world ) { super( entityType, world ); }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        goalSelector.addGoal( -9, new NinjaGoal<>( this ) );
    }
    
    /** Override to change starting equipment or stats. */
    @Override
    public void finalizeVariantSpawn( IServerWorld world, DifficultyInstance difficulty, @Nullable SpawnReason spawnReason,
                                      @Nullable ILivingEntityData groupData ) {
        setCanPickUpLoot( true );
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) { revealTo( target, true ); }
    
    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt( DamageSource source, float amount ) {
        if( super.hurt( source, amount ) ) {
            if( source.getEntity() instanceof LivingEntity ) revealTo( (LivingEntity) source.getEntity(), false );
            return true;
        }
        return false;
    }
    
    /** @return Interacts (right click) with this entity and returns the result. */
    @Override
    public ActionResultType mobInteract( PlayerEntity player, Hand hand ) {
        // Attack if the player tries to right click the "block"
        if( !level.isClientSide() && getHiddenDragon() != null ) revealTo( player, true );
        return super.mobInteract( player, hand );
    }
    
    /** Called by the player when it touches this entity. */
    @Override
    public void playerTouch( PlayerEntity player ) {
        if( !level.isClientSide() && getHiddenDragon() != null && !player.isCreative() ) revealTo( player, true );
        super.playerTouch( player );
    }
    
    /** Called each tick to update this entity. */
    @Override
    public void tick() {
        if( !level.isClientSide() ) {
            if( canHide ) {
                setHiddenDragon( NinjaGoal.pickDisguise( this ) );
            }
            else if( onGround && getHiddenDragon() == null &&
                    (getTarget() == null || getTarget() instanceof PlayerEntity && ((PlayerEntity) getTarget()).isCreative()) ) {
                canHide = true;
            }
        }
        super.tick();
    }
    
    /** Plays an appropriate step sound for this entity based on the floor block. */
    @Override
    protected void playStepSound( BlockPos pos, BlockState state ) { } // Disable
    
    /** @return The sound this entity makes idly. */
    @Override
    protected SoundEvent getAmbientSound() { return isCrouchingTiger() ? null : super.getAmbientSound(); }
    
    /** Moves this entity to a new position and rotation. */
    @Override
    public void moveTo( double x, double y, double z, float yaw, float pitch ) {
        if( !isCrouchingTiger() ) super.moveTo( x, y, z, yaw, pitch );
    }
    
    /** Sets this entity's movement. */
    @Override
    public void setDeltaMovement( Vector3d vec ) {
        if( !isCrouchingTiger() ) super.setDeltaMovement( vec );
    }
    
    /** Returns true if this entity should push and be pushed by other entities when colliding. */
    @Override
    public boolean isPushable() {
        return super.isPushable() && !isCrouchingTiger();
    }
    
    /** Sets this entity on fire for a specific duration. */
    @Override
    public void setRemainingFireTicks( int ticks ) {
        if( !isCrouchingTiger() ) super.setRemainingFireTicks( ticks );
    }
    
    /** Reveals this ninja and sets its target so that it doesn't immediately re-disguise itself. */
    public void revealTo( LivingEntity target, boolean ambush ) {
        if( getHiddenDragon() == null ) return;
        setHiddenDragon( null );
        
        if( !(target instanceof PlayerEntity && ((PlayerEntity) target).isCreative()) ) {
            setTarget( target );
            
            if( ambush ) {
                MobHelper.applyEffect( target, Effects.POISON );
                MobHelper.applyEffect( target, Effects.MOVEMENT_SLOWDOWN );
                MobHelper.applyEffect( target, Effects.BLINDNESS );
                MobHelper.removeNightVision( target );
            }
        }
    }
    
    
    //--------------- INinja Implementations ----------------
    
    /** The parameter for the ninja immobile state. */
    private static final DataParameter<Boolean> IS_HIDING = EntityDataManager.defineId( NinjaWitherSkeletonEntity.class, DataSerializers.BOOLEAN );
    /** The parameter for the ninja disguise block. */
    private static final DataParameter<Optional<BlockState>> HIDING_BLOCK = EntityDataManager.defineId( NinjaWitherSkeletonEntity.class, DataSerializers.BLOCK_STATE );
    
    private boolean canHide = true;
    
    /** Called from the Entity.class constructor to define data watcher variables. */
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define( IS_HIDING, false );
        entityData.define( HIDING_BLOCK, Optional.empty() );
    }
    
    /** @return Whether this ninja is currently immobile. */
    @Override
    public boolean isCrouchingTiger() { return getEntityData().get( IS_HIDING ); }
    
    /** Sets this ninja's immovable state. When activated, the entity is 'snapped' to the nearest block position. */
    @Override
    public void setCrouchingTiger( boolean value ) {
        if( value != isCrouchingTiger() ) {
            getEntityData().set( IS_HIDING, value );
        }
    }
    
    /** @return The block being hidden (rendered) as, or null if not hiding. */
    @Nullable
    @Override
    public BlockState getHiddenDragon() {
        if( isAlive() ) return getEntityData().get( HIDING_BLOCK ).orElse( null );
        return null;
    }
    
    /** Sets the block being hidden (rendered) as, set to null to cancel hiding. */
    @Override
    public void setHiddenDragon( @Nullable BlockState block ) {
        getEntityData().set( HIDING_BLOCK, Optional.ofNullable( block ) );
        canHide = false;
        
        // Smoke puff when emerging from disguise
        if( block == null ) {
            spawnAnim();
        }
    }
}