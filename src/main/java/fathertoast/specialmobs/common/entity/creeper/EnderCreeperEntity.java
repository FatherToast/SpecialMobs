package fathertoast.specialmobs.common.entity.creeper;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.util.AttributeHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.ResetAngerGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.EntityTeleportEvent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumSet;
import java.util.UUID;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class EnderCreeperEntity extends _SpecialCreeperEntity implements IAngerable {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0xCC00FA, BestiaryInfo.BaseWeight.LOW );
        //TODO theme - the end
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return AttributeHelper.of( _SpecialCreeperEntity.createAttributes() )
                .addAttribute( Attributes.MAX_HEALTH, 20.0 )
                .build();
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Endercreeper",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addLootTable( "common", EntityType.ENDERMAN.getDefaultLootTable() );
    }
    
    @SpecialMob.Constructor
    public EnderCreeperEntity( EntityType<? extends _SpecialCreeperEntity> entityType, World world ) {
        super( entityType, world );
        getSpecialData().setDamagedByWater( true );
        setCannotExplodeWhileWet( true );
        xpReward += 2;
    }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        AIHelper.insertGoal( goalSelector, 4, new StareGoal( this ) );
        
        AIHelper.removeGoals( targetSelector, NearestAttackableTargetGoal.class );
        targetSelector.addGoal( 1, new FindPlayerGoal( this, this::isAngryAt ) );
        targetSelector.addGoal( 4, new ResetAngerGoal<>( this, false ) );
    }
    
    /** Override to save data to this entity's NBT data. */
    public void addVariantSaveData( CompoundNBT saveTag ) {
        addPersistentAngerSaveData( saveTag );
    }
    
    /** Override to load data from this entity's NBT data. */
    public void readVariantSaveData( CompoundNBT saveTag ) {
        if( !level.isClientSide )
            readPersistentAngerSaveData( (ServerWorld) level, saveTag );
    }
    
    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "ender" ),
            GET_TEXTURE_PATH( "ender_eyes" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
    
    
    //--------------- IAngerable Implementations ----------------
    
    private static final RangedInteger PERSISTENT_ANGER_TIME = TickRangeConverter.rangeOfSeconds( 20, 39 );
    private int remainingPersistentAngerTime;
    private UUID persistentAngerTarget;
    
    @Override
    public void startPersistentAngerTimer() { setRemainingPersistentAngerTime( PERSISTENT_ANGER_TIME.randomValue( random ) ); }
    
    @Override
    public void setRemainingPersistentAngerTime( int ticks ) { remainingPersistentAngerTime = ticks; }
    
    @Override
    public int getRemainingPersistentAngerTime() { return remainingPersistentAngerTime; }
    
    @Override
    public void setPersistentAngerTarget( @Nullable UUID id ) { persistentAngerTarget = id; }
    
    @Override
    public UUID getPersistentAngerTarget() { return persistentAngerTarget; }
    
    
    //--------------- Enderman Implementations ----------------
    
    private static final DataParameter<Boolean> DATA_CREEPY = EntityDataManager.defineId( EnderCreeperEntity.class, DataSerializers.BOOLEAN );
    private static final DataParameter<Boolean> DATA_STARED_AT = EntityDataManager.defineId( EnderCreeperEntity.class, DataSerializers.BOOLEAN );
    
    private int lastStareSound = Integer.MIN_VALUE;
    private int targetChangeTime;
    
    /** Called from the Entity.class constructor to define data watcher variables. */
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define( DATA_CREEPY, false );
        entityData.define( DATA_STARED_AT, false );
    }
    
    private boolean hasBeenStaredAt() { return entityData.get( DATA_STARED_AT ); }
    
    private void setBeingStaredAt() { entityData.set( DATA_STARED_AT, true ); }
    
    /** Sets this entity's target. */
    @Override
    public void setTarget( @Nullable LivingEntity target ) {
        if( target == null ) {
            targetChangeTime = 0;
            entityData.set( DATA_CREEPY, false );
            entityData.set( DATA_STARED_AT, false );
        }
        else {
            targetChangeTime = tickCount;
            entityData.set( DATA_CREEPY, true );
        }
        super.setTarget( target );
    }
    
    private void playStareSound() {
        if( tickCount >= lastStareSound + 400 ) {
            lastStareSound = tickCount;
            if( !isSilent() ) {
                level.playLocalSound( getX(), getEyeY(), getZ(), SoundEvents.ENDERMAN_STARE, getSoundSource(),
                        2.5F, 1.0F, false );
            }
        }
    }
    
    /** Called when a data watcher parameter is changed. */
    @Override
    public void onSyncedDataUpdated( DataParameter<?> parameter ) {
        if( DATA_CREEPY.equals( parameter ) && hasBeenStaredAt() && level.isClientSide ) {
            playStareSound();
        }
        super.onSyncedDataUpdated( parameter );
    }
    
    /** @return True if the player is looking at this "enderman". */
    private boolean isLookingAtMe( PlayerEntity player ) {
        final ItemStack playerHelm = player.inventory.armor.get( 3 );
        try {
            if( playerHelm.isEnderMask( player, null ) ) return false;
        }
        catch( NullPointerException ex ) {
            SpecialMobs.LOG.error( "Helmet '{}' does not support nullable enderman for ::isEnderMask check!",
                    playerHelm.getDescriptionId() );
            return false;
        }
        
        final Vector3d playerViewVec = player.getViewVector( 1.0F ).normalize();
        final Vector3d playerToThisVec = new Vector3d(
                getX() - player.getX(),
                getEyeY() - player.getEyeY(),
                getZ() - player.getZ() );
        final double distance = playerToThisVec.length();
        final double viewProjection = playerViewVec.dot( playerToThisVec.normalize() );
        return viewProjection > 1.0 - 0.025 / distance && player.canSee( this );
    }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        if( level.isClientSide ) {
            for( int i = 0; i < 2; ++i ) {
                level.addParticle( ParticleTypes.PORTAL,
                        getRandomX( 0.5 ), getRandomY() - 0.25, getRandomZ( 0.5 ),
                        (random.nextDouble() - 0.5) * 2.0, -random.nextDouble(), (random.nextDouble() - 0.5) * 2.0 );
            }
        }
        //jumping = false;
        if( !level.isClientSide ) {
            updatePersistentAnger( (ServerWorld) level, true );
        }
        super.aiStep();
    }
    
    @Override
    protected void customServerAiStep() {
        if( level.isDay() && tickCount >= targetChangeTime + 600 ) {
            final float brightness = getBrightness();
            if( brightness > 0.5F && level.canSeeSky( blockPosition() ) && random.nextFloat() * 30.0F < (brightness - 0.4F) * 2.0F ) {
                setTarget( null );
                teleport();
            }
        }
        super.customServerAiStep();
    }
    
    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt( DamageSource source, float amount ) {
        if( isInvulnerableTo( source ) ) return false;
        
        if( source instanceof IndirectEntityDamageSource ) {
            for( int i = 0; i < 64; ++i ) {
                if( teleport() ) return true;
            }
            return false;
        }
        
        boolean success = super.hurt( source, amount );
        if( !level.isClientSide() && !(source.getEntity() instanceof LivingEntity) && random.nextInt( 10 ) != 0 ) {
            teleport();
        }
        return success;
    }
    
    /** @return Teleports this "enderman" to a random nearby position; returns true if successful. */
    @SuppressWarnings( "UnusedReturnValue" ) // Keep return value to mirror enderman impl
    protected boolean teleport() {
        if( level.isClientSide() || !isAlive() ) return false;
        
        final double x = getX() + (random.nextDouble() - 0.5) * 64.0;
        final double y = getY() + (double) (random.nextInt( 64 ) - 32);
        final double z = getZ() + (random.nextDouble() - 0.5) * 64.0;
        return teleport( x, y, z );
    }
    
    /** @return Teleports this "enderman" towards another entity; returns true if successful. */
    protected boolean teleportTowards( Entity target ) {
        final Vector3d directionFromTarget = new Vector3d(
                getX() - target.getX(),
                getY( 0.5 ) - target.getEyeY(),
                getZ() - target.getZ() )
                .normalize();
        
        final double x = getX() + (random.nextDouble() - 0.5) * 8.0 - directionFromTarget.x * 16.0;
        final double y = getY() + (double) (random.nextInt( 16 ) - 8) - directionFromTarget.y * 16.0;
        final double z = getZ() + (random.nextDouble() - 0.5) * 8.0 - directionFromTarget.z * 16.0;
        return teleport( x, y, z );
    }
    
    /** @return Teleports this "enderman" to a new position; returns true if successful. */
    protected boolean teleport( double x, double y, double z ) {
        final BlockPos.Mutable pos = new BlockPos.Mutable( x, y, z );
        
        while( pos.getY() > 0 && !level.getBlockState( pos ).getMaterial().blocksMotion() ) {
            pos.move( Direction.DOWN );
        }
        
        final BlockState block = level.getBlockState( pos );
        if( !block.getMaterial().blocksMotion() || block.getFluidState().is( FluidTags.WATER ) ) return false;
        
        EntityTeleportEvent.EnderEntity event = ForgeEventFactory.onEnderTeleport( this, x, y, z );
        if( event.isCanceled() ) return false;
        
        final boolean success = randomTeleport( event.getTargetX(), event.getTargetY(), event.getTargetZ(), true );
        if( success && !isSilent() ) {
            level.playSound( null, xo, yo, zo, SoundEvents.ENDERMAN_TELEPORT, getSoundSource(),
                    1.0F, 1.0F );
            playSound( SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F );
        }
        return success;
    }
    
    /** The enderman stare goal adapted for use by a creeper. */
    static class StareGoal extends Goal {
        
        private final EnderCreeperEntity creeper;
        
        private LivingEntity target;
        
        public StareGoal( EnderCreeperEntity entity ) {
            creeper = entity;
            setFlags( EnumSet.of( Goal.Flag.JUMP, Goal.Flag.MOVE ) );
        }
        
        /** @return Returns true if this AI can be activated. */
        @Override
        public boolean canUse() {
            target = creeper.getTarget();
            return target instanceof PlayerEntity && target.distanceToSqr( creeper ) <= 256.0 && creeper.isLookingAtMe( (PlayerEntity) target );
        }
        
        /** Called when this AI is activated. */
        @Override
        public void start() { creeper.getNavigation().stop(); }
        
        /** Called each tick while this AI is active. */
        @Override
        public void tick() {
            creeper.getLookControl().setLookAt( target.getX(), target.getEyeY(), target.getZ() );
        }
    }
    
    /** The enderman find player goal adapted for use by a creeper. */
    static class FindPlayerGoal extends NearestAttackableTargetGoal<PlayerEntity> {
        
        private final EnderCreeperEntity creeper;
        private final EntityPredicate startAggroTargetConditions;
        private final EntityPredicate continueAggroTargetConditions = new EntityPredicate().allowUnseeable();
        
        private PlayerEntity pendingTarget;
        private int aggroTime;
        private int teleportTime;
        
        public FindPlayerGoal( EnderCreeperEntity entity, @Nullable Predicate<LivingEntity> targetSelector ) {
            super( entity, PlayerEntity.class, 10, false, false, targetSelector );
            creeper = entity;
            startAggroTargetConditions = new EntityPredicate().range( getFollowDistance() ).selector(
                    ( player ) -> entity.isLookingAtMe( (PlayerEntity) player ) );
        }
        
        /** @return Returns true if this AI can be activated. */
        @Override
        public boolean canUse() {
            pendingTarget = creeper.level.getNearestPlayer( startAggroTargetConditions, creeper );
            return pendingTarget != null;
        }
        
        /** Called when this AI is activated. */
        @Override
        public void start() {
            aggroTime = 5;
            teleportTime = 0;
            creeper.setBeingStaredAt();
        }
        
        /** Called when this AI is deactivated. */
        @Override
        public void stop() {
            pendingTarget = null;
            super.stop();
        }
        
        /** @return Called each update while active and returns true if this AI can remain active. */
        @Override
        public boolean canContinueToUse() {
            if( pendingTarget != null ) {
                if( !creeper.isLookingAtMe( pendingTarget ) ) return false;
                
                creeper.lookAt( pendingTarget, 10.0F, 10.0F );
                return true;
            }
            return target != null && continueAggroTargetConditions.test( creeper, target ) || super.canContinueToUse();
        }
        
        /** Called each tick while this AI is active. */
        @Override
        public void tick() {
            if( creeper.getTarget() == null ) {
                super.setTarget( null );
            }
            if( pendingTarget != null ) {
                if( --aggroTime <= 0 ) {
                    target = pendingTarget;
                    pendingTarget = null;
                    super.start();
                }
            }
            else {
                if( target != null && !creeper.isPassenger() ) {
                    if( creeper.isLookingAtMe( (PlayerEntity) target ) ) {
                        if( target.distanceToSqr( creeper ) < 16.0 ) {
                            creeper.teleport();
                        }
                        teleportTime = 0;
                    }
                    else if( target.distanceToSqr( creeper ) > 256.0 && teleportTime++ >= 30 && creeper.teleportTowards( target ) ) {
                        teleportTime = 0;
                    }
                }
                super.tick();
            }
        }
    }
}