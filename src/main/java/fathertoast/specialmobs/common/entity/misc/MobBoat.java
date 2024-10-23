package fathertoast.specialmobs.common.entity.misc;

import com.google.common.collect.Lists;
import fathertoast.specialmobs.common.core.register.SMEntities;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Copy of {@link Boat} with some changes to allow mobs to control it.
 */
public class MobBoat extends Entity implements IEntityAdditionalSpawnData {

    private static final EntityDataAccessor<Integer> DATA_ID_HURT = SynchedEntityData.defineId(MobBoat.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ID_HURTDIR = SynchedEntityData.defineId(MobBoat.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_ID_DAMAGE = SynchedEntityData.defineId(MobBoat.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_ID_TYPE = SynchedEntityData.defineId(MobBoat.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ID_PADDLE_LEFT = SynchedEntityData.defineId(MobBoat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_ID_PADDLE_RIGHT = SynchedEntityData.defineId(MobBoat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ID_BUBBLE_TIME = SynchedEntityData.defineId(MobBoat.class, EntityDataSerializers.INT);

    private final float[] paddlePositions = new float[2];
    private float invFriction;
    private float outOfControlTicks;
    public float deltaRotation;
    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYRot;
    private double lerpXRot;
    private boolean inputLeft;
    private boolean inputRight;
    private boolean inputUp;
    private boolean inputDown;
    private double waterLevel;
    private float landFriction;
    private MobBoat.Status status;
    private MobBoat.Status oldStatus;
    private double lastYd;
    private boolean isAboveBubbleColumn;
    private boolean bubbleColumnDirectionIsDown;
    private float bubbleMultiplier;
    private float bubbleAngle;
    private float bubbleAngleO;
    private int timeExistedNoPassengers;


    public MobBoat(EntityType<? extends MobBoat> entityType, Level level) {
        super(entityType, level);
        blocksBuilding = true;
    }

    public MobBoat(Level level, double x, double y, double z) {
        this(SMEntities.MOB_BOAT.get(), level);
        setPos(x, y, z);
        xo = x;
        yo = y;
        zo = z;
    }

    public static Boat copyFromMobBoat( MobBoat mobBoat ) {
        Boat boat = new Boat( mobBoat.level(), mobBoat.getX(), mobBoat.getY(), mobBoat.getZ() );

        boat.setXRot( mobBoat.getXRot() );
        boat.setYRot( mobBoat.getYRot() );
        boat.xo = mobBoat.xo;
        boat.yo = mobBoat.yo;
        boat.zo = mobBoat.zo;
        boat.deltaRotation = mobBoat.deltaRotation;

        boat.setVariant( mobBoat.getVariant() );

        return boat;
    }

    @Override
    protected float getEyeHeight( Pose pose, EntityDimensions dimensions ) {
        return dimensions.height;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define( DATA_ID_HURT, 0 );
        entityData.define( DATA_ID_HURTDIR, 1 );
        entityData.define( DATA_ID_DAMAGE, 0.0F );
        entityData.define( DATA_ID_TYPE, Boat.Type.OAK.ordinal() );
        entityData.define( DATA_ID_PADDLE_LEFT, false );
        entityData.define( DATA_ID_PADDLE_RIGHT, false );
        entityData.define( DATA_ID_BUBBLE_TIME, 0 );
    }

    @Override
    public boolean canCollideWith( Entity entity ) {
        return Boat.canVehicleCollide( this, entity );
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected Vec3 getRelativePortalPosition( Direction.Axis axis, BlockUtil.FoundRectangle rectangle ) {
        return LivingEntity.resetForwardDirectionOfRelativePortalPosition( super.getRelativePortalPosition( axis, rectangle ) );
    }

    @Override
    public double getPassengersRidingOffset() {
        return getVariant() == Boat.Type.BAMBOO ? 0.25D : -0.1D;
    }

    @Override
    public boolean hurt( DamageSource damageSource, float damage ) {
        if ( isInvulnerableTo( damageSource ) ) {
            return false;
        }
        else if ( !level().isClientSide && !isRemoved() ) {
            setHurtDir( -getHurtDir() );
            setHurtTime( 10 );
            setDamage( getDamage() + damage * 10.0F );
            markHurt();
            gameEvent( GameEvent.ENTITY_DAMAGE, damageSource.getEntity() );
            boolean instabreak = damageSource.getEntity() instanceof Player player && player.getAbilities().instabuild;

            if ( instabreak || getDamage() > 40.0F ) {
                if ( !instabreak && level().getGameRules().getBoolean( GameRules.RULE_DOENTITYDROPS ) ) {
                    destroy( damageSource );
                }
                discard();
            }
            return true;
        }
        else {
            return true;
        }
    }

    protected void destroy( DamageSource damageSource ) {
        spawnAtLocation( getDropItem() );
    }

    @Override
    public void onAboveBubbleCol( boolean dragDown ) {
        if ( !level().isClientSide ) {
            isAboveBubbleColumn = true;
            bubbleColumnDirectionIsDown = dragDown;

            if ( getBubbleTime() == 0 ) {
                setBubbleTime( 60 );
            }
        }
        level().addParticle(
                ParticleTypes.SPLASH,
                getX() + (double) random.nextFloat(),
                getY() + 0.7D,
                getZ() + (double) random.nextFloat(),
                0.0D, 0.0D, 0.0D
        );

        if ( random.nextInt( 20 ) == 0 ) {
            level().playLocalSound( getX(), getY(), getZ(), getSwimSplashSound(), getSoundSource(), 1.0F, 0.8F + 0.4F * random.nextFloat(), false );
            gameEvent( GameEvent.SPLASH, getControllingPassenger() );
        }
    }

    @Override
    public void push( Entity entity ) {
        if ( entity instanceof Boat || entity instanceof MobBoat ) {
            if ( entity.getBoundingBox().minY < getBoundingBox().maxY ) {
                super.push( entity );
            }
        }
        else if ( entity.getBoundingBox().minY <= getBoundingBox().minY ) {
            super.push( entity );
        }
    }

    public Item getDropItem() {
        return switch ( getVariant() ) {
            case SPRUCE -> Items.SPRUCE_BOAT;
            case BIRCH -> Items.BIRCH_BOAT;
            case JUNGLE -> Items.JUNGLE_BOAT;
            case ACACIA -> Items.ACACIA_BOAT;
            case CHERRY -> Items.CHERRY_BOAT;
            case DARK_OAK -> Items.DARK_OAK_BOAT;
            case MANGROVE -> Items.MANGROVE_BOAT;
            case BAMBOO -> Items.BAMBOO_RAFT;
            default -> Items.OAK_BOAT;
        };
    }

    @Override
    public void animateHurt( float yaw ) {
        setHurtDir( -getHurtDir() );
        setHurtTime( 10 );
        setDamage( getDamage() * 11.0F );
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public void lerpTo( double lerpX, double lerpY, double lerpZ, float lerpYRot, float lerpXRot, int i, boolean b ) {
        this.lerpX = lerpX;
        this.lerpY = lerpY;
        this.lerpZ = lerpZ;
        this.lerpYRot = lerpYRot;
        this.lerpXRot = lerpXRot;
        this.lerpSteps = 10;
    }

    @Override
    public Direction getMotionDirection() {
        return getDirection().getClockWise();
    }

    @Override
    public void tick() {
        oldStatus = status;
        status = getStatus();

        if ( status != Status.UNDER_WATER && status != Status.UNDER_FLOWING_WATER ) {
            outOfControlTicks = 0.0F;
        }
        else {
            ++outOfControlTicks;
        }

        if ( !level().isClientSide && outOfControlTicks >= 60.0F ) {
            ejectPassengers();
        }

        if ( getHurtTime() > 0 ) {
            setHurtTime( getHurtTime() - 1 );
        }

        if ( getDamage() > 0.0F ) {
            setDamage( getDamage() - 1.0F );
        }
        super.tick();
        tickLerp();

        // As long as there as a living entity in the "control" seat, allow the boat to be controlled
        if ( getControllingPassenger() != null ) {
            floatBoat();

            if ( level().isClientSide ) {
                controlBoat();
                level().sendPacketToServer( new ServerboundPaddleBoatPacket( getPaddleState( 0 ), getPaddleState( 1 ) ) );
            }
            move( MoverType.SELF, getDeltaMovement() );
        }
        else {
            setDeltaMovement(Vec3.ZERO);
        }

        // Silently replace mob boat with equivalent vanilla boat if appropriate
        if ( timeExistedNoPassengers > 60 ) {
            ejectPassengers(); // Just in case
            discard();

            if (!level().isClientSide) {
                Boat boat = copyFromMobBoat(this);
                level().addFreshEntity(boat);
                return;
            }
            return;
        }
        tickBubbleColumn();

        for ( int i = 0; i <= 1; ++i ) {
            if ( getPaddleState( i ) ) {
                if ( !isSilent()
                        && (double)( paddlePositions[i] % ( (float) Math.PI * 2F)) <= (double)( (float) Math.PI / 4F)
                        && (double)( ( paddlePositions[i] + ( (float) Math.PI / 8F)) % ( (float) Math.PI * 2F)) >= (double)( (float) Math.PI / 4F)) {

                    SoundEvent paddleSound = getPaddleSound();

                    if ( paddleSound != null ) {
                        Vec3 viewVec = getViewVector( 1.0F );
                        double xOffset = i == 1 ? -viewVec.z : viewVec.z;
                        double zOffset = i == 1 ? viewVec.x : -viewVec.x;

                        level().playSound(
                                null,
                                getX() + xOffset,
                                getY(),
                                getZ() + zOffset,
                                paddleSound,
                                getSoundSource(),
                                1.0F, 0.8F + 0.4F * random.nextFloat()
                        );
                    }
                }
                paddlePositions[i] += ( (float) Math.PI / 8F );
            }
            else {
                paddlePositions[i] = 0.0F;
            }
        }
        checkInsideBlocks();

        // Like vanilla boats, make nearby mobs auto-mount the boat
        List<Entity> nearbyEntities = level().getEntities( this, getBoundingBox().inflate( 0.2D, -0.01D, 0.2D ), EntitySelector.pushableBy( this ) );

        if ( !nearbyEntities.isEmpty() ) {
            for ( Entity entity : nearbyEntities ) {
                if ( !entity.hasPassenger( this ) ) {
                    if ( !level().isClientSide && getPassengers().size() < getMaxPassengers()
                            && !entity.isPassenger()
                            && hasEnoughSpaceFor( entity )
                            && entity instanceof LivingEntity
                            && !( entity instanceof WaterAnimal )
                            && !( entity instanceof Player ) ) {

                        entity.startRiding( this );
                    }
                    else {
                        push( entity );
                    }
                }
            }
        }

        if ( !level().isClientSide ) {
            if (getPassengers().isEmpty()) {
                ++timeExistedNoPassengers;
            }
            else {
                timeExistedNoPassengers = 0;
            }
        }
    }

    private void tickBubbleColumn() {
        if ( level().isClientSide ) {

            if ( getBubbleTime() > 0 ) {
                bubbleMultiplier += 0.05F;
            }
            else {
                bubbleMultiplier -= 0.1F;
            }
            bubbleMultiplier = Mth.clamp( bubbleMultiplier, 0.0F, 1.0F );
            bubbleAngleO = bubbleAngle;
            bubbleAngle = 10.0F * (float) Math.sin( 0.5F * (float) level().getGameTime() ) * bubbleMultiplier;
        }
        else {
            if ( !isAboveBubbleColumn ) {
                setBubbleTime( 0 );
            }
            int bubbleTime = getBubbleTime();

            if ( bubbleTime > 0 ) {
                --bubbleTime;
                setBubbleTime( bubbleTime );
                int j = 60 - bubbleTime - 1;

                if ( j > 0 && bubbleTime == 0 ) {
                    setBubbleTime( 0 );
                    Vec3 vec3 = getDeltaMovement();

                    if ( bubbleColumnDirectionIsDown ) {
                        setDeltaMovement( vec3.add( 0.0D, -0.7D, 0.0D ) );
                        ejectPassengers();
                    }
                    else {
                        setDeltaMovement(
                                vec3.x,
                                hasPassenger( (entity) -> entity instanceof Player ) ? 2.7D : 0.6D,
                                vec3.z
                        );
                    }
                }
                isAboveBubbleColumn = false;
            }
        }
    }

    @Nullable
    protected SoundEvent getPaddleSound() {
        return switch ( getStatus() ) {
            case IN_WATER, UNDER_WATER, UNDER_FLOWING_WATER -> SoundEvents.BOAT_PADDLE_WATER;
            case ON_LAND -> SoundEvents.BOAT_PADDLE_LAND;
            default -> null;
        };
    }

    private void tickLerp() {
        if ( getControllingPassenger() != null && level().isClientSide ) {
            lerpSteps = 0;
            syncPacketPositionCodec( getX(), getY(), getZ() );
        }

        if ( lerpSteps > 0 ) {
            double x = getX() + ( lerpX - getX() ) / (double) lerpSteps;
            double y = getY() + ( lerpY - getY() ) / (double) lerpSteps;
            double z = getZ() + ( lerpZ - getZ() ) / (double) lerpSteps;
            double yRot = Mth.wrapDegrees( lerpYRot - (double) getYRot() );

            setYRot( getYRot() + (float) yRot / (float) lerpSteps);
            setXRot( getXRot() + (float)( lerpXRot - (double) getXRot() ) / (float) lerpSteps );

            --lerpSteps;

            setPos( x, y, z );
            setRot( getYRot(), getXRot() );
        }
    }

    public void setPaddleState( boolean paddleLeft, boolean paddleRight ) {
        entityData.set( DATA_ID_PADDLE_LEFT, paddleLeft );
        entityData.set( DATA_ID_PADDLE_RIGHT, paddleRight );
    }

    public float getRowingTime( int paddleIndex, float limbSwing ) {
        return getPaddleState( paddleIndex )
                ? Mth.clampedLerp(paddlePositions[paddleIndex] - ( (float) Math.PI / 8F ), paddlePositions[paddleIndex], limbSwing )
                : 0.0F;
    }

    private MobBoat.Status getStatus() {
        MobBoat.Status status = isUnderwater();

        if ( status != null ) {
            waterLevel = getBoundingBox().maxY;
            return status;
        }
        else if ( checkInWater() ) {
            return MobBoat.Status.IN_WATER;
        }
        else {
            float friction = getGroundFriction();

            if ( friction > 0.0F ) {
                landFriction = friction;
                return MobBoat.Status.ON_LAND;
            }
            else {
                return MobBoat.Status.IN_AIR;
            }
        }
    }

    public float getWaterLevelAbove() {
        AABB boundingBox = getBoundingBox();
        int minX = Mth.floor( boundingBox.minX );
        int maxX = Mth.ceil( boundingBox.maxX );
        int maxY = Mth.floor( boundingBox.maxY );
        int minY = Mth.ceil( boundingBox.maxY - lastYd );
        int minZ = Mth.floor( boundingBox.minZ );
        int maxZ = Mth.ceil( boundingBox.maxZ );
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        label:
        for ( int y = maxY; y < minY; ++y ) {
            float waterLevel = 0.0F;

            for ( int x = minX; x < maxX; ++x ) {
                for ( int z = minZ; z < maxZ; ++z ) {
                    pos.set( x, y, z );
                    FluidState fluidState = level().getFluidState( pos );

                    // TODO - Consider calling fluidState.supportsBoating() with a dummy boat or something maybe?
                    if ( fluidState.is( Fluids.WATER )) {
                        waterLevel = Math.max( waterLevel, fluidState.getHeight( level(), pos ) );
                    }

                    if ( waterLevel >= 1.0F ) {
                        continue label;
                    }
                }
            }

            if ( waterLevel < 1.0F ) {
                return (float) pos.getY() + waterLevel;
            }
        }
        return (float)( minY + 1 );
    }

    public float getGroundFriction() {
        AABB boundingBox = getBoundingBox();
        boundingBox.setMinY( boundingBox.minY - 0.001D );

        int minX = Mth.floor( boundingBox.minX ) - 1;
        int maxX = Mth.ceil( boundingBox.maxX ) + 1;
        int minY = Mth.floor( boundingBox.minY ) - 1;
        int maxY = Mth.ceil( boundingBox.maxY ) + 1;
        int minZ = Mth.floor( boundingBox.minZ ) - 1;
        int maxZ = Mth.ceil( boundingBox.maxZ ) + 1;
        VoxelShape voxelShape = Shapes.create( boundingBox );
        float friction = 0.0F;
        int collisionAmount = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for ( int x = minX; x < maxX; ++x ) {
            for ( int z = minZ; z < maxZ; ++z ) {
                int c = ( x != minX && x != maxX - 1 ? 0 : 1 ) + ( z != minZ && z != maxZ - 1 ? 0 : 1 );

                if ( c != 2 ) {
                    for ( int y = minY; y < maxY; ++y ) {

                        if ( c <= 0 || y != minY && y != maxY - 1 ) {
                            pos.set( x, y, z );
                            BlockState state = level().getBlockState( pos );

                            if ( !( state.getBlock() instanceof WaterlilyBlock ) && Shapes.joinIsNotEmpty( state.getCollisionShape( level(), pos ).move( x, y, z ), voxelShape, BooleanOp.AND ) ) {
                                friction += state.getFriction( level(), pos, this );
                                ++collisionAmount;
                            }
                        }
                    }
                }
            }
        }
        return friction / (float) collisionAmount;
    }

    private boolean checkInWater() {
        AABB boundingBox = getBoundingBox();
        int minX = Mth.floor( boundingBox.minX );
        int maxX = Mth.ceil( boundingBox.maxX );
        int minY = Mth.floor( boundingBox.minY );
        int maxY = Mth.ceil( boundingBox.minY + 0.001D );
        int minZ = Mth.floor( boundingBox.minZ );
        int maxZ = Mth.ceil( boundingBox.maxZ );
        boolean inWater = false;
        waterLevel = -Double.MAX_VALUE;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for ( int x = minX; x < maxX; ++x ) {
            for ( int y = minY; y < maxY; ++y ) {
                for ( int z = minZ; z < maxZ; ++z ) {
                    pos.set( x, y, z );
                    FluidState fluidState = level().getFluidState( pos );

                    if ( fluidState.is( Fluids.WATER ) ) {
                        float fluidY = (float) y + fluidState.getHeight( level(), pos );
                        waterLevel = Math.max( fluidY, waterLevel );
                        inWater |= boundingBox.minY < (double) fluidY;
                    }
                }
            }
        }
        return inWater;
    }

    @Nullable
    private MobBoat.Status isUnderwater() {
        AABB boundingBox = getBoundingBox();
        int minX = Mth.floor( boundingBox.minX );
        int maxX = Mth.ceil( boundingBox.maxX );
        int minY = Mth.floor( boundingBox.maxY );
        double minYOffset = boundingBox.maxY + 0.001D;
        int maxY = Mth.ceil( minYOffset );
        int minZ = Mth.floor( boundingBox.minZ );
        int maxZ = Mth.ceil( boundingBox.maxZ );
        boolean isUnderwater = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for ( int x = minX; x < maxX; ++x ) {
            for ( int y = minY; y < maxY; ++y ) {
                for ( int z = minZ; z < maxZ; ++z ) {
                    pos.set( x, y, z );
                    FluidState fluidState = level().getFluidState( pos );

                    if ( fluidState.is( Fluids.WATER ) && minYOffset < (double)( (float) pos.getY() + fluidState.getHeight( level(), pos ) ) ) {
                        if ( !fluidState.isSource() ) {
                            return MobBoat.Status.UNDER_FLOWING_WATER;
                        }
                        isUnderwater = true;
                    }
                }
            }
        }
        return isUnderwater ? MobBoat.Status.UNDER_WATER : null;
    }

    private void floatBoat() {
        double flowingSpeed = isNoGravity() ? 0.0D : (double) -0.04F;
        double underwaterSpeed = 0.0D;
        invFriction = 0.05F;

        if ( oldStatus == MobBoat.Status.IN_AIR && status != MobBoat.Status.IN_AIR && status != MobBoat.Status.ON_LAND ) {
            waterLevel = getY( 1.0D );
            setPos( getX(), (double)( getWaterLevelAbove() - getBbHeight() ) + 0.101D, getZ() );
            setDeltaMovement( getDeltaMovement().multiply( 1.0D, 0.0D, 1.0D ) );
            lastYd = 0.0D;
            status = MobBoat.Status.IN_WATER;
        }
        else {
            if ( status == MobBoat.Status.IN_WATER ) {
                underwaterSpeed = ( waterLevel - getY() ) / (double) getBbHeight();
                invFriction = 0.9F;
            }
            else if ( status == MobBoat.Status.UNDER_FLOWING_WATER ) {
                flowingSpeed = -7.0E-4D;
                invFriction = 0.9F;
            }
            else if ( status == MobBoat.Status.UNDER_WATER ) {
                underwaterSpeed = 0.01F;
                invFriction = 0.45F;
            }
            else if ( status == MobBoat.Status.IN_AIR ) {
                invFriction = 0.9F;
            }
            else if ( status == MobBoat.Status.ON_LAND ) {
                invFriction = landFriction;

                if ( getControllingPassenger() instanceof Player ) {
                    landFriction /= 2.0F;
                }
            }
            Vec3 deltaMovement = getDeltaMovement();
            setDeltaMovement( deltaMovement.x * (double) invFriction, deltaMovement.y + flowingSpeed, deltaMovement.z * (double) invFriction );
            deltaRotation *= invFriction;

            if ( underwaterSpeed > 0.0D ) {
                Vec3 newDeltaMovement = getDeltaMovement();
                setDeltaMovement( newDeltaMovement.x, ( newDeltaMovement.y + underwaterSpeed * 0.06153846016296973D ) * 0.75D, newDeltaMovement.z );
            }
        }
    }

    public void controlBoat() {
        if ( isVehicle() ) {
            float momentum = 0.0F;

            if ( inputLeft ) {
                --deltaRotation;
            }

            if ( inputRight ) {
                ++deltaRotation;
            }

            if ( inputRight != inputLeft && !inputUp && !inputDown ) {
                momentum += 0.005F;
            }
            setYRot( getYRot() + deltaRotation );

            if ( inputUp ) {
                momentum += 0.04F;
            }

            if ( inputDown ) {
                momentum -= 0.005F;
            }

            setDeltaMovement( getDeltaMovement().add(
                    Mth.sin(-getYRot() * ( (float) Math.PI / 180F ) ) * momentum,
                    0.0D,
                    Mth.cos( getYRot() * ( (float) Math.PI / 180F ) ) * momentum)
            );

            setPaddleState( inputRight && !inputLeft || inputUp, inputLeft && !inputRight || inputUp );
        }
    }

    protected float getSinglePassengerXOffset() {
        return 0.0F;
    }

    public boolean hasEnoughSpaceFor( Entity entity ) {
        return entity.getBbWidth() < getBbWidth();
    }

    @Override
    protected void positionRider( Entity rider, Entity.MoveFunction moveFunction ) {
        if ( hasPassenger( rider ) ) {
            float xOffset = getSinglePassengerXOffset();
            float yOffset = (float)( ( isRemoved() ? (double) 0.01F : getPassengersRidingOffset() ) + rider.getMyRidingOffset() );

            if ( getPassengers().size() > 1 ) {
                int i = getPassengers().indexOf( rider );

                if ( i == 0 ) {
                    xOffset = 0.2F;
                }
                else {
                    xOffset = -0.6F;
                }

                if ( rider instanceof Animal ) {
                    xOffset += 0.2F;
                }
            }
            Vec3 vec3 = ( new Vec3( xOffset, 0.0D, 0.0D ) ).yRot( -getYRot() * ( (float) Math.PI / 180F) - ( (float) Math.PI / 2F ) );
            moveFunction.accept( rider, getX() + vec3.x, getY() + (double)yOffset, getZ() + vec3.z );

            rider.setYRot( rider.getYRot() + deltaRotation );
            rider.setYHeadRot( rider.getYHeadRot() + deltaRotation );
            clampRotation( rider );

            if ( rider instanceof Animal animal && getPassengers().size() == getMaxPassengers() ) {
                int sorcery = rider.getId() % 2 == 0 ? 90 : 270;
                rider.setYBodyRot( animal.yBodyRot + (float) sorcery );
                rider.setYHeadRot( rider.getYHeadRot() + (float) sorcery );
            }
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger( LivingEntity passenger ) {
        Vec3 vec3 = getCollisionHorizontalEscapeVector( getBbWidth() * Mth.SQRT_OF_TWO, passenger.getBbWidth(), passenger.getYRot() );
        double x = getX() + vec3.x;
        double z = getZ() + vec3.z;
        BlockPos pos = BlockPos.containing( x, getBoundingBox().maxY, z );
        BlockPos belowPos = pos.below();

        if ( !level().isWaterAt( belowPos ) ) {
            List<Vec3> dismountLocations = Lists.newArrayList();
            double floorHeight = level().getBlockFloorHeight( pos );

            if ( DismountHelper.isBlockFloorValid( floorHeight ) ) {
                dismountLocations.add( new Vec3( x, (double) pos.getY() + floorHeight, z ) );
            }
            double belowFloorHeight = level().getBlockFloorHeight( belowPos );

            if ( DismountHelper.isBlockFloorValid( belowFloorHeight ) ) {
                dismountLocations.add( new Vec3( x, (double) belowPos.getY() + belowFloorHeight, z ) );
            }

            for ( Pose pose : passenger.getDismountPoses() ) {
                for ( Vec3 location : dismountLocations ) {
                    if ( DismountHelper.canDismountTo( level(), location, passenger, pose ) ) {
                        passenger.setPose( pose );
                        return location;
                    }
                }
            }
        }
        return super.getDismountLocationForPassenger( passenger );
    }

    protected void clampRotation( Entity entity ) {
        entity.setYBodyRot( getYRot() );
        float f = Mth.wrapDegrees( entity.getYRot() - getYRot() );
        float f1 = Mth.clamp( f, -105.0F, 105.0F );

        entity.yRotO += f1 - f;
        entity.setYRot( entity.getYRot() + f1 - f );
        entity.setYHeadRot( entity.getYRot() );
    }

    @Override
    public void onPassengerTurned( Entity entity ) {
        clampRotation( entity );
    }

    @Override
    protected void addAdditionalSaveData( CompoundTag compoundTag ) {
        compoundTag.putString( "Type", getVariant().getSerializedName() );
    }

    @Override
    protected void readAdditionalSaveData( CompoundTag compoundTag ) {
        if (compoundTag.contains( "Type", CompoundTag.TAG_STRING ) ) {
            setVariant( Boat.Type.byName( compoundTag.getString( "Type" ) ) );
        }
    }

    @Override
    public InteractionResult interact( Player player, InteractionHand hand ) {
        if ( player.isSecondaryUseActive() ) {
            return InteractionResult.PASS;
        }
        else if ( outOfControlTicks < 60.0F ) {
            if ( !level().isClientSide ) {
                return player.startRiding( this ) ? InteractionResult.CONSUME : InteractionResult.PASS;
            }
            else {
                return InteractionResult.SUCCESS;
            }
        }
        else {
            return InteractionResult.PASS;
        }
    }

    @Override
    protected void checkFallDamage( double yVelocity, boolean onGround, BlockState state, BlockPos pos ) {
        lastYd = getDeltaMovement().y;

        if ( !isPassenger() ) {
            if ( onGround ) {
                if ( fallDistance > 3.0F ) {
                    if ( status != MobBoat.Status.ON_LAND ) {
                        resetFallDistance();
                        return;
                    }
                    causeFallDamage( fallDistance, 1.0F, damageSources().fall() );

                    if ( !level().isClientSide && !isRemoved() ) {
                        kill();

                        if ( level().getGameRules().getBoolean( GameRules.RULE_DOENTITYDROPS ) ) {
                            for ( int i = 0; i < 3; ++i ) {
                                spawnAtLocation( getVariant().getPlanks() );
                            }

                            for ( int j = 0; j < 2; ++j ) {
                                spawnAtLocation( Items.STICK );
                            }
                        }
                    }
                }
                resetFallDistance();
            }
            else if ( !level().getFluidState( blockPosition().below() ).is( Fluids.WATER )  && yVelocity < 0.0D ) {
                fallDistance -= (float) yVelocity;
            }
        }
    }

    public boolean getPaddleState( int side ) {
        return entityData.get( side == 0 ? DATA_ID_PADDLE_LEFT : DATA_ID_PADDLE_RIGHT ) && getControllingPassenger() != null;
    }

    public void setDamage( float damage ) {
        entityData.set( DATA_ID_DAMAGE, damage );
    }

    public float getDamage() {
        return entityData.get( DATA_ID_DAMAGE );
    }

    public void setHurtTime( int hurtTime ) {
        entityData.set( DATA_ID_HURT, hurtTime );
    }

    public int getHurtTime() {
        return entityData.get( DATA_ID_HURT );
    }

    private void setBubbleTime( int bubbleTime ) {
        entityData.set( DATA_ID_BUBBLE_TIME, bubbleTime );
    }

    private int getBubbleTime() {
        return entityData.get( DATA_ID_BUBBLE_TIME );
    }

    public float getBubbleAngle( float f ) {
        return Mth.lerp( f, bubbleAngleO, bubbleAngle );
    }

    public void setHurtDir( int hurtDir ) {
        entityData.set( DATA_ID_HURTDIR, hurtDir );
    }

    public int getHurtDir() {
        return entityData.get( DATA_ID_HURTDIR );
    }

    public void setVariant( Boat.Type type ) {
        entityData.set( DATA_ID_TYPE, type.ordinal() );
    }

    public Boat.Type getVariant() {
        return Boat.Type.byId( entityData.get( DATA_ID_TYPE ) );
    }

    @Override
    protected boolean canAddPassenger( Entity entity ) {
        return getPassengers().size() < getMaxPassengers() && getEyeInFluidType() != ForgeMod.WATER_TYPE.get();
    }

    protected int getMaxPassengers() {
        return 2;
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity entity = getFirstPassenger();
        LivingEntity captain;

        if (entity instanceof LivingEntity livingEntity) {
            captain = livingEntity;
        }
        else {
            captain = null;
        }
        return captain;
    }

    public void setInput( boolean left, boolean right, boolean forward, boolean backward ) {
        inputLeft = left;
        inputRight = right;
        inputUp = forward;
        inputDown = backward;
    }

    @Override
    public boolean isUnderWater() {
        return status == MobBoat.Status.UNDER_WATER || status == MobBoat.Status.UNDER_FLOWING_WATER;
    }

    @Override
    protected void addPassenger( Entity entity ) {
        super.addPassenger( entity );

        if (isControlledByLocalInstance() && lerpSteps > 0) {
            lerpSteps = 0;
            absMoveTo( lerpX, lerpY, lerpZ, (float) lerpYRot, (float) lerpXRot );
        }
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(getDropItem());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket( this ); }

    @Override
    public void writeSpawnData( FriendlyByteBuf buffer ) {

    }

    @Override
    public void readSpawnData( FriendlyByteBuf additionalData ) {

    }

    public enum Status {
        IN_WATER,
        UNDER_WATER,
        UNDER_FLOWING_WATER,
        ON_LAND,
        IN_AIR;
    }
}
