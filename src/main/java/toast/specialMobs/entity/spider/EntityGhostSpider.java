package toast.specialMobs.entity.spider;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.Properties;
import toast.specialMobs._SpecialMobs;

import java.util.List;

public class EntityGhostSpider extends Entity_SpecialSpider {
    
    /// Useful properties for this class.
    private static final boolean XRAY_GHOSTS = Properties.getBoolean( Properties.STATS, "xray_ghosts" );
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "spider/ghost.png" ),
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "spider/ghost_eyes.png" )
    };
    
    public EntityGhostSpider( World world ) {
        super( world );
        noClip = true;
        getSpecialData().setTextures( EntityGhostSpider.TEXTURES );
        getSpecialData().canBreatheInWater = true;
        getSpecialData().isImmuneToFalling = true;
        getSpecialData().ignorePressurePlates = true;
        getSpecialData().ignoreWaterPush = true;
    }
    
    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        isHostile = true;
        getSpecialData().multAttribute( SharedMonsterAttributes.movementSpeed, 1.2 );
    }
    
    /// Called each tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        if( worldObj.isDaytime() && !worldObj.isRemote ) {
            float brightness = getBrightness( 1.0F );
            if( brightness > 0.5F && rand.nextFloat() * 30.0F < (brightness - 0.4F) * 2.0F && worldObj.canBlockSeeTheSky( (int) Math.floor( posX ), (int) Math.floor( posY ), (int) Math.floor( posZ ) ) ) {
                ItemStack helmet = getEquipmentInSlot( 4 );
                if( helmet != null ) {
                    if( helmet.isItemStackDamageable() ) {
                        helmet.setItemDamage( helmet.getItemDamageForDisplay() + rand.nextInt( 2 ) );
                        if( helmet.getItemDamageForDisplay() >= helmet.getMaxDamage() ) {
                            renderBrokenItemStack( helmet );
                            setCurrentItemOrArmor( 4, (ItemStack) null );
                        }
                    }
                }
                else {
                    setFire( 8 );
                }
            }
        }
        super.onLivingUpdate();
        if( onGround && super.isEntityInsideOpaqueBlock() ) {
            jump();
        }
    }
    
    /// Finds the closest player within 16 blocks to attack, or null if this Entity isn't interested in attacking.
    @Override
    protected Entity findPlayerToAttack() {
        Entity player = super.findPlayerToAttack();
        return player != null && (EntityGhostSpider.XRAY_GHOSTS || canEntityBeSeen( player )) ? player : null;
    }
    
    /// Tries to move the entity by the passed in displacement.
    @Override
    public void moveEntity( double x, double y, double z ) {
        double yI = y;
        boolean shouldFall = false;
        if( entityToAttack != null && entityToAttack.boundingBox.maxY <= boundingBox.minY ) {
            float dX = (float) (entityToAttack.posX - posX);
            float dZ = (float) (entityToAttack.posZ - posZ);
            float range = (entityToAttack.width + width) / 2.0F;
            if( dX * dX + dZ * dZ < range * range ) {
                shouldFall = true;
            }
        }
        if( y < 0.0 && !shouldFall ) {
            List list = worldObj.getCollidingBoundingBoxes( this, boundingBox.addCoord( x, y, z ) );
            
            for( Object o : list ) {
                y = ((AxisAlignedBB) o).calculateYOffset( boundingBox, y );
            }
        }
        super.moveEntity( x, y, z );
        isCollidedHorizontally = false;
        isCollidedVertically = yI != y;
        onGround = isCollidedVertically && yI < 0.0;
        isCollided = isCollidedVertically;
        updateFallState( y, onGround );
        if( isCollidedVertically ) {
            motionY = 0.0;
        }
    }
    
    /// Get this entity's creature type.
    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEAD;
    }
    
    /// Checks if this entity is inside of an opaque block
    @Override
    public boolean isEntityInsideOpaqueBlock() {
        return false; /// Immune to suffocation.
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        if( hit && (rand.nextInt( 3 ) == 0 || rand.nextInt( 1 + looting ) > 0) ) {
            dropItem( Items.slime_ball, 1 );
        }
    }
}