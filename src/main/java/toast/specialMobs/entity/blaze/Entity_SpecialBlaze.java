package toast.specialMobs.entity.blaze;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.ISpecialMob;
import toast.specialMobs.entity.SpecialMobData;

public class Entity_SpecialBlaze extends EntityBlaze implements ISpecialMob {
    /// Useful properties for this class.
    //private static final double HOSTILE_CHANCE = Properties.getDouble(Properties.STATS, "hostile_pigzombies");
    
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] { new ResourceLocation( "textures/entity/blaze.png" ) };
    
    /// This mob's special mob data.
    private SpecialMobData specialData;
    
    /// The state of this blaze's attack.
    public int attackState;
    /// The amount of fireballs in each burst.
    public short fireballBurstCount;
    /// The ticks between each shot in a burst.
    public short fireballBurstDelay;
    
    public Entity_SpecialBlaze( World world ) {
        super( world );
        getSpecialData().isImmuneToFire = isImmuneToFire;
        setRangedAI( 3, 6, 60, 100, 30.0F );
        getSpecialData().arrowSpread = 0.5F;
    }
    
    /// Used to initialize data watcher variables.
    @Override
    protected void entityInit() {
        specialData = new SpecialMobData( this, Entity_SpecialBlaze.TEXTURES );
        super.entityInit();
    }
    
    /// Helper method to set the attack AI more easily.
    protected void setRangedAI( int burstCount, int burstDelay, int chargeTime, int cooldownTime, float range ) {
        fireballBurstCount = (short) burstCount;
        fireballBurstDelay = (short) burstDelay;
        
        SpecialMobData data = getSpecialData();
        data.arrowRefireMin = (short) chargeTime;
        data.arrowRefireMax = (short) (chargeTime + cooldownTime);
        data.arrowRange = range;
    }
    
    /// Returns this mob's special data.
    @Override
    /// ISpecialMob
    public SpecialMobData getSpecialData() {
        return specialData;
    }
    
    /// Called to modify inherited attributes.
    @Override
    /// ISpecialMob
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
        return getSpecialData().onSpawnWithEgg( data, new EntityBlaze( worldObj ) );
    }
    
    /// Called each tick this entity's attack target can be seen.
    @Override
    protected void attackEntity( Entity target, float distance ) {
        SpecialMobData data = getSpecialData();
        if( attackTime <= 0 && distance < 2.0F && target.boundingBox.maxY > boundingBox.minY && target.boundingBox.minY < boundingBox.maxY ) {
            attackTime = 20;
            attackEntityAsMob( target );
        }
        else if( canBeHurtByFire( target ) && distance < data.arrowRange ) {
            if( attackTime == 0 ) {
                attackState++;
                if( attackState == 1 ) {
                    attackTime = data.arrowRefireMin;
                    func_70844_e( true ); // setRenderBurning
                }
                else if( attackState <= fireballBurstCount + 1 ) {
                    attackTime = fireballBurstDelay;
                }
                else {
                    attackTime = data.arrowRefireMax - data.arrowRefireMin;
                    attackState = 0;
                    func_70844_e( false ); // setRenderBurning
                }
                
                if( attackState > 1 ) {
                    shootFireballAtEntity( target, distance );
                }
            }
            rotationYaw = (float) (Math.atan2( target.posZ - posZ, target.posX - posX ) * 180.0 / Math.PI) - 90.0F;
            hasAttacked = true;
        }
        else {
            if( onGround ) {
                moveEntityWithHeading( 0.0F, (float) getEntityAttribute( SharedMonsterAttributes.movementSpeed ).getAttributeValue() * 7.0F );
            }
            else {
                moveFlying( 0.0F, (float) getEntityAttribute( SharedMonsterAttributes.movementSpeed ).getAttributeValue() * 7.0F, 0.03F );
            }
        }
    }
    
    // Returns true if the target can be hurt by fireballs.
    protected boolean canBeHurtByFire( Entity entity ) {
        return !entity.isImmuneToFire() && (!(entity instanceof EntityLivingBase) || !((EntityLivingBase) entity).isPotionActive( Potion.fireResistance ));
    }
    
    // Called to attack the target entity with a fireball.
    public void shootFireballAtEntity( Entity target, float distance ) {
        double dX = target.posX - posX;
        double dY = target.boundingBox.minY + target.height / 2.0F - posY - height / 2.0F;
        double dZ = target.posZ - posZ;
        float spread = (float) Math.sqrt( distance ) * getSpecialData().arrowSpread;
        worldObj.playAuxSFXAtEntity( null, 1009, (int) posX, (int) posY, (int) posZ, 0 );
        EntitySmallFireball fireball = new EntitySmallFireball( worldObj, this, dX + rand.nextGaussian() * spread, dY, dZ + rand.nextGaussian() * spread );
        fireball.posY = posY + height / 2.0F + 0.5;
        worldObj.spawnEntityInWorld( fireball );
    }
    
    /// Called to attack the target.
    @Override
    public boolean attackEntityAsMob( Entity target ) {
        if( super.attackEntityAsMob( target ) ) {
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
        saveTag.setShort( "SMFireballBurstCount", fireballBurstCount );
        saveTag.setShort( "SMFireballBurstDelay", fireballBurstDelay );
        
        getSpecialData().isImmuneToFire = isImmuneToFire;
        getSpecialData().writeToNBT( saveTag );
    }
    
    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT( NBTTagCompound tag ) {
        super.readEntityFromNBT( tag );
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
        if( saveTag.hasKey( "SMFireballBurstCount" ) ) {
            fireballBurstCount = saveTag.getShort( "SMFireballBurstCount" );
        }
        else if( tag.hasKey( "SMFireballBurstCount" ) ) {
            fireballBurstCount = tag.getShort( "SMFireballBurstCount" );
        }
        if( saveTag.hasKey( "SMFireballBurstDelay" ) ) {
            fireballBurstDelay = saveTag.getShort( "SMFireballBurstDelay" );
        }
        else if( tag.hasKey( "SMFireballBurstDelay" ) ) {
            fireballBurstDelay = tag.getShort( "SMFireballBurstDelay" );
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