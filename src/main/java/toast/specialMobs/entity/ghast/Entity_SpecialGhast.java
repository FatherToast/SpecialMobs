package toast.specialMobs.entity.ghast;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.ISpecialMob;
import toast.specialMobs.entity.SpecialMobData;

public class Entity_SpecialGhast extends EntityGhast implements ISpecialMob {
    
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] { new ResourceLocation( "textures/entity/ghast/ghast.png" ), new ResourceLocation( "textures/entity/ghast/ghast_shooting.png" ) };
    
    /// The base explosion strength of this ghast's fireballs.
    public int explosionStrength = 1;
    /// The currently targeted entity.
    public Entity targetedEntity;
    /// Cooldown time between target loss and new target aquirement.
    public int aggroCooldown;
    
    /// This mob's special mob data.
    private SpecialMobData specialData;
    
    public Entity_SpecialGhast( World world ) {
        super( world );
        getSpecialData().isImmuneToFire = isImmuneToFire;
    }
    
    /// Used to initialize data watcher variables.
    @Override
    protected void entityInit() {
        specialData = new SpecialMobData( this, Entity_SpecialGhast.TEXTURES );
        super.entityInit();
        setRangedAI( 20, 60, 64.0F );
    }
    
    /// Helper method to set the attack AI more easily.
    protected void setRangedAI( int minDelay, int maxDelay, float range ) {
        SpecialMobData data = getSpecialData();
        data.arrowRefireMin = (short) minDelay;
        data.arrowRefireMax = (short) maxDelay;
        data.arrowRange = range;
    }
    
    /// Initializes this entity's attributes.
    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute( SharedMonsterAttributes.attackDamage );
        getEntityAttribute( SharedMonsterAttributes.attackDamage ).setBaseValue( 4.0 );
    }
    
    @Override
    public SpecialMobData getSpecialData() {
        return specialData;
    }
    
    @Override
    public void adjustEntityAttributes() {
        float prevMax = getMaxHealth();
        adjustTypeAttributes();
        setHealth( getMaxHealth() + getHealth() - prevMax );
    }
    
    /// Overridden to modify inherited attribites.
    protected void adjustTypeAttributes() {
        /// Override to alter attributes.
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
        EntityGhast proxy = new EntityGhast( worldObj );
        proxy.getAttributeMap().registerAttribute( SharedMonsterAttributes.attackDamage ); // Vanilla ghasts do not have this attribute
        return getSpecialData().onSpawnWithEgg( data, proxy );
    }
    
    /// Called to tick this entity's AI.
    @Override
    protected void updateEntityActionState() {
        /// Check for despawning.
        if( !worldObj.isRemote && worldObj.difficultySetting.getDifficultyId() == 0 ) {
            setDead();
        }
        despawnEntity();
        prevAttackCounter = attackCounter;
        /// Update movement, targets, and attacks.
        updateEntityGoal();
        /// Update the texture.
        if( !worldObj.isRemote ) {
            boolean shooting = getFireTexture() == 1;
            boolean shouldBeShooting = attackCounter > 10;
            if( shooting != shouldBeShooting ) {
                setFireTexture( shouldBeShooting );
            }
        }
    }
    
    // Updates the current goal.
    protected void updateEntityGoal() {
        // Perform movement.
        double vX = waypointX - posX;
        double vY = waypointY - posY;
        double vZ = waypointZ - posZ;
        double v = vX * vX + vY * vY + vZ * vZ;
        
        if( v < 1.0 || v > 3600.0 ) {
            setRandomWaypoints( 32.0F );
        }
        if( courseChangeCooldown-- <= 0 ) {
            courseChangeCooldown += rand.nextInt( 5 ) + 2;
            v = Math.sqrt( v );
            
            if( isCourseTraversable( v ) ) {
                double speed = getEntityAttribute( SharedMonsterAttributes.movementSpeed ).getAttributeValue() / v;
                motionX += vX * speed;
                motionY += vY * speed;
                motionZ += vZ * speed;
            }
            else {
                clearWaypoints();
            }
        }
        // Update the current target.
        updateEntityTarget();
        // Execute goal, if able.
        if( targetedEntity != null && targetedEntity.getDistanceSqToEntity( this ) < getSpecialData().arrowRange * getSpecialData().arrowRange ) {
            double x = targetedEntity.posX - posX;
            double z = targetedEntity.posZ - posZ;
            renderYawOffset = rotationYaw = -((float) Math.atan2( x, z )) * 180.0F / (float) Math.PI;
            
            if( canEntityBeSeen( targetedEntity ) ) {
                if( attackCounter == getSpecialData().arrowRefireMin >> 1 ) {
                    worldObj.playAuxSFXAtEntity( null, 1007, (int) posX, (int) posY, (int) posZ, 0 );
                }
                attackCounter++;
                
                if( attackCounter >= getSpecialData().arrowRefireMin ) {
                    if( !worldObj.isRemote ) {
                        shootFireballAtEntity( targetedEntity );
                    }
                    attackCounter = getSpecialData().arrowRefireMin - getSpecialData().arrowRefireMax;
                }
            }
            else if( attackCounter > 0 ) {
                attackCounter--;
            }
        }
        else {
            renderYawOffset = rotationYaw = -((float) Math.atan2( motionX, motionZ )) * 180.0F / (float) Math.PI;
            
            if( attackCounter > 0 ) {
                attackCounter--;
            }
        }
    }
    
    // Sets a random waypoint within range.
    public void setRandomWaypoints( float range ) {
        waypointX = posX + (rand.nextFloat() - 0.5F) * range;
        waypointY = posY + (rand.nextFloat() - 0.5F) * range;
        waypointZ = posZ + (rand.nextFloat() - 0.5F) * range;
    }
    
    // Sets a random waypoint within range.
    public void clearWaypoints() {
        waypointX = posX;
        waypointY = posY;
        waypointZ = posZ;
    }
    
    // True if the ghast has an unobstructed line of travel to the waypoint.
    public boolean isCourseTraversable( double v ) {
        double dX = (waypointX - posX) / v;
        double dY = (waypointY - posY) / v;
        double dZ = (waypointZ - posZ) / v;
        AxisAlignedBB aabb = boundingBox.copy();
        
        for( int i = 1; i < v; i++ ) {
            aabb.offset( dX, dY, dZ );
            
            if( !worldObj.getCollidingBoundingBoxes( this, aabb ).isEmpty() )
                return false;
        }
        return true;
    }
    
    /// Updates this entity's target.
    protected void updateEntityTarget() {
        if( targetedEntity != null && targetedEntity.isDead ) {
            targetedEntity = null;
        }
        if( targetedEntity == null || aggroCooldown-- <= 0 ) {
            targetedEntity = worldObj.getClosestVulnerablePlayerToEntity( this, 100.0 );
            
            if( targetedEntity != null ) {
                aggroCooldown = 20;
            }
        }
    }
    
    /// Get/set functions for the texture.
    public byte getFireTexture() {
        return dataWatcher.getWatchableObjectByte( 16 );
    }
    
    public void setFireTexture( boolean fire ) {
        dataWatcher.updateObject( 16, fire ? (byte) 1 : (byte) 0 );
    }
    
    // Called to attack the target entity with a fireball.
    public void shootFireballAtEntity( Entity target ) {
        double dX = target.posX - posX;
        double dY = target.boundingBox.minY + target.height / 2.0F - posY - height / 2.0F;
        double dZ = target.posZ - posZ;
        worldObj.playAuxSFXAtEntity( null, 1008, (int) posX, (int) posY, (int) posZ, 0 );
        EntityLargeFireball fireball = new EntityLargeFireball( worldObj, this, dX, dY, dZ );
        fireball.field_92057_e = Math.round( explosionStrength * getTypeExplosionMult() ); // Sets the fireball's explosion strength
        Vec3 vec3 = getLook( 1.0F );
        fireball.posX = posX + vec3.xCoord * width;
        fireball.posY = posY + height / 2.0F + 0.5;
        fireball.posZ = posZ + vec3.zCoord * width;
        worldObj.spawnEntityInWorld( fireball );
    }
    
    /// Called to attack the target entity.
    @Override
    public boolean attackEntityAsMob( Entity target ) {
        float attackDamage = (float) getEntityAttribute( SharedMonsterAttributes.attackDamage ).getAttributeValue();
        int knockback = 0;
        if( target instanceof EntityLivingBase ) {
            attackDamage += EnchantmentHelper.getEnchantmentModifierLiving( this, (EntityLivingBase) target );
            knockback = EnchantmentHelper.getKnockbackModifier( this, (EntityLivingBase) target );
        }
        
        boolean hit = target.attackEntityFrom( DamageSource.causeMobDamage( this ), attackDamage );
        if( hit ) {
            if( knockback > 0 ) {
                target.addVelocity( -MathHelper.sin( rotationYaw * (float) Math.PI / 180.0F ) * knockback * 0.5F, 0.1, MathHelper.cos( rotationYaw * (float) Math.PI / 180.0F ) * knockback * 0.5F );
                motionX *= 0.6;
                motionZ *= 0.6;
            }
            
            int fireAspect = EnchantmentHelper.getFireAspectModifier( this );
            if( fireAspect > 0 ) {
                target.setFire( fireAspect << 2 );
            }
            if( target instanceof EntityLivingBase ) {
                EnchantmentHelper.func_151384_a( (EntityLivingBase) target, this ); // Triggers hit entity's enchants.
            }
            EnchantmentHelper.func_151385_b( this, target ); // Triggers attacker's enchants.
            
            onTypeAttack( target );
        }
        return hit;
    }
    
    /// Overridden to modify attack effects.
    protected void onTypeAttack( Entity target ) {
        // Override to alter attack.
    }
    
    /// Returns the multiplier this ghast has for its explosion size.
    protected float getTypeExplosionMult() {
        return 1.0F;
    }
    
    /// Called by a special fireball's onImpact().
    public void onImpact( Entity fireball, Entity entityHit, double x, double y, double z ) {
        // Override to alter impact.
    }
    
    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT( NBTTagCompound tag ) {
        super.writeEntityToNBT( tag );
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
        saveTag.setInteger( "ExplosionPower", explosionStrength );
        
        getSpecialData().isImmuneToFire = isImmuneToFire;
        getSpecialData().writeToNBT( saveTag );
    }
    
    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT( NBTTagCompound tag ) {
        super.readEntityFromNBT( tag );
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
        if( saveTag.hasKey( "ExplosionPower" ) ) {
            explosionStrength = saveTag.getInteger( "ExplosionPower" );
        }
        else if( tag.hasKey( "ExplosionPower" ) ) {
            explosionStrength = tag.getInteger( "ExplosionPower" );
        }
        
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