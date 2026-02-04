package toast.specialMobs.entity.slime;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.Properties;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.ISpecialMob;
import toast.specialMobs.entity.SpecialMobData;

public class Entity_SpecialSlime extends EntitySlime implements ISpecialMob {
    
    /// Useful properties for this class.
    private static final boolean TINY_SLIME_DAMAGE = Properties.getBoolean( Properties.STATS, "tiny_slime_damage" );
    
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( "textures/entity/slime/slime.png" )
    };
    
    /// This mob's special mob data.
    private SpecialMobData specialData;
    
    /// Ticks the slime must stay on the ground before it can jump again.
    public int slimeJumpDelay;
    
    public Entity_SpecialSlime( World world ) {
        super( world );
    }
    
    /// Gets the multiplier for this type's size.
    protected float getSizeMultiplier() {
        return 0.6F;
    }
    
    /// Gets the additional experience this slime type gives.
    protected int getTypeXp() {
        return 0;
    }
    
    /// Used to initialize data watcher variables.
    @Override
    protected void entityInit() {
        specialData = new SpecialMobData( this, Entity_SpecialSlime.TEXTURES );
        super.entityInit();
    }
    
    /// Initializes this entity's attributes.
    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute( SharedMonsterAttributes.attackDamage );
        getEntityAttribute( SharedMonsterAttributes.attackDamage ).setBaseValue( 0.0 );
    }
    
    /// Returns this mob's special data.
    @Override // ISpecialMob
    public SpecialMobData getSpecialData() {
        return specialData;
    }
    
    /// Called to modify inherited attributes.
    @Override // ISpecialMob
    public void adjustEntityAttributes() {
        adjustTypeAttributes();
    }
    
    /// Overridden to modify inherited attribites, except for health.
    protected void adjustTypeAttributes() {
        // Override to alter attributes.
    }
    
    /// Overridden to modify inherited max health.
    protected void adjustHealthAttribute() {
        // Override to alter attribute.
    }
    
    /// Sets the slime's size and updates its bounding box.
    @Override
    protected void setSlimeSize( int size ) {
        super.setSlimeSize( size );
        
        setSize( size * getSizeMultiplier(), size * getSizeMultiplier() );
        setPosition( posX, posY, posZ );
        adjustHealthAttribute();
        setHealth( getMaxHealth() );
        experienceValue += getTypeXp();
    }
    
    /// Called each tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        getSpecialData().onUpdate();
    }
    
    /// Called when this entity is first spawned to initialize it.
    @Override
    public IEntityLivingData onSpawnWithEgg( IEntityLivingData data ) {
        EntitySlime proxy = new EntitySlime( worldObj );
        proxy.getAttributeMap().registerAttribute( SharedMonsterAttributes.attackDamage ); // Vanilla slimes do not have this attribute
        return getSpecialData().onSpawnWithEgg( data, proxy );
    }
    
    /// Called to tick this entity's AI.
    @Override
    protected void updateEntityActionState() {
        /// Check for despawning.
        despawnEntity();
        /// Acquire target.
        EntityPlayer target = worldObj.getClosestVulnerablePlayerToEntity( this, 16.0 );
        if( target != null ) {
            faceEntity( target, 10.0F, 20.0F );
        }
        attackEntityByType( target );
        /// Update movement.
        if( onGround && slimeJumpDelay-- <= 0 ) {
            slimeJumpDelay = getJumpDelay();
            if( jumpByType( target ) ) {
                isJumping = true;
                if( makesSoundOnJump() ) {
                    playSound( getJumpSound(), getSoundVolume(), ((rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F) * 0.8F );
                }
            }
        }
        else {
            isJumping = false;
            if( onGround ) {
                moveStrafing = moveForward = 0.0F;
            }
        }
    }
    
    /// Called each tick to update behavior.
    protected void attackEntityByType( EntityPlayer target ) {
        /// Override to alter behavior.
    }
    
    /// Called when this slime jumps, returns true if it successfully jumps.
    protected boolean jumpByType( EntityPlayer target ) {
        if( target != null ) {
            slimeJumpDelay /= 3;
        }
        moveStrafing = 1.0F - rand.nextFloat() * 2.0F;
        moveForward = getSlimeSize();
        return true;
    }
    
    /// Called by a player entity when they collide with an entity.
    @Override
    public void onCollideWithPlayer( EntityPlayer player ) {
        if( !worldObj.isRemote && attackTime <= 0 && canDamagePlayer() && canEntityBeSeen( player ) && getDistanceSqToEntity( player ) < width * width + player.width * player.width * 1.3F && attackEntityAsMob( player ) ) {
            attackTime = 20;
            playSound( "mob.slime.attack", 1.0F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F );
        }
    }
    
    /// Indicates weather the slime is able to damage players (based upon the slime's size).
    @Override
    protected boolean canDamagePlayer() {
        return Entity_SpecialSlime.TINY_SLIME_DAMAGE || getSlimeSize() > 1;
    }
    
    /// Called to attack the target.
    @Override
    public boolean attackEntityAsMob( Entity target ) {
        float damage = (float) getEntityAttribute( SharedMonsterAttributes.attackDamage ).getAttributeValue() + getAttackStrength();
        int knockback = 0;
        if( target instanceof EntityLivingBase ) {
            damage += EnchantmentHelper.getEnchantmentModifierLiving( this, (EntityLivingBase) target );
            knockback += EnchantmentHelper.getKnockbackModifier( this, (EntityLivingBase) target );
        }
        
        if( target.attackEntityFrom( DamageSource.causeMobDamage( this ), damage ) ) {
            if( knockback > 0 ) {
                target.addVelocity( -MathHelper.sin( rotationYaw * (float) Math.PI / 180.0F ) * knockback * 0.5F, 0.1, MathHelper.cos( rotationYaw * (float) Math.PI / 180.0F ) * knockback * 0.5F );
                motionX *= 0.6;
                motionZ *= 0.6;
            }
            int fireAspect = EnchantmentHelper.getFireAspectModifier( this );
            if( fireAspect > 0 ) {
                target.setFire( fireAspect * 4 );
            }
            if( target instanceof EntityLivingBase ) {
                EnchantmentHelper.func_151384_a( (EntityLivingBase) target, this );
            }
            EnchantmentHelper.func_151385_b( this, target );
            
            onTypeAttack( target );
            return true;
        }
        return false;
    }
    
    /// Overridden to modify attack effects.
    protected void onTypeAttack( Entity target ) {
        /// Override to alter attack.
    }
    
    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT( NBTTagCompound tag ) {
        super.writeEntityToNBT( tag );
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
        getSpecialData().isImmuneToFire = isImmuneToFire;
        getSpecialData().writeToNBT( saveTag );
    }
    
    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT( NBTTagCompound tag ) {
        super.readEntityFromNBT( tag );
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
        getSpecialData().readFromNBT( tag );
        getSpecialData().readFromNBT( saveTag );
        isImmuneToFire = getSpecialData().isImmuneToFire;
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        if( _SpecialMobs.debug ) {
            dropRareDrop( Math.max( 0, rand.nextInt( 5 ) - 3 ) );
        }
    }
    
    /// Used to create smaller slimes on death.
    @Override
    protected Entity_SpecialSlime createInstance() {
        try {
            return getClass().getConstructor( new Class[] { World.class } ).newInstance( new Object[] { worldObj } );
        }
        catch( Exception ex ) {
            _SpecialMobs.debugException( "Error splitting slime! " + ex.getClass().getName() + " @" + getClass().getName() );
            return new Entity_SpecialSlime( worldObj );
        }
    }
    
    /// Returns the current armor level of this mob.
    @Override
    public int getTotalArmorValue() {
        return Math.min( 20, super.getTotalArmorValue() + getSpecialData().armor );
    }
    
    /// Sets this entity on fire.
    @Override
    public void setFire( int time ) {
        if( !getSpecialData().isImmuneToBurning ) {
            super.setFire( time );
        }
    }
    
    /// Returns the current armor level of this mob.
    @Override
    public boolean allowLeashing() {
        return !getLeashed() && getSpecialData().allowLeashing;
    }
    
    /// Sets the entity inside a web block.
    @Override
    public void setInWeb() {
        if( !getSpecialData().isImmuneToWebs ) {
            super.setInWeb();
        }
    }
    
    /// Called when the mob falls. Calculates and applies fall damage.
    @Override
    protected void fall( float distance ) {
        if( !getSpecialData().isImmuneToFalling ) {
            super.fall( distance );
        }
    }
    
    /// Return whether this entity should NOT trigger a pressure plate or a tripwire.
    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return getSpecialData().ignorePressurePlates;
    }
    
    /// True if the entity can breathe underwater.
    @Override
    public boolean canBreatheUnderwater() {
        return getSpecialData().canBreatheInWater;
    }
    
    /// True if the entity can be pushed by flowing water.
    @Override
    public boolean isPushedByWater() {
        return !getSpecialData().ignoreWaterPush;
    }
    
    /// Returns true if the potion can be applied.
    @Override
    public boolean isPotionApplicable( PotionEffect effect ) {
        return getSpecialData().isPotionApplicable( effect );
    }
}