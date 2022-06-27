package fathertoast.specialmobs.common.entity.skeleton;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.ai.INinja;
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
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class NinjaSkeletonEntity extends _SpecialSkeletonEntity implements INinja {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<NinjaSkeletonEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0x333366 );
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return AttributeHelper.of( _SpecialSkeletonEntity.createAttributes() )
                .multAttribute( Attributes.MOVEMENT_SPEED, 1.2 )
                .build();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Skeleton Ninja",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addUncommonDrop( "uncommon", Blocks.INFESTED_STONE, Blocks.INFESTED_COBBLESTONE, Blocks.INFESTED_STONE_BRICKS,
                Blocks.INFESTED_CRACKED_STONE_BRICKS, Blocks.INFESTED_MOSSY_STONE_BRICKS, Blocks.INFESTED_CHISELED_STONE_BRICKS );
    }
    
    @SpecialMob.Constructor
    public NinjaSkeletonEntity( EntityType<? extends _SpecialSkeletonEntity> entityType, World world ) {
        super( entityType, world );
        xpReward += 2;
    }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        setRangedAI( 1.0, 10, 9.0F );
        
        //TODO AIHelper.insertGoalReverse( goalSelector, getVariantAttackPriority() - 1, null );
    }
    
    /** Called during spawn finalization to set starting equipment. */
    @Override
    protected void populateDefaultEquipmentSlots( DifficultyInstance difficulty ) {
        super.populateDefaultEquipmentSlots( difficulty );
        setCanPickUpLoot( true );
    }
    
    /** Override to change this entity's chance to spawn with a melee weapon. */
    @Override
    protected double getVariantMeleeChance() { return 0.5; }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( Entity target ) {
        revealTo( target );
    }
    
    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt( DamageSource source, float amount ) {
        if( super.hurt( source, amount ) ) {
            revealTo( source.getEntity() );
            return true;
        }
        return false;
    }
    
    /** @return Interacts (right click) with this entity and returns the result. */
    @Override
    public ActionResultType mobInteract( PlayerEntity player, Hand hand ) {
        // Attack if the player tries to right click the "block"
        if( !level.isClientSide() && getDisguiseBlock() != null ) revealTo( player );
        return super.mobInteract( player, hand );
    }
    
    /** Called each tick to update this entity. */
    @Override
    public void tick() {
        // TODO can this be moved to the ninja AI?
        if( !level.isClientSide() ) {
            if( canHide ) {
                //EntityAINinja.startHiding( this ); TODO
                this.setHiding( true );
                this.setDisguiseBlock( Blocks.DIRT.defaultBlockState() );
            }
            else if( onGround && getDisguiseBlock() == null &&
                    (getTarget() == null || getTarget() instanceof PlayerEntity && ((PlayerEntity) getTarget()).isCreative()) ) {
                canHide = true;
            }
        }
        super.tick();
    }
    
    //    // Moves this entity.
    //    @Override TODO
    //    public void move( MoverType type, double x, double y, double z ) {
    //        if( isHiding() && type != MoverType.PISTON ) {
    //            motionY = 0.0;
    //        }
    //        else {
    //            super.move( type, x, y, z );
    //        }
    //    }
    
    /** Returns true if this entity should push and be pushed by other entities when colliding. */
    @Override
    public boolean isPushable() {
        return super.isPushable() && !isHiding();
    }
    
    /** Sets this entity on fire for a specific duration. */
    @Override
    public void setRemainingFireTicks( int ticks ) {
        if( !isHiding() ) super.setRemainingFireTicks( ticks );
    }
    
    /** Reveals this ninja and sets its target so that it doesn't immediately re-disguise itself. */
    public void revealTo( @Nullable Entity target ) {
        setDisguiseBlock( null );
        if( target instanceof LivingEntity ) setTarget( (LivingEntity) target );
    }
    
    private static final ResourceLocation[] TEXTURES = {
            new ResourceLocation( "textures/entity/skeleton/skeleton.png" ),
            null,
            GET_TEXTURE_PATH( "ninja_overlay" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
    
    
    //--------------- INinja Implementations ----------------
    
    /** The parameter for the ninja immobile state. */
    private static final DataParameter<Boolean> IS_HIDING = EntityDataManager.defineId( NinjaSkeletonEntity.class, DataSerializers.BOOLEAN );
    /** The parameter for the ninja disguise block. */
    private static final DataParameter<Optional<BlockState>> HIDING_BLOCK = EntityDataManager.defineId( NinjaSkeletonEntity.class, DataSerializers.BLOCK_STATE );
    
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
    public boolean isHiding() { return getEntityData().get( IS_HIDING ); }
    
    /** Sets this ninja's immovable state. When activated, the entity is 'snapped' to the nearest block position. */
    @Override
    public void setHiding( boolean value ) {
        if( value != isHiding() ) {
            getEntityData().set( IS_HIDING, value );
            if( value ) {
                clearFire();
                moveTo( Math.floor( getX() ) + 0.5, Math.floor( getY() ), Math.floor( getZ() ) + 0.5 );
            }
        }
    }
    
    /** @return The block being hidden (rendered) as, or null if not hiding. */
    @Nullable
    @Override
    public BlockState getDisguiseBlock() {
        if( isAlive() ) return getEntityData().get( HIDING_BLOCK ).orElse( null );
        return null;
    }
    
    /** Sets the block being hidden (rendered) as, set to null to cancel hiding. */
    @Override
    public void setDisguiseBlock( @Nullable BlockState block ) {
        getEntityData().set( HIDING_BLOCK, Optional.ofNullable( block ) );
        canHide = false;
        
        // Smoke puff when emerging from disguise
        if( block == null ) {
            //spawnExplosionParticle(); TODO
        }
    }
}