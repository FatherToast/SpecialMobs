package toast.specialMobs.entity.spider;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.Properties;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.EntitySpecialSpitball;
import toast.specialMobs.entity.ISpecialMob;
import toast.specialMobs.entity.SpecialMobData;

import java.util.UUID;

public class Entity_SpecialSpider extends EntitySpider implements ISpecialMob, IRangedAttackMob {
    
    /// Useful properties for this class.
    private static final double HOSTILE_CHANCE = Properties.getDouble( Properties.STATS, "hostile_spiders" );
    private static final double SPIT_CHANCE = Properties.getDouble( Properties.STATS, "spit_chance_spider" );
    /// Attacking speed boost modifier.
    private static final UUID stopModifierUUID = UUID.fromString( "70A57A49-7EC5-45BA-B886-3B90B23A1718" );
    private static final AttributeModifier stopModifier = new AttributeModifier( Entity_SpecialSpider.stopModifierUUID, "Attacking speed boost", -1.0, 2 ).setSaved( false );
    
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( "textures/entity/spider/spider.png" ),
            new ResourceLocation( "textures/entity/spider_eyes.png" )
    };
    
    /// If true, this spider will attack regardless of light level.
    public boolean isHostile;
    /// Used to trick the spider into thinking it is in darkness when isHostile is true.
    public boolean fakeDarkness;
    
    /// Delay until the next spit attack.
    public int spitDelay = 0;
    /// Ticks until this spider stops moving once in range for a spit attack.
    public byte sightDelay = 20;
    /// True if the spider can see its target (used for spitting spiders).
    public boolean seesTarget = false;
    
    /// This mob's special mob data.
    private SpecialMobData specialData;
    
    public Entity_SpecialSpider( World world ) {
        super( world );
        getSpecialData().immuneToPotions.add( Potion.poison.id );
        getSpecialData().isImmuneToWebs = true;
    }
    
    /// Used to initialize data watcher variables.
    @Override
    protected void entityInit() {
        specialData = new SpecialMobData( this, Entity_SpecialSpider.TEXTURES );
        super.entityInit();
        if( rand.nextDouble() < Entity_SpecialSpider.SPIT_CHANCE ) {
            setRangedAI( 15, 40, 10.0F );
        }
    }
    
    /// Helper method to set the attack AI more easily.
    protected void setRangedAI( int minDelay, int maxDelay, float range ) {
        SpecialMobData data = getSpecialData();
        data.arrowRefireMin = (short) minDelay;
        data.arrowRefireMax = (short) maxDelay;
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
        if( rand.nextDouble() < Entity_SpecialSpider.HOSTILE_CHANCE ) {
            isHostile = true;
        }
        
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
        if( spitDelay > 0 ) {
            spitDelay--;
        }
        super.onLivingUpdate();
        getSpecialData().onUpdate();
    }
    
    /// Called to update this entity's AI.
    @Override
    protected void updateEntityActionState() {
        seesTarget = false;
        super.updateEntityActionState();
        if( !seesTarget ) {
            sightDelay = 20;
            IAttributeInstance attribute = getEntityAttribute( SharedMonsterAttributes.movementSpeed );
            if( attribute.getModifier( Entity_SpecialSpider.stopModifierUUID ) != null ) {
                attribute.removeModifier( Entity_SpecialSpider.stopModifier );
            }
        }
        
        if( isHostile && !hasPath() ) {
            rotationYaw = MathHelper.wrapAngleTo180_float( rotationYaw + (float) rand.nextGaussian() * 20.0F );
        }
    }
    
    /// Finds the closest player within 16 blocks to attack, or null if this Entity isn't interested in attacking.
    @Override
    protected Entity findPlayerToAttack() {
        if( isHostile ) {
            fakeDarkness = true;
        }
        return super.findPlayerToAttack();
    }
    
    /// Called each tick this entity's attack target can be seen.
    @Override
    protected void attackEntity( Entity target, float distance ) {
        seesTarget = true;
        IAttributeInstance attribute = getEntityAttribute( SharedMonsterAttributes.movementSpeed );
        boolean stopped = attribute.getModifier( Entity_SpecialSpider.stopModifierUUID ) != null;
        
        if( getSpecialData().arrowRange > 0.0F ) {
            if( !worldObj.isRemote && distance < getSpecialData().arrowRange ) {
                if( target instanceof EntityLivingBase && spitDelay <= 0 ) {
                    float damage = distance / getSpecialData().arrowRange;
                    spitDelay = (int) (damage * (getSpecialData().arrowRefireMax - getSpecialData().arrowRefireMin) + getSpecialData().arrowRefireMin);
                    damage = Math.max( 0.1F, Math.min( 1.0F, damage ) );
                    attackEntityWithRangedAttack( (EntityLivingBase) target, damage );
                }
                if( sightDelay > 0 ) {
                    sightDelay--;
                }
                boolean shouldStop = sightDelay <= 0;
                
                if( stopped != shouldStop ) {
                    if( shouldStop ) {
                        attribute.applyModifier( Entity_SpecialSpider.stopModifier );
                    }
                    else {
                        attribute.removeModifier( Entity_SpecialSpider.stopModifier );
                    }
                }
            }
            else {
                sightDelay = 20;
            }
        }
        else {
            if( stopped ) {
                attribute.removeModifier( Entity_SpecialSpider.stopModifier );
            }
            if( isHostile ) {
                fakeDarkness = true;
            }
            super.attackEntity( target, distance );
        }
    }
    
    /// Attack the specified entity using a ranged attack.
    @Override
    public void attackEntityWithRangedAttack( EntityLivingBase target, float range ) {
        EntitySpecialSpitball spitball = new EntitySpecialSpitball( worldObj, this, target, 0.6F, getTypeArrowSpread() );
        spitball.setDamage( range * getSpecialData().arrowDamage + (float) rand.nextGaussian() * 0.25F + worldObj.difficultySetting.getDifficultyId() * 0.11F );
        
        playSound( "mob.slime.attack", 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 0.8F) );
        worldObj.spawnEntityInWorld( spitball );
    }
    
    /// Overridden to modify the base arrow variance.
    protected float getTypeArrowSpread() {
        return getSpecialData().arrowSpread - worldObj.difficultySetting.getDifficultyId() * (getSpecialData().arrowSpread / 4.0F + 0.5F);
    }
    
    /// Gets how bright this entity is.
    @Override
    public float getBrightness( float partialTick ) {
        if( fakeDarkness ) {
            fakeDarkness = false;
            return 0.0F;
        }
        return super.getBrightness( partialTick );
    }
    
    /// Called when this entity is first spawned to initialize it.
    @Override
    public IEntityLivingData onSpawnWithEgg( IEntityLivingData data ) {
        return getSpecialData().onSpawnWithEgg( data, new EntitySpider( worldObj ) );
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
    public void onTypeAttack( Entity target ) {
        /// Override to alter attack.
    }
    
    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT( NBTTagCompound tag ) {
        super.writeEntityToNBT( tag );
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
        if( isHostile ) {
            saveTag.setBoolean( "SMAnger", true );
        }
        
        getSpecialData().isImmuneToFire = isImmuneToFire;
        getSpecialData().writeToNBT( saveTag );
    }
    
    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT( NBTTagCompound tag ) {
        super.readEntityFromNBT( tag );
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
        if( saveTag.hasKey( "SMAnger" ) ) {
            isHostile = saveTag.getBoolean( "SMAnger" );
        }
        else if( tag.hasKey( "SMAnger" ) ) {
            isHostile = tag.getBoolean( "SMAnger" );
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