package toast.specialMobs.entity.blaze;

import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.MobHelper;
import toast.specialMobs._SpecialMobs;

public class EntityJoltBlaze extends Entity_SpecialBlaze {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "blaze/jolt.png" )
    };
    
    public EntityJoltBlaze( World world ) {
        super( world );
        getSpecialData().setTextures( EntityJoltBlaze.TEXTURES );
        experienceValue += 2;
    }
    
    /// Called every tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        if( !worldObj.isRemote && isEntityAlive() && entityToAttack != null && rand.nextInt( 10 ) == 0 && entityToAttack.getDistanceSqToEntity( this ) > 256.0 ) {
            teleportToEntity( entityToAttack );
            attackTime = Math.max( 20, attackTime );
        }
        super.onLivingUpdate();
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        if( hit ) {
            for( int i = rand.nextInt( 3 + looting ); i-- > 0; ) {
                dropItem( Items.redstone, 1 );
            }
        }
    }
    
    /// Damages this entity from the damageSource by the given amount. Returns true if this entity is damaged.
    @Override
    public boolean attackEntityFrom( DamageSource damageSource, float damage ) {
        if( !worldObj.isRemote && !DamageSource.drown.damageType.equals( damageSource.damageType ) ) {
            double xI = posX;
            double yI = posY;
            double zI = posZ;
            
            for( int i = 0; i < 64; i++ ) {
                if( teleportRandomly() ) {
                    if( damageSource instanceof EntityDamageSourceIndirect )
                        return true;
                    boolean hit = super.attackEntityFrom( damageSource, damage );
                    
                    if( getHealth() > 0.0F ) {
                        MobHelper.lightningExplode( this, xI, yI, zI, 0 );
                    }
                    else {
                        setPosition( xI, yI, zI );
                    }
                    return hit;
                }
            }
        }
        return super.attackEntityFrom( damageSource, damage );
    }
    
    /// Teleports this enderman to a random nearby location. Returns true if this entity teleports.
    protected boolean teleportRandomly() {
        double x = posX + (rand.nextDouble() - 0.5) * 16.0;
        double y = posY + (rand.nextInt( 12 ) - 4);
        double z = posZ + (rand.nextDouble() - 0.5) * 16.0;
        return teleportTo( x, y, z, false );
    }
    
    /// Teleports this enderman to the given entity. Returns true if this entity teleports.
    @SuppressWarnings( "UnusedReturnValue" )
    protected boolean teleportToEntity( Entity entity ) {
        double x = entity.posX + (rand.nextDouble() - 0.5) * 8.0;
        double y = entity.posY + rand.nextInt( 8 ) - 2;
        double z = entity.posZ + (rand.nextDouble() - 0.5) * 8.0;
        return teleportTo( x, y, z, true );
    }
    
    /// Teleports this enderman to the given coordinates. Returns true if this entity teleports.
    protected boolean teleportTo( double x, double y, double z, boolean strikeDestination ) {
        double xI = posX;
        double yI = posY;
        double zI = posZ;
        setPosition( x, y, z );
        if( !worldObj.getCollidingBoundingBoxes( this, boundingBox ).isEmpty() || worldObj.isAnyLiquid( boundingBox ) ) {
            setPosition( xI, yI, zI );
            return false;
        }
        for( int i = 0; i < 128; i++ ) {
            double posRelative = i / 127.0;
            float vX = (rand.nextFloat() - 0.5F) * 0.2F;
            float vY = (rand.nextFloat() - 0.5F) * 0.2F;
            float vZ = (rand.nextFloat() - 0.5F) * 0.2F;
            double dX = xI + (posX - xI) * posRelative + (rand.nextDouble() - 0.5) * width * 2.0;
            double dY = yI + (posY - yI) * posRelative + rand.nextDouble() * height;
            double dZ = zI + (posZ - zI) * posRelative + (rand.nextDouble() - 0.5) * width * 2.0;
            worldObj.spawnParticle( "smoke", dX, dY, dZ, vX, vY, vZ );
        }
        if( strikeDestination ) {
            MobHelper.lightningExplode( this, 0 );
        }
        return true;
    }
}