package toast.specialMobs.entity.pigzombie;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.Properties;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.ISpecialMob;
import toast.specialMobs.entity.SpecialMobData;

import java.util.UUID;

public class Entity_SpecialPigZombie extends EntityPigZombie implements ISpecialMob, IRangedAttackMob {
    
    /// Useful properties for this class.
    private static final double HOSTILE_CHANCE = Properties.getDouble( Properties.STATS, "hostile_pigzombies" );
    private static final double BOW_CHANCE = Properties.getDouble( Properties.STATS, "bow_chance_pigzombie" );
    /// Attacking speed boost modifier.
    private static final UUID stopModifierUUID = UUID.fromString( "70A57A49-7EC5-45BA-B886-3B90B23A1718" );
    private static final AttributeModifier stopModifier = new AttributeModifier( Entity_SpecialPigZombie.stopModifierUUID, "Attacking speed boost", -1.0, 2 ).setSaved( false );
    
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] { new ResourceLocation( "textures/entity/zombie_pigman.png" ) };
    
    /// Extra data to make sure anger level is saved.
    public boolean isHostile;
    
    /// Ticks until this pig zombie stops moving once in range for a bow attack.
    public byte sightDelay = 20;
    /// True if the pig zombie can see its target (used for bow pigmen).
    public boolean seesTarget = false;
    
    /// This mob's special mob data.
    private SpecialMobData specialData;
    
    public Entity_SpecialPigZombie( World world ) {
        super( world );
        getSpecialData().isImmuneToFire = isImmuneToFire;
        setRangedAI( 20, 60, 13.0F );
    }
    
    /// Used to initialize data watcher variables.
    @Override
    protected void entityInit() {
        specialData = new SpecialMobData( this, Entity_SpecialPigZombie.TEXTURES );
        super.entityInit();
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
        if( !isHostile && rand.nextDouble() < Entity_SpecialPigZombie.HOSTILE_CHANCE ) {
            NBTTagCompound entityData = new NBTTagCompound();
            writeToNBT( entityData );
            entityData.setShort( "Anger", (short) (400 + rand.nextInt( 400 )) );
            readFromNBT( entityData );
        }
        if( rand.nextDouble() < Entity_SpecialPigZombie.BOW_CHANCE ) {
            ItemStack itemStack = new ItemStack( Items.bow );
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
        
        float prevMax = getMaxHealth();
        adjustTypeAttributes();
        setHealth( getMaxHealth() + getHealth() - prevMax );
    }
    
    /// Overridden to modify inherited attribites.
    protected void adjustTypeAttributes() {
        /// Override to alter attributes.
    }
    
    /// Called to update this entity's AI.
    @Override
    protected void updateEntityActionState() {
        seesTarget = false;
        super.updateEntityActionState();
        if( !seesTarget ) {
            sightDelay = 20;
            IAttributeInstance attribute = getEntityAttribute( SharedMonsterAttributes.movementSpeed );
            if( attribute.getModifier( Entity_SpecialPigZombie.stopModifierUUID ) != null ) {
                attribute.removeModifier( Entity_SpecialPigZombie.stopModifier );
            }
        }
        
        if( !isHostile && entityToAttack instanceof EntityPlayer ) {
            isHostile = true;
        }
        if( isHostile && !hasPath() ) {
            rotationYaw = MathHelper.wrapAngleTo180_float( rotationYaw + (float) rand.nextGaussian() * 20.0F );
        }
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
        return getSpecialData().onSpawnWithEgg( data, new EntityPigZombie( worldObj ) );
    }
    
    /// Called each tick this entity's attack target can be seen.
    @Override
    protected void attackEntity( Entity target, float distance ) {
        seesTarget = true;
        IAttributeInstance attribute = getEntityAttribute( SharedMonsterAttributes.movementSpeed );
        boolean stopped = attribute.getModifier( Entity_SpecialPigZombie.stopModifierUUID ) != null;
        if( willShootBow() && getHeldItem() != null && getHeldItem().getItem() instanceof ItemBow ) {
            if( !worldObj.isRemote && distance < getSpecialData().arrowRange ) {
                if( target instanceof EntityLivingBase && attackTime <= 0 ) {
                    float damage = distance / getSpecialData().arrowRange;
                    attackTime = (int) (damage * (getSpecialData().arrowRefireMax - getSpecialData().arrowRefireMin) + getSpecialData().arrowRefireMin);
                    damage = Math.max( 0.1F, Math.min( 1.0F, damage ) );
                    attackEntityWithRangedAttack( (EntityLivingBase) target, damage );
                }
                if( sightDelay > 0 ) {
                    sightDelay--;
                }
                boolean shouldStop = sightDelay <= 0;
                if( stopped != shouldStop ) {
                    if( shouldStop ) {
                        attribute.applyModifier( Entity_SpecialPigZombie.stopModifier );
                    }
                    else {
                        attribute.removeModifier( Entity_SpecialPigZombie.stopModifier );
                    }
                }
            }
            else {
                sightDelay = 20;
            }
        }
        else {
            if( stopped ) {
                attribute.removeModifier( Entity_SpecialPigZombie.stopModifier );
            }
            super.attackEntity( target, distance );
        }
    }
    
    /// If this returns false, this mob will not shoot with a bow.
    public boolean willShootBow() {
        return true;
    }
    
    /// Attack the specified entity using a ranged attack.
    @Override
    public void attackEntityWithRangedAttack( EntityLivingBase target, float range ) {
        EntityArrow arrow = new EntityArrow( worldObj, this, target, 1.6F, getTypeArrowSpread() );
        arrow.setDamage( range * getSpecialData().arrowDamage + rand.nextGaussian() * 0.25 + worldObj.difficultySetting.getDifficultyId() * 0.11F );
        
        int power = EnchantmentHelper.getEnchantmentLevel( Enchantment.power.effectId, getHeldItem() );
        int punch = EnchantmentHelper.getEnchantmentLevel( Enchantment.punch.effectId, getHeldItem() );
        if( power > 0 ) {
            arrow.setDamage( arrow.getDamage() + power * 0.5 + 0.5 );
        }
        if( punch > 0 ) {
            arrow.setKnockbackStrength( punch );
        }
        if( EnchantmentHelper.getEnchantmentLevel( Enchantment.flame.effectId, getHeldItem() ) > 0 ) {
            arrow.setFire( 100 );
        }
        
        playSound( "random.bow", 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 0.8F) );
        worldObj.spawnEntityInWorld( arrow );
    }
    
    /// Overridden to modify the base arrow variance.
    protected float getTypeArrowSpread() {
        return getSpecialData().arrowSpread - worldObj.difficultySetting.getDifficultyId() * (getSpecialData().arrowSpread / 4.0F + 0.5F);
    }
    
    /// Called to attack the target.
    @Override
    public boolean attackEntityAsMob( Entity target ) {
        swingItem();
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
        else {
            isHostile = tag.getShort( "Anger" ) != 0;
        }
        
        getSpecialData().readFromNBT( tag );
        getSpecialData().readFromNBT( saveTag );
        isImmuneToFire = getSpecialData().isImmuneToFire;
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        if( getHeldItem() != null && getHeldItem().getItem() instanceof ItemBow ) {
            for( int i = rand.nextInt( 3 + looting ); i-- > 0; ) {
                dropItem( Items.arrow, 1 );
            }
        }
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