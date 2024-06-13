package fathertoast.specialmobs.common.entity.witch;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.entity.ai.FluidPathNavigator;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityTeleportEvent;

@SpecialMob
public class WindWitchEntity extends _SpecialWitchEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<WindWitchEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0x6388B2 ).theme( BestiaryInfo.Theme.MOUNTAIN )
                .uniqueTextureBaseOnly()
                .addExperience( 2 ).fallImmune()
                .multiplyAttribute( Attributes.MOVEMENT_SPEED, 1.2 );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Wind Witch",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addCommonDrop( "common", Items.FEATHER );
        loot.addSemicommonDrop( "semicommon", Items.ENDER_PEARL );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<WindWitchEntity> getVariantFactory() { return WindWitchEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends WindWitchEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** Ticks before this witch can teleport. */
    private int teleportDelay;
    
    public WindWitchEntity( EntityType<? extends _SpecialWitchEntity> entityType, Level level ) {
        super( entityType, level );
        setPathfindingMalus( BlockPathTypes.WATER, BlockPathTypes.WALKABLE.getMalus() );
    }
    
    /** Override to change this entity's AI goals. */
    protected void registerVariantGoals() {
        AIHelper.replaceWaterAvoidingRandomWalking( this, 1.0 );
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
        MobHelper.floatInFluid( this, 0.06, ForgeMod.WATER_TYPE.get() );
        MobHelper.hopOntoShore( this, FluidTags.WATER );
    }
    
    /** Called each tick to update this entity's movement. */
    @Override
    public void aiStep() {
        if( !level().isClientSide() && isAlive() && teleportDelay-- <= 0 && getTarget() != null && random.nextInt( 20 ) == 0 ) {
            if( getTarget().distanceToSqr( this ) > 64.0 ) {
                for( int i = 0; i < 16; i++ ) {
                    if( teleportTowards( getTarget() ) ) {
                        teleportDelay = 60;
                        removeEffect( MobEffects.INVISIBILITY );
                        break;
                    }
                }
            }
            else {
                MobHelper.applyDurationEffect( this, MobEffects.INVISIBILITY, 30 );
                for( int i = 0; i < 16; i++ ) {
                    if( teleport() ) {
                        teleportDelay = 30;
                        break;
                    }
                }
            }
        }
        super.aiStep();
    }
    
    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt( DamageSource source, float amount ) {
        if( isInvulnerableTo( source ) || fireImmune() && source.is( DamageTypeTags.IS_FIRE ) ) return false;
        
        if( source.is(DamageTypeTags.IS_PROJECTILE) ) {
            for( int i = 0; i < 64; i++ ) {
                if( teleport() ) return true;
            }
            return false;
        }
        
        final boolean success = super.hurt( source, amount );
        if( !level().isClientSide() && getHealth() > 0.0F ) {
            if( source.getEntity() instanceof LivingEntity) {
                teleportDelay -= 15;
                if( teleportDelay <= 0 && random.nextFloat() < 0.5F ) {
                    for( int i = 0; i < 16; i++ ) {
                        if( teleport() ) break;
                    }
                }
                else {
                    removeEffect( MobEffects.INVISIBILITY );
                }
            }
            else if( random.nextInt( 10 ) != 0 ) {
                teleport();
            }
        }
        return success;
    }
    
    /** @return Teleports this "enderman" to a random nearby position; returns true if successful. */
    protected boolean teleport() {
        if( level().isClientSide() || !isAlive() ) return false;
        
        final double x = getX() + (random.nextDouble() - 0.5) * 20.0;
        final double y = getY() + (double) (random.nextInt( 12 ) - 4);
        final double z = getZ() + (random.nextDouble() - 0.5) * 20.0;
        return teleport( x, y, z );
    }
    
    /** @return Teleports this "enderman" towards another entity; returns true if successful. */
    protected boolean teleportTowards( Entity target ) {
        final Vec3 directionFromTarget = new Vec3(
                getX() - target.getX(),
                getY( 0.5 ) - target.getEyeY(),
                getZ() - target.getZ() )
                .normalize();
        
        final double x = getX() + (random.nextDouble() - 0.5) * 8.0 - directionFromTarget.x * 10.0;
        final double y = getY() + (double) (random.nextInt( 8 ) - 2) - directionFromTarget.y * 10.0;
        final double z = getZ() + (random.nextDouble() - 0.5) * 8.0 - directionFromTarget.z * 10.0;
        return teleport( x, y, z );
    }
    
    /** @return Teleports this "enderman" to a new position; returns true if successful. */
    protected boolean teleport( double x, double y, double z ) {
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos( x, y, z );
        
        while( pos.getY() > 0 ) {
            // Allow wind witch to teleport on top of water
            final BlockState block = level().getBlockState( pos );
            if( block.blocksMotion() || block.getFluidState().is( FluidTags.WATER ) ) {
                
                final EntityTeleportEvent.EnderEntity event = ForgeEventFactory.onEnderTeleport( this, x, y + 1, z );
                if( event.isCanceled() ) return false;
                
                final boolean success = uncheckedTeleport( event.getTargetX(), event.getTargetY(), event.getTargetZ(), false );
                if( success && !isSilent() ) {
                    level().playSound( null, xo, yo, zo, SoundEvents.GHAST_SHOOT, getSoundSource(),
                            1.0F, 1.0F );
                    playSound( SoundEvents.GHAST_SHOOT, 1.0F, 1.0F );
                }
                return success;
            }
            else {
                pos.move( Direction.DOWN );
                y--;
            }
        }
        return false;
    }
    
    /** This is #randomTeleport, but uses a pre-determined y-coord. */
    @SuppressWarnings( "SameParameterValue" ) // Don't care; maintain vanilla's method signature
    private boolean uncheckedTeleport( double x, double y, double z, boolean spawnParticles ) {
        final double xI = getX();
        final double yI = getY();
        final double zI = getZ();
        
        //noinspection deprecation
        if( level().hasChunkAt( BlockPos.containing( x, y, z ) ) ) {
            teleportTo( x, y, z );
            
            if( level().noCollision( this ) && !level().containsAnyLiquid( getBoundingBox() ) ) {
                if( spawnParticles ) References.EntityEvent.TELEPORT_TRAIL_PARTICLES.broadcast( this );
                getNavigation().stop();
                return true;
            }
        }
        teleportTo( xI, yI, zI );
        return false;
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundTag saveTag ) {
        setPathfindingMalus( BlockPathTypes.WATER, BlockPathTypes.WALKABLE.getMalus() );
    }
}