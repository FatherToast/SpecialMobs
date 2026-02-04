package toast.specialMobs.entity.creeper;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs._SpecialMobs;

import java.util.UUID;

public class EntityEnderCreeper extends Entity_SpecialCreeper {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "creeper/ender.png" ),
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "creeper/ender_eyes.png" )
    };
    
    /// The speed boost when attacking. Identical to an enderman's speed boost.
    private static final UUID attackingSpeedBoostUUID = UUID.fromString( "020E0DFB-87AE-4653-9556-831010E291A0" );
    private static final AttributeModifier attackingSpeedBoost = new AttributeModifier( EntityEnderCreeper.attackingSpeedBoostUUID, "Attacking speed boost", 6.2, 0 ).setSaved( false );
    
    /// The entity to attack last tick. Used to update the attacking speed boost.
    private Entity lastEntityToAttack;
    /// Ticks since this enderman last teleported.
    private int teleportDelay = 0;
    /// Ticks this enderman has been looked at.
    private int lookDelay = 0;
    
    public EntityEnderCreeper( World world ) {
        super( world );
        getSpecialData().setTextures( EntityEnderCreeper.TEXTURES );
        experienceValue += 2;
    }
    
    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        getSpecialData().addAttribute( SharedMonsterAttributes.maxHealth, 20.0 );
        getSpecialData().multAttribute( SharedMonsterAttributes.movementSpeed, 1.2 );
    }
    
    /// Returns true if this mob should use the new AI.
    @Override
    public boolean isAIEnabled() {
        return false;
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        for( int i = rand.nextInt( 2 + looting ); i-- > 0; ) {
            dropItem( Items.ender_pearl, 1 );
        }
    }
    
    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop( int superRare ) {
        ItemStack drop = new ItemStack( Items.fishing_rod );
        EffectHelper.setItemName( drop, "Whip of Destruction", 0xd );
        drop.addEnchantment( Enchantment.sharpness, 1 );
        drop.addEnchantment( Enchantment.unbreaking, 10 );
        entityDropItem( drop, 0.0F );
    }
    
    /// Returns an EntityPlayer to attack or null if none is found.
    @Override
    protected Entity findPlayerToAttack() {
        EntityPlayer player = worldObj.getClosestVulnerablePlayerToEntity( this, 64.0 );
        if( player != null ) {
            if( shouldAttackPlayer( player ) ) {
                if( lookDelay++ == 5 ) {
                    lookDelay = 0;
                    return player;
                }
            }
            else {
                lookDelay = 0;
            }
        }
        return null;
    }
    
    /// Carried from EntityEnderman.class.
    private boolean shouldAttackPlayer( EntityPlayer player ) {
        ItemStack itemStack = player.inventory.armorInventory[3];
        if( itemStack != null && itemStack.getItem() == Item.getItemFromBlock( Blocks.pumpkin ) )
            return false;
        Vec3 lookVec = player.getLook( 1.0F ).normalize();
        Vec3 posVec = Vec3.createVectorHelper( posX - player.posX, boundingBox.minY + height / 2.0 - player.posY - player.getEyeHeight(), posZ - player.posZ );
        double distance = posVec.lengthVector();
        posVec = posVec.normalize();
        double dotProduct = lookVec.dotProduct( posVec );
        return dotProduct > 1.0 - 0.025 / distance && player.canEntityBeSeen( this );
    }
    
    /// Called every tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        if( lastEntityToAttack != entityToAttack ) {
            IAttributeInstance attribute = getEntityAttribute( SharedMonsterAttributes.movementSpeed );
            attribute.removeModifier( EntityEnderCreeper.attackingSpeedBoost );
            if( entityToAttack != null ) {
                attribute.applyModifier( EntityEnderCreeper.attackingSpeedBoost );
            }
        }
        lastEntityToAttack = entityToAttack;
        
        for( int i = 0; i < 2; i++ ) {
            worldObj.spawnParticle( "portal", posX + (rand.nextDouble() - 0.5) * width, posY + rand.nextDouble() * height - 0.25, posZ + (rand.nextDouble() - 0.5) * width, (rand.nextDouble() - 0.5) * 2.0, -rand.nextDouble(), (rand.nextDouble() - 0.5) * 2.0 );
        }
        if( worldObj.isDaytime() && !worldObj.isRemote ) {
            float brightness = getBrightness( 1.0F );
            if( brightness > 0.5F && worldObj.canBlockSeeTheSky( (int) Math.floor( posX ), (int) Math.floor( posY ), (int) Math.floor( posZ ) ) && rand.nextFloat() * 30.0F < (brightness - 0.4F) * 2.0F ) {
                entityToAttack = null;
                teleportRandomly();
            }
        }
        if( isWet() ) {
            attackEntityFrom( DamageSource.drown, 1 );
            entityToAttack = null;
            teleportRandomly();
        }
        isJumping = false;
        if( entityToAttack != null ) {
            if( entityToAttack.getDistanceSqToEntity( this ) < 9.0 && canEntityBeSeen( entityToAttack ) ) {
                setCreeperState( 1 );
            }
            else {
                setCreeperState( -1 );
            }
            faceEntity( entityToAttack, 100.0F, 100.0F );
        }
        if( !worldObj.isRemote && isEntityAlive() ) {
            if( entityToAttack != null ) {
                if( entityToAttack instanceof EntityPlayer && shouldAttackPlayer( (EntityPlayer) entityToAttack ) ) {
                    if( getCreeperState() < 0 && entityToAttack.getDistanceSqToEntity( this ) < 16.0 ) {
                        teleportRandomly();
                    }
                    teleportDelay = 0;
                }
                else if( entityToAttack.getDistanceSqToEntity( this ) > 256.0 && teleportDelay++ >= 30 && teleportToEntity( entityToAttack ) ) {
                    teleportDelay = 0;
                }
            }
            else {
                teleportDelay = 0;
            }
        }
        super.onLivingUpdate();
    }
    
    /// Damages this entity from the damageSource by the given amount. Returns true if this entity is damaged.
    @Override
    public boolean attackEntityFrom( DamageSource damageSource, float damage ) {
        if( damageSource instanceof EntityDamageSourceIndirect ) {
            for( int i = 0; i < 64; i++ ) {
                if( teleportRandomly() )
                    return true;
            }
        }
        return super.attackEntityFrom( damageSource, damage );
    }
    
    /// Teleports this enderman to a random nearby location. Returns true if this entity teleports.
    protected boolean teleportRandomly() {
        double x = posX + (rand.nextDouble() - 0.5) * 64.0;
        double y = posY + (rand.nextInt( 64 ) - 32);
        double z = posZ + (rand.nextDouble() - 0.5) * 64.0;
        return teleportTo( x, y, z );
    }
    
    /// Teleports this enderman to the given entity. Returns true if this entity teleports.
    protected boolean teleportToEntity( Entity entity ) {
        Vec3 vector = Vec3.createVectorHelper( posX - entity.posX, boundingBox.minY + height / 2.0F - entity.posY + entity.getEyeHeight(), posZ - entity.posZ );
        vector = vector.normalize();
        double x = posX + (rand.nextDouble() - 0.5) * 8.0 - vector.xCoord * 16.0;
        double y = posY + (rand.nextInt( 16 ) - 8) - vector.yCoord * 16.0;
        double z = posZ + (rand.nextDouble() - 0.5) * 8.0 - vector.zCoord * 16.0;
        return teleportTo( x, y, z );
    }
    
    /// Teleports this enderman to the given coordinates. Returns true if this entity teleports.
    protected boolean teleportTo( double x, double y, double z ) {
        double xI = posX;
        double yI = posY;
        double zI = posZ;
        posX = x;
        posY = y;
        posZ = z;
        boolean canTeleport = false;
        int blockX = (int) Math.floor( posX );
        int blockY = (int) Math.floor( posY );
        int blockZ = (int) Math.floor( posZ );
        Block block;
        if( worldObj.blockExists( blockX, blockY, blockZ ) ) {
            boolean canTeleportToBlock = false;
            while( !canTeleportToBlock && blockY > 0 ) {
                block = worldObj.getBlock( blockX, blockY - 1, blockZ );
                if( block != null && block.getMaterial().blocksMovement() ) {
                    canTeleportToBlock = true;
                }
                else {
                    --posY;
                    --blockY;
                }
            }
            if( canTeleportToBlock ) {
                setPosition( posX, posY, posZ );
                if( worldObj.getCollidingBoundingBoxes( this, boundingBox ).isEmpty() && !worldObj.isAnyLiquid( boundingBox ) ) {
                    canTeleport = true;
                }
            }
        }
        if( !canTeleport ) {
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
            worldObj.spawnParticle( "portal", dX, dY, dZ, vX, vY, vZ );
        }
        worldObj.playSoundEffect( xI, yI, zI, "mob.endermen.portal", 1.0F, 1.0F );
        worldObj.playSoundAtEntity( this, "mob.endermen.portal", 1.0F, 1.0F );
        return true;
    }
}