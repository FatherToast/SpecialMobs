package fathertoast.specialmobs.common.entity.enderman;

import fathertoast.specialmobs.common.bestiary.BestiaryInfo;
import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.entity.ai.AIHelper;
import fathertoast.specialmobs.common.entity.ai.FluidPathNavigator;
import fathertoast.specialmobs.common.util.References;
import fathertoast.specialmobs.datagen.loot.LootTableBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.FrostWalkerEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.EntityTeleportEvent;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SpecialMob
public class IcyEndermanEntity extends _SpecialEndermanEntity {
    
    //--------------- Static Special Mob Hooks ----------------
    
    @SpecialMob.BestiaryInfoSupplier
    public static BestiaryInfo bestiaryInfo( EntityType.Builder<LivingEntity> entityType ) {
        return new BestiaryInfo( 0x72959C );
        //TODO theme - ice
    }
    
    @SpecialMob.AttributeCreator
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return _SpecialEndermanEntity.createAttributes();
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
        loot.addUncommonDrop( "uncommon", Blocks.ICE );
    }
    
    @SpecialMob.Constructor
    public IcyEndermanEntity( EntityType<? extends _SpecialEndermanEntity> entityType, World world ) {
        super( entityType, world );
        getSpecialData().addPotionImmunity( Effects.MOVEMENT_SLOWDOWN );
        xpReward += 1;
        
        setPathfindingMalus( PathNodeType.WATER, PathNodeType.WALKABLE.getMalus() );
    }
    
    
    //--------------- Variant-Specific Implementations ----------------
    
    /** Override to apply effects when this entity hits a target with a melee attack. */
    @Override
    protected void onVariantAttack( Entity target ) {
        if( target instanceof LivingEntity ) {
            final LivingEntity livingTarget = (LivingEntity) target;
            final int duration = MobHelper.getShortDebuffDuration( level.getDifficulty() );
            
            livingTarget.addEffect( new EffectInstance( Effects.MOVEMENT_SLOWDOWN, duration, 4 ) );
            livingTarget.addEffect( new EffectInstance( Effects.DIG_SLOWDOWN, duration, 2 ) );
        }
    }
    
    /** Override to change this entity's AI goals. */
    protected void registerVariantGoals() {
        AIHelper.replaceWaterAvoidingRandomWalking( this, 1.0 );
    }
    
    /** @return A new path navigator for this entity to use. */
    @Override
    protected PathNavigator createNavigation( World world ) {
        return new FluidPathNavigator( this, world, true, false );
    }
    
    /** @return Whether this entity can stand on a particular type of fluid. */
    @Override
    public boolean canStandOnFluid( Fluid fluid ) { return fluid.is( FluidTags.WATER ); }
    
    /** @return Attempts to damage this entity; returns true if the hit was successful. */
    @Override
    public boolean hurt( DamageSource source, float amount ) {
        if( DamageSource.DROWN.equals( source ) ) {
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
        final BlockPos.Mutable pos = new BlockPos.Mutable( x, y, z );
        
        while( pos.getY() > 0 ) {
            // Allow icy endermen to teleport on top of water
            final BlockState block = level.getBlockState( pos );
            if( block.getMaterial().blocksMotion() || block.getFluidState().is( FluidTags.WATER ) ) {
                
                final EntityTeleportEvent.EnderEntity event = ForgeEventFactory.onEnderTeleport( this, x, y + 1, z );
                if( event.isCanceled() ) return false;
                
                final boolean success = uncheckedTeleport( event.getTargetX(), event.getTargetY(), event.getTargetZ(), true );
                if( success ) {
                    updateFrostWalker( pos.immutable().above() );
                    if( !isSilent() ) {
                        level.playSound( null, xo, yo, zo, SoundEvents.ENDERMAN_TELEPORT, getSoundSource(),
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
        if( level.hasChunkAt( new BlockPos( x, y, z ) ) ) {
            teleportTo( x, y, z );
            
            if( level.noCollision( this ) && !level.containsAnyLiquid( getBoundingBox() ) ) {
                if( spawnParticles ) level.broadcastEntityEvent( this, References.EVENT_TELEPORT_TRAIL_PARTICLES );
                getNavigation().stop();
                return true;
            }
        }
        teleportTo( xI, yI, zI );
        return false;
    }
    
    /** Override to load data from this entity's NBT data. */
    @Override
    public void readVariantSaveData( CompoundNBT saveTag ) {
        setPathfindingMalus( PathNodeType.WATER, PathNodeType.WALKABLE.getMalus() );
    }
    
    /** Called to make the frost walker ice platform around this entity, as needed. */
    private void updateFrostWalker( BlockPos pos ) {
        final boolean actualOnGround = onGround;
        onGround = true; // Spoof the frost walker enchant requirement to be on the ground
        FrostWalkerEnchantment.onEntityMoved( this, level, pos, 1 );
        onGround = actualOnGround;
    }
    
    private static final ResourceLocation[] TEXTURES = {
            GET_TEXTURE_PATH( "icy" ),
            GET_TEXTURE_PATH( "icy_eyes" )
    };
    
    /** @return All default textures for this entity. */
    @Override
    public ResourceLocation[] getDefaultTextures() { return TEXTURES; }
}