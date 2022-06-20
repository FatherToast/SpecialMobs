package fathertoast.specialmobs.common.util;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.play.server.SExplosionPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.EntityExplosionContext;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

/**
 * This class is used to make handling and modifying vanilla explosions much easier by wrapping them
 * and providing helper methods.
 */
@SuppressWarnings( "UnusedReturnValue" )
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ExplosionHelper {
    
    /** Gets the explosion mode to use as a result of appropriate Forge events. */
    public static Explosion.Mode getMode( Entity entity ) {
        return ForgeEventFactory.getMobGriefingEvent( entity.level, entity ) ? Explosion.Mode.DESTROY : Explosion.Mode.NONE;
    }
    
    /** Creates and fully executes an explosion for a source entity at that source's current position. */
    public static Explosion explode( Entity entity, float power, boolean damageBlocks, boolean fiery ) {
        return explode( entity, entity.getX(), entity.getY(), entity.getZ(), power, damageBlocks, fiery );
    }
    
    /** Creates and fully executes an explosion for a source entity at a specified position. */
    public static Explosion explode( Entity entity, double x, double y, double z, float power, boolean damageBlocks, boolean fiery ) {
        return entity.level.explode( entity, x, y, z, power, fiery, damageBlocks ? getMode( entity ) : Explosion.Mode.NONE );
    }
    
    public final Entity source;
    public final World level;
    public final float radius;
    
    public final Explosion.Mode mode;
    public final EntityExplosionContext damageCalculator;
    public final Explosion explosion;
    
    /**
     * Creates an explosion for a source entity at that source's current position. Instantiating this class allows a
     * little more control over the explosion (vs. the static explode methods), such as modifying affected blocks.
     * <p>
     * To fully execute the explosion, you must manually call initializeExplosion() and then finalizeExplosion().
     *
     * @see #initializeExplosion()
     * @see #finalizeExplosion()
     */
    public ExplosionHelper( Entity entity, float power, boolean damageBlocks, boolean fiery ) {
        this( entity, entity.getX(), entity.getY(), entity.getZ(), power, damageBlocks, fiery );
    }
    
    /**
     * Creates an explosion for a source entity at a specified position. Instantiating this class allows a little more
     * control over the explosion (vs. the static explode methods), such as modifying affected blocks.
     * <p>
     * To fully execute the explosion, you must manually call initializeExplosion() and then finalizeExplosion().
     *
     * @see #initializeExplosion()
     * @see #finalizeExplosion()
     */
    public ExplosionHelper( Entity entity, double x, double y, double z, float power, boolean damageBlocks, boolean fiery ) {
        source = entity;
        level = entity.level;
        radius = power;
        
        mode = damageBlocks ? getMode( entity ) : Explosion.Mode.NONE;
        damageCalculator = new EntityExplosionContext( entity );
        explosion = new Explosion( level, entity, null, damageCalculator, x, y, z, power, fiery, mode );
    }
    
    /** @return This explosion's position vector. */
    public Vector3d getPos() { return explosion.getPosition(); }
    
    /**
     * @return A list of all block positions hit by this explosion. This is populated after calling initializeExplosion()
     * and can be modified before calling finalizeExplosion().
     */
    public List<BlockPos> getHitBlocks() { return explosion.getToBlow(); }
    
    /**
     * Does a quick check if the block can explode, firing all relevant Forge events.
     * The power value used here ignores distance and shielding.
     */
    public boolean tryExplodeBlock( BlockPos pos, BlockState block, float power ) {
        return tryExplodeBlock( pos, block, level.getFluidState( pos ), power );
    }
    
    /**
     * Does a quick check if the block can explode, firing all relevant Forge events.
     * The power value used here ignores distance and shielding.
     */
    public boolean tryExplodeBlock( BlockPos pos, BlockState block, FluidState fluid, float power ) {
        float blockDamage = power * (0.7F + level.random.nextFloat() * 0.6F);
        
        final Optional<Float> optional = getBlockExplosionResistance( pos, block, fluid );
        if( optional.isPresent() ) blockDamage -= (optional.get() + 0.3F) * 0.3F;
        
        return blockDamage > 0.0F && shouldBlockExplode( pos, block, blockDamage );
    }
    
    /** @return The block's explosion resistance (helper method). */
    public Optional<Float> getBlockExplosionResistance( BlockPos pos, BlockState block ) {
        return damageCalculator.getBlockExplosionResistance( explosion, level, pos, block, level.getFluidState( pos ) );
    }
    
    /** @return The block's explosion resistance (helper method). */
    public Optional<Float> getBlockExplosionResistance( BlockPos pos, BlockState block, FluidState fluid ) {
        return damageCalculator.getBlockExplosionResistance( explosion, level, pos, block, fluid );
    }
    
    /** @return True if the block should be destroyed from the given damage (helper method). */
    public boolean shouldBlockExplode( BlockPos pos, BlockState block, float blockDamage ) {
        return damageCalculator.shouldBlockExplode( explosion, level, pos, block, blockDamage );
    }
    
    /**
     * Runs the first part of explosion logic. This step calculates all blocks/entities to hit with the explosion
     * and deals damage/knockback to all hit entities.
     *
     * @return True if the explosion should continue to finalization, or false if its start event was canceled.
     */
    public boolean initializeExplosion() {
        if( net.minecraftforge.event.ForgeEventFactory.onExplosionStart( level, explosion ) ) return false;
        explosion.explode();
        return true;
    }
    
    /**
     * Runs the second and last part of explosion logic. This step breaks all hit blocks and places fire blocks
     * (if these capabilities are enabled) and notifies players that the explosion happened.
     */
    public void finalizeExplosion() {
        if( level instanceof ServerWorld ) {
            final ServerWorld serverLevel = (ServerWorld) level;
            
            // Handle server-side explosion logic
            explosion.finalizeExplosion( false );
            if( mode == Explosion.Mode.NONE ) {
                explosion.clearToBlow();
            }
            final Vector3d pos = getPos();
            for( ServerPlayerEntity player : serverLevel.players() ) {
                if( player.distanceToSqr( pos.x, pos.y, pos.z ) < 4096.0 ) {
                    player.connection.send( new SExplosionPacket( pos.x, pos.y, pos.z, radius,
                            explosion.getToBlow(), explosion.getHitPlayers().get( player ) ) );
                }
            }
        }
        else {
            // Handle client-side explosion logic
            explosion.finalizeExplosion( true );
        }
    }
}