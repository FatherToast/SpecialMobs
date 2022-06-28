package fathertoast.specialmobs.common.entity.witherskeleton;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.ai.INinja;
import fathertoast.specialmobs.common.entity.ai.NinjaGoal;
import fathertoast.specialmobs.common.util.AttributeHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class NinjaWitherSkeletonEntity extends _SpecialWitherSkeletonEntity implements INinja {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<NinjaWitherSkeletonEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0x333366 );
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return AttributeHelper.of( _SpecialWitherSkeletonEntity.createAttributes() )
                .multAttribute( Attributes.MOVEMENT_SPEED, 1.2 )
                .build();
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
    
    @SpecialMob.Constructor
    public NinjaWitherSkeletonEntity( EntityType<? extends _SpecialWitherSkeletonEntity> entityType, World world ) {
        super( entityType, world );
        xpReward += 2;
    }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        setRangedAI( 1.0, 10, 9.0F );
        goalSelector.addGoal( -9, new NinjaGoal<>( this ) );
    }
    
    /** Called during spawn finalization to set starting equipment. */
    @Override
    protected void populateDefaultEquipmentSlots( DifficultyInstance difficulty ) {
        super.populateDefaultEquipmentSlots( difficulty );
        setCanPickUpLoot( true );
    }
    
    /** Override to change this entity's chance to spawn with a bow. */
    @Override
    protected double getVariantBowChance() { return 0.5; }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( Entity target ) { revealTo( target, true ); }
    
    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt( DamageSource source, float amount ) {
        if( super.hurt( source, amount ) ) {
            revealTo( source.getEntity(), false );
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
    public void revealTo( @Nullable Entity target, boolean ambush ) {
        if( getHiddenDragon() == null ) return;
        setHiddenDragon( null );
        
        if( target instanceof LivingEntity && !(target instanceof PlayerEntity && ((PlayerEntity) target).isCreative()) ) {
            final LivingEntity livingTarget = (LivingEntity) target;
            setTarget( livingTarget );
            
            if( ambush ) {
                final int duration = MobHelper.getDebuffDuration( level.getDifficulty() );
                
                livingTarget.addEffect( new EffectInstance( Effects.POISON, duration ) );
                livingTarget.addEffect( new EffectInstance( Effects.MOVEMENT_SLOWDOWN, duration ) );
                livingTarget.addEffect( new EffectInstance( Effects.BLINDNESS, duration ) );
                livingTarget.removeEffect( Effects.NIGHT_VISION ); // Prevent blind + night vision combo (black screen)
            }
        }
    }
    
    private static final ResourceLocation[] TEXTURES = {
            new ResourceLocation( "textures/entity/skeleton/wither_skeleton.png" ),
            null,
            GET_TEXTURE_PATH( "ninja_overlay" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
    
    
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
        if( block == null ) spawnAnim();
    }
}