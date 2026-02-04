package toast.specialMobs.entity.witch;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs._SpecialMobs;

public class EntityWindWitch extends Entity_SpecialWitch {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "witch/wind.png" )
    };
    
    /// Ticks before this entity can teleport.
    public int teleportDelay;
    
    public EntityWindWitch( World world ) {
        super( world );
        getSpecialData().setTextures( EntityWindWitch.TEXTURES );
        getSpecialData().isImmuneToFalling = true;
        experienceValue += 2;
    }
    
    /// Override to set the attack AI to use.
    @Override
    protected void initTypeAI() {
        setMeleeAI();
    }
    
    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        getSpecialData().addAttribute( SharedMonsterAttributes.attackDamage, 2.0 );
        getSpecialData().multAttribute( SharedMonsterAttributes.movementSpeed, 1.2 );
        
        ItemStack itemStack = new ItemStack( Items.wooden_sword );
        float tension = worldObj.func_147462_b( posX, posY, posZ );
        
        if( rand.nextFloat() < 0.25F * tension ) {
            try {
                EnchantmentHelper.addRandomEnchantment( rand, itemStack, (int) (5.0F + tension * rand.nextInt( 18 )) );
            }
            catch( Exception ex ) {
                _SpecialMobs.console( "Error applying enchantments! entity:" + this );
                // noinspection all
                ex.printStackTrace();
            }
        }
        setCurrentItemOrArmor( 0, itemStack );
    }
    
    /// Called every tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        if( !worldObj.isRemote && isEntityAlive() && teleportDelay-- <= 0 && getAttackTarget() != null && rand.nextInt( 20 ) == 0 ) {
            if( getAttackTarget().getDistanceSqToEntity( this ) > 36.0 ) {
                removePotionEffect( Potion.invisibility.id );
                for( int i = 0; i < 16; i++ ) {
                    if( teleportToEntity( getAttackTarget() ) ) {
                        teleportDelay = 60;
                        break;
                    }
                }
            }
            else {
                addPotionEffect( new PotionEffect( Potion.invisibility.id, 30 ) );
                for( int i = 0; i < 16; i++ ) {
                    if( teleportRandomly() ) {
                        teleportDelay = 30;
                        break;
                    }
                }
            }
        }
        super.onLivingUpdate();
    }
    
    /// Damages this entity from the damageSource by the given amount. Returns true if this entity is damaged.
    @Override
    public boolean attackEntityFrom( DamageSource damageSource, float damage ) {
        if( !worldObj.isRemote && damageSource.getEntity() != null ) {
            if( (teleportDelay -= 15) <= 0 && (damageSource instanceof EntityDamageSourceIndirect || rand.nextBoolean()) ) {
                double xI = posX;
                double yI = posY;
                double zI = posZ;
                
                for( int i = 0; i < 64; i++ ) {
                    if( teleportRandomly() ) {
                        teleportDelay = 30;
                        addPotionEffect( new PotionEffect( Potion.invisibility.id, 30 ) );
                        if( damageSource instanceof EntityDamageSourceIndirect )
                            return true;
                        boolean hit = super.attackEntityFrom( damageSource, damage );
                        
                        if( getHealth() <= 0.0F ) {
                            setPosition( xI, yI, zI );
                        }
                        return hit;
                    }
                }
            }
            else {
                removePotionEffect( Potion.invisibility.id );
            }
        }
        return super.attackEntityFrom( damageSource, damage );
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        if( hit && (rand.nextInt( 3 ) == 0 || rand.nextInt( 1 + looting ) > 0) ) {
            dropItem( Items.feather, 1 );
        }
    }
    
    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop( int superRare ) {
        ItemStack potion = new ItemStack( Items.potionitem, 1, 8206 );
        EffectHelper.setItemName( potion, "Potion of Hiding", 0xf );
        EffectHelper.addPotionEffect( potion, Potion.invisibility, 1200, 0 );
        EffectHelper.addPotionEffect( potion, Potion.blindness, 1200, 0 );
        entityDropItem( potion, 0.0F );
    }
    
    /// Teleports this enderman to a random nearby location. Returns true if this entity teleports.
    protected boolean teleportRandomly() {
        double x = posX + (rand.nextDouble() - 0.5) * 20.0;
        double y = posY + (rand.nextInt( 12 ) - 4);
        double z = posZ + (rand.nextDouble() - 0.5) * 20.0;
        return teleportTo( x, y, z );
    }
    
    /// Teleports this enderman to the given entity. Returns true if this entity teleports.
    protected boolean teleportToEntity( Entity entity ) {
        double x = entity.posX + (rand.nextDouble() - 0.5) * 8.0;
        double y = entity.posY + rand.nextInt( 8 ) - 2;
        double z = entity.posZ + (rand.nextDouble() - 0.5) * 8.0;
        return teleportTo( x, y, z );
    }
    
    /// Teleports this enderman to the given coordinates. Returns true if this entity teleports.
    protected boolean teleportTo( double x, double y, double z ) {
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
        return true;
    }
}