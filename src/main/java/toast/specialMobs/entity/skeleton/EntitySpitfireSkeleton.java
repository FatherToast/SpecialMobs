package toast.specialMobs.entity.skeleton;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;

public class EntitySpitfireSkeleton extends Entity_SpecialSkeleton {
    
    public EntitySpitfireSkeleton( World world ) {
        super( world );
        getNavigator().setAvoidsWater( true );
        stepHeight = 1.0F;
        setSize( 0.9F, 2.7F );
        updateScale();
        getSpecialData().resetRenderScale( 1.5F );
        getSpecialData().setTextures( EntityFireSkeleton.TEXTURES );
        getSpecialData().isImmuneToFire = isImmuneToFire = true;
        getSpecialData().isDamagedByWater = true;
        experienceValue += 4;
    }
    
    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        getSpecialData().addAttribute( SharedMonsterAttributes.maxHealth, 20.0 );
        getSpecialData().addAttribute( SharedMonsterAttributes.attackDamage, 2.0 );
        getSpecialData().arrowSpread = 2.0F;
        
        ItemStack itemStack = getHeldItem();
        if( itemStack != null ) {
            if( itemStack.getItem() instanceof ItemBow ) {
                EffectHelper.overrideEnchantment( itemStack, Enchantment.flame, rand.nextInt( Enchantment.flame.getMaxLevel() ) + 1 );
            }
            else {
                EffectHelper.overrideEnchantment( itemStack, Enchantment.fireAspect, rand.nextInt( Enchantment.fireAspect.getMaxLevel() ) + 1 );
            }
        }
    }
    
    /// Overridden to modify attack effects.
    @Override
    protected void onTypeAttack( Entity target ) {
        if( getHeldItem() == null ) {
            target.setFire( 10 );
        }
    }
    
    /// Attack the specified entity using a ranged attack.
    @Override
    public void attackEntityWithRangedAttack( EntityLivingBase target, float range ) {
        double dX = target.posX - posX;
        double dY = target.boundingBox.minY + target.height / 2.0F - posY - height / 2.0F;
        double dZ = target.posZ - posZ;
        float spread = (float) Math.sqrt( range ) * getSpecialData().arrowSpread;
        worldObj.playAuxSFXAtEntity( null, 1009, (int) posX, (int) posY, (int) posZ, 0 );
        EntitySmallFireball fireball;
        
        for( int i = 0; i < 4; i++ ) {
            fireball = new EntitySmallFireball( worldObj, this, dX + rand.nextGaussian() * spread, dY, dZ + rand.nextGaussian() * spread );
            fireball.posY = posY + height / 2.0F + 0.5;
            worldObj.spawnEntityInWorld( fireball );
        }
    }
    
    /// Called every tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if( worldObj.isRemote && getSkeletonType() == 1 ) {
            setSize( 0.95F, 3.24F );
            updateScale();
        }
    }
    
    /// If true, this entity is a baby.
    @Override
    public boolean isChild() {
        return false;
    }
    
    /// Called when the entity is attacked.
    @Override
    public boolean attackEntityFrom( DamageSource damageSource, float damage ) {
        if( damageSource.getSourceOfDamage() instanceof EntitySnowball ) {
            damage = Math.max( 2.0F, damage );
        }
        return super.attackEntityFrom( damageSource, damage );
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        for( int i = rand.nextInt( 2 ) + 1; i-- > 0; ) {
            dropItem( Items.bone, 1 );
        }
        for( int i = rand.nextInt( 3 + looting ); i-- > 0; ) {
            dropItem( Items.fire_charge, 1 );
        }
    }
    
    /// Set this skeleton's type.
    @Override
    public void setSkeletonType( int type ) {
        super.setSkeletonType( type );
        if( type == 1 ) {
            setSize( 0.95F, 3.24F );
        }
        else {
            setSize( 0.9F, 2.7F );
        }
        updateScale();
    }
    
    /// Returns true if this mob should be rendered on fire.
    @Override
    public boolean isBurning() {
        return isEntityAlive() && !isWet();
    }
}