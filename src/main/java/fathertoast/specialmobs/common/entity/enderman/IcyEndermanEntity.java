package fathertoast.specialmobs.common.entity.enderman;

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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityTeleportEvent;

@SpecialMob
public class IcyEndermanEntity extends _SpecialEndermanEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.SpeciesReference
    public static MobFamily.Species<IcyEndermanEntity> SPECIES;
    
    @SpecialMob.BestiaryInfoSupplier
    public static void getBestiaryInfo( BestiaryInfo.Builder bestiaryInfo ) {
        bestiaryInfo.color( 0xDDEAEA ).theme( BestiaryInfo.Theme.ICE )
                .uniqueTextureWithEyes()
                .addExperience( 1 ).effectImmune( MobEffects.MOVEMENT_SLOWDOWN );
        
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Icy Enderman",
                "", "", "", "", "", "" );//TODO
    }
    
    @SpecialMob.LootTableProvider
    public static void buildLootTable( LootTableBuilder loot ) {
        addBaseLoot( loot );
        loot.addClusterDrop( "common", Items.SNOWBALL );
        loot.addUncommonDrop( "uncommon", Blocks.BLUE_ICE );
    }
    
    @SpecialMob.Factory
    public static EntityType.EntityFactory<IcyEndermanEntity> getVariantFactory() { return IcyEndermanEntity::new; }
    
    /** @return This entity's mob species. */
    @SpecialMob.SpeciesSupplier
    @Override
    public MobFamily.Species<? extends IcyEndermanEntity> getSpecies() { return SPECIES; }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    public IcyEndermanEntity( EntityType<? extends _SpecialEndermanEntity> entityType, Level level ) {
        super( entityType, level );
        setPathfindingMalus( BlockPathTypes.WATER, BlockPathTypes.WALKABLE.getMalus() );
    }
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( LivingEntity target ) {
        MobHelper.applyEffect( target, MobEffects.MOVEMENT_SLOWDOWN, 5, 0.5F );
        MobHelper.applyEffect( target, MobEffects.DIG_SLOWDOWN, 3 );
    }
    
    /** Override to change this entity's AI goals. */
    @Override
    protected void registerVariantGoals() {
        AIHelper.replaceWaterAvoidingRandomWalking( this, 1.0 );
    }
    
    /** @return A new path navigator for this entity to use. */
    @Override
    protected PathNavigation createNavigation( Level level ) {
        return new FluidPathNavigator( this, level, true, false );
    }
    
    /** @return Whether this entity can stand on a particular type of fluid. */
    @Override
    public boolean canStandOnFluid( FluidState fluid ) { return fluid.is( FluidTags.WATER ); }
    
    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt( DamageSource source, float amount ) {
        if( source.is( DamageTypeTags.IS_DROWNING ) ) {
            for( int i = 0; i < 64; i++ ) {
                if( teleport() ) return true;
            }
            return false;
        }
        return super.hurt( source, amount );
    }
    
    /** Called whenever this entity's block position changes. */
    @Override
    protected void onChangedBlock( BlockPos pos ) {
        super.onChangedBlock( pos );
        updateFrostWalker( pos );
    }
    
    /** @return Teleports this enderman to a new position; returns true if successful. */
    @Override
    protected boolean teleport( double x, double y, double z ) {
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos( x, y, z );
        
        while( pos.getY() > 0 ) {
            // Allow icy endermen to teleport on top of water
            final BlockState block = level().getBlockState( pos );
            if( block.blocksMotion() || block.getFluidState().is( FluidTags.WATER ) ) {
                
                final EntityTeleportEvent.EnderEntity event = ForgeEventFactory.onEnderTeleport( this, x, y + 1, z );
                if( event.isCanceled() ) return false;
                
                final boolean success = uncheckedTeleport( event.getTargetX(), event.getTargetY(), event.getTargetZ(), true );
                if( success ) {
                    updateFrostWalker( pos.immutable().above() );
                    if( !isSilent() ) {
                        level().playSound( null, xo, yo, zo, SoundEvents.ENDERMAN_TELEPORT, getSoundSource(),
                                1.0F, 1.0F );
                        playSound( SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F );
                    }
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
    
    /** Called to make the frost walker ice platform around this entity, as needed. */
    private void updateFrostWalker( BlockPos pos ) {
        final boolean actualOnGround = onGround();
        setOnGround( true ); // Spoof the frost walker enchant requirement to be on the ground
        FrostWalkerEnchantment.onEntityMoved( this, level(), pos, 1 );
        setOnGround( actualOnGround );
    }
}