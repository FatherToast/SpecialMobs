package fathertoast.specialmobs.common.entity.creeper;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.species.CreeperSpeciesConfig;
import fathertoast.specialmobs.common.config.species.SpeciesConfig;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityTeleportEvent;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.UUID;
import java.util.function.Predicate;

@SpecialMob
public class EnderCreeperEntity extends _SpecialCreeperEntity implements NeutralMob {
    
    //--------------- Static Special Mob Hooks ----------------

    @SpecialMob.SpeciesReference
    public static MobFamily.Species<EnderCreeperEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xCC00FA ).weight( BestiaryInfo.DefaultWeight.LOW )
                .uniqueTextureWithEyes()
                .addExperience( 2 ).waterSensitive()
                .addToAttribute( Attributes.MAX_HEALTH, 20.0 );
    }
    
    @SpecialMob.ConfigSupplier
    public static SpeciesConfig createConfig( MobFamily.Species<?> species ) {
        return new CreeperSpeciesConfig( species, true, false, false );
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
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<EnderCreeperEntity> getVariantFactory() { return EnderCreeperEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends EnderCreeperEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public EnderCreeperEntity( EntityType<? extends _SpecialCreeperEntity> entityType, Level level ) { super( entityType, level ); }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        AIHelper.insertGoal( goalSelector, 4, new StareGoal( this ) );
        
        AIHelper.removeGoals( targetSelector, NearestAttackableTargetGoal.class );
        targetSelector.addGoal( 1, new FindPlayerGoal( this, this::isAngryAt ) );
        targetSelector.addGoal( 4, new ResetUniversalAngerTargetGoal<>( this, false ) );
    }
    
    /** Override to save data to this entity's NBT data. */
    public void addVariantSaveData( CompoundTag saveTag ) {
        addPersistentAngerSaveData( saveTag );
    }
    
    /** Override to load data from this entity's NBT data. */
    public void readVariantSaveData( CompoundTag saveTag ) {
        if( !level().isClientSide )
            readPersistentAngerSaveData( level(), saveTag );
    }
    
    
    //--------------- IAngerable Implementations ----------------
    
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds( 20, 39 );
    private int remainingPersistentAngerTime;
    private UUID persistentAngerTarget;
    
    @Override
    public void startPersistentAngerTimer() { setRemainingPersistentAngerTime( PERSISTENT_ANGER_TIME.sample( random ) ); }
    
    @Override
    public void setRemainingPersistentAngerTime( int ticks ) { remainingPersistentAngerTime = ticks; }
    
    @Override
    public int getRemainingPersistentAngerTime() { return remainingPersistentAngerTime; }
    
    @Override
    public void setPersistentAngerTarget( @Nullable UUID id ) { persistentAngerTarget = id; }
    
    @Override
    public UUID getPersistentAngerTarget() { return persistentAngerTarget; }
    
    
    //--------------- Enderman Implementations ----------------
    
    private static final EntityDataAccessor<Boolean> DATA_CREEPY = SynchedEntityData.defineId( EnderCreeperEntity.class, EntityDataSerializers.BOOLEAN );
    private static final EntityDataAccessor<Boolean> DATA_STARED_AT = SynchedEntityData.defineId( EnderCreeperEntity.class, EntityDataSerializers.BOOLEAN );
    
    private int lastStareSound = Integer.MIN_VALUE;
    private int targetChangeTime;
    
    /** Called from the Entity.class constructor to define data watcher variables. */
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define( DATA_CREEPY, false );
        entityData.define( DATA_STARED_AT, false );
    }
    
    public boolean isCreepy() { return entityData.get( DATA_CREEPY ); }
    
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
                level().playLocalSound( getX(), getEyeY(), getZ(), SoundEvents.ENDERMAN_STARE, getSoundSource(),
                        2.5F, 1.0F, false );
            }
        }
    }
    
    /** Called when a data watcher parameter is changed. */
    @Override
    public void onSyncedDataUpdated( EntityDataAccessor<?> parameter ) {
        if( DATA_CREEPY.equals( parameter ) && hasBeenStaredAt() && level().isClientSide ) {
            playStareSound();
        }
        super.onSyncedDataUpdated( parameter );
    }
    
    /** @return True if the player is looking at this "enderman". */
    private boolean isLookingAtMe( Player player ) {
        final ItemStack playerHelm = player.getInventory().armor.get( 3 );
        try {
            if( playerHelm.isEnderMask( player, null ) ) return false;
        }
        catch( NullPointerException ex ) {
            SpecialMobs.LOG.error( "Helmet '{}' does not support nullable enderman for ::isEnderMask check!",
                    playerHelm.getDescriptionId() );
            return false;
        }
        
        final Vec3 playerViewVec = player.getViewVector( 1.0F ).normalize();
        final Vec3 playerToThisVec = new Vec3(
                getX() - player.getX(),
                getEyeY() - player.getEyeY(),
                getZ() - player.getZ() );
        final double distance = playerToThisVec.length();
        final double viewProjection = playerViewVec.dot( playerToThisVec.normalize() );
        return viewProjection > 1.0 - 0.025 / distance && player.hasLineOfSight( this );
    }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        if( level().isClientSide ) {
            for( int i = 0; i < 2; ++i ) {
                level().addParticle( ParticleTypes.PORTAL,
                        getRandomX( 0.5 ), getRandomY() - 0.25, getRandomZ( 0.5 ),
                        (random.nextDouble() - 0.5) * 2.0, -random.nextDouble(), (random.nextDouble() - 0.5) * 2.0 );
            }
        }
        //jumping = false;
        if( !level().isClientSide ) {
            updatePersistentAnger( (ServerLevel) level(), true );
        }
        super.aiStep();
    }
    
    @Override
    protected void customServerAiStep() {
        if( level().isDay() && tickCount >= targetChangeTime + 600 ) {
            final float brightness = getLightLevelDependentMagicValue();
            if( brightness > 0.5F && level().canSeeSky( blockPosition() ) && random.nextFloat() * 30.0F < (brightness - 0.4F) * 2.0F ) {
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

        if( source.is( DamageTypeTags.IS_PROJECTILE ) ) {
            for( int i = 0; i < 64; ++i ) {
                if( teleport() ) return true;
            }
            return false;
        }
        
        boolean success = super.hurt( source, amount );
        if( !level().isClientSide() && !(source.getEntity() instanceof LivingEntity) && random.nextInt( 10 ) != 0 ) {
            teleport();
        }
        return success;
    }
    
    /** @return Teleports this "enderman" to a random nearby position; returns true if successful. */
    @SuppressWarnings( "UnusedReturnValue" ) // Keep return value to mirror enderman impl
    protected boolean teleport() {
        if( level().isClientSide() || !isAlive() ) return false;
        
        final double x = getX() + (random.nextDouble() - 0.5) * 64.0;
        final double y = getY() + (double) (random.nextInt( 64 ) - 32);
        final double z = getZ() + (random.nextDouble() - 0.5) * 64.0;
        return teleport( x, y, z );
    }
    
    /** @return Teleports this "enderman" towards another entity; returns true if successful. */
    protected boolean teleportTowards( Entity target ) {
        final Vec3 directionFromTarget = new Vec3(
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
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos( x, y, z );
        
        while( pos.getY() > 0 && !level().getBlockState( pos ).blocksMotion() ) {
            pos.move( Direction.DOWN );
        }
        
        final BlockState block = level().getBlockState( pos );
        if( !block.blocksMotion() || block.getFluidState().is( FluidTags.WATER ) ) return false;
        
        EntityTeleportEvent.EnderEntity event = ForgeEventFactory.onEnderTeleport( this, x, y, z );
        if( event.isCanceled() ) return false;
        
        final boolean success = randomTeleport( event.getTargetX(), event.getTargetY(), event.getTargetZ(), true );
        if( success && !isSilent() ) {
            level().playSound( null, xo, yo, zo, SoundEvents.ENDERMAN_TELEPORT, getSoundSource(),
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
            return target instanceof Player && target.distanceToSqr( creeper ) <= 256.0 && creeper.isLookingAtMe( (Player) target );
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
    static class FindPlayerGoal extends NearestAttackableTargetGoal<Player> {
        
        private final EnderCreeperEntity creeper;
        private final TargetingConditions startAggroTargetConditions;
        private final TargetingConditions continueAggroTargetConditions = TargetingConditions.forCombat().ignoreLineOfSight();
        
        private Player pendingTarget;
        private int aggroTime;
        private int teleportTime;
        
        public FindPlayerGoal( EnderCreeperEntity entity, @Nullable Predicate<LivingEntity> targetSelector ) {
            super( entity, Player.class, 10, false, false, targetSelector );
            creeper = entity;
            startAggroTargetConditions = TargetingConditions.forCombat().range( getFollowDistance() ).selector(
                    // Safe cast, we should only be searching for players anyways
                    ( target ) -> entity.isLookingAtMe( (Player) target) );
        }
        
        /** @return Returns true if this AI can be activated. */
        @Override
        public boolean canUse() {
            pendingTarget = creeper.level().getNearestPlayer( startAggroTargetConditions, creeper );
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
                    if( creeper.isLookingAtMe( (Player) target ) ) {
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