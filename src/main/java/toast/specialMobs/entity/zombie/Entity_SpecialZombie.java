package toast.specialMobs.entity.zombie;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.MobHelper;
import toast.specialMobs.Properties;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.ISpecialMob;
import toast.specialMobs.entity.SpecialMobData;

public class Entity_SpecialZombie extends EntityZombie implements ISpecialMob, IRangedAttackMob {
    
    /// Useful properties for this class.
    private static final double INFECT_CHANCE = Properties.getDouble( Properties.STATS, "villager_infection" );
    private static final double BOW_CHANCE = Properties.getDouble( Properties.STATS, "bow_chance_zombie" );
    
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( "textures/entity/zombie/zombie.png" ),
            new ResourceLocation( "textures/entity/zombie/zombie_villager.png" )
    };
    
    /// This entity's AI attack patterns.
    public EntityAIArrowAttack aiArrowAttack;
    public EntityAIAttackOnCollide[] aiAttackOnCollide;
    
    /// This mob's special mob data.
    private SpecialMobData specialData;
    
    public Entity_SpecialZombie( World world ) {
        super( world );
        MobHelper.clearMeleeAttackAI( this );
        if( world != null && !world.isRemote ) {
            setCombatTask();
        }
    }
    
    /// Used to initialize data watcher variables.
    @Override
    protected void entityInit() {
        specialData = new SpecialMobData( this, Entity_SpecialZombie.TEXTURES );
        super.entityInit();
        initTypeAI();
    }
    
    /// Override to set the attack AI to use.
    protected void initTypeAI() {
        setRangedAI( 0.9, 23, 70, 12.0F );
        setMeleeAI( 1.0 );
    }
    
    /// Helper methods to set the attack AI more easily.
    protected void loadRangedAI() {
        tasks.removeTask( aiArrowAttack );
        SpecialMobData data = getSpecialData();
        aiArrowAttack = new EntityAIArrowAttack( this, data.arrowMoveSpeed, data.arrowRefireMin, data.arrowRefireMax, data.arrowRange );
        setCombatTask();
    }
    
    protected void setRangedAI( double moveSpeed, int minDelay, int maxDelay, float range ) {
        SpecialMobData data = getSpecialData();
        data.arrowMoveSpeed = (float) moveSpeed;
        data.arrowRefireMin = (short) minDelay;
        data.arrowRefireMax = (short) maxDelay;
        data.arrowRange = range;
        aiArrowAttack = new EntityAIArrowAttack( this, data.arrowMoveSpeed, data.arrowRefireMin, data.arrowRefireMax, data.arrowRange );
    }
    
    protected void setMeleeAI( double moveSpeed ) {
        aiAttackOnCollide = new EntityAIAttackOnCollide[] {
                new EntityAIAttackOnCollide( this, EntityPlayer.class, moveSpeed, false ),
                new EntityAIAttackOnCollide( this, EntityVillager.class, moveSpeed, true )
        };
    }
    
    /// Returns this mob's special data.
    @Override // ISpecialMob
    public SpecialMobData getSpecialData() {
        return specialData;
    }
    
    /// Called to modify inherited attributes.
    @Override // ISpecialMob
    public void adjustEntityAttributes() {
        if( rand.nextDouble() < Entity_SpecialZombie.BOW_CHANCE ) {
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
        // Override to alter attributes.
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
    
    /// Called each tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        getSpecialData().onUpdate();
    }
    
    /// Called when this entity is first spawned to initialize it.
    @Override
    public IEntityLivingData onSpawnWithEgg( IEntityLivingData data ) {
        return getSpecialData().onSpawnWithEgg( data, new EntityZombie( worldObj ) );
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
    
    /// This method gets called when the entity kills another one.
    @Override
    public void onKillEntity( EntityLivingBase entity ) {
        if( !worldObj.isRemote && entity instanceof EntityVillager && rand.nextDouble() < Entity_SpecialZombie.INFECT_CHANCE ) {
            Entity_SpecialZombie zombie = null;
            try {
                zombie = getClass().getConstructor( new Class[] { World.class } ).newInstance( new Object[] { worldObj } );
            }
            catch( Exception ex ) {
                _SpecialMobs.debugException( "Error infecting villager! " + ex.getClass().getName() + " @" + getClass().getName() );
                return;
            }
            zombie.copyLocationAndAnglesFrom( entity );
            worldObj.spawnEntityInWorld( zombie );
            zombie.onSpawnWithEgg( (IEntityLivingData) null );
            zombie.setVillager( true );
            zombie.setChild( entity.isChild() );
            worldObj.playAuxSFXAtEntity( (EntityPlayer) null, 1016, (int) posX, (int) posY, (int) posZ, 0 );
            entity.setDead();
        }
    }
    
    /// Sets this entity's combat AI.
    public void setCombatTask() {
        tasks.removeTask( aiAttackOnCollide[0] );
        tasks.removeTask( aiAttackOnCollide[1] );
        tasks.removeTask( aiArrowAttack );
        
        ItemStack itemStack = getHeldItem();
        if( itemStack != null && itemStack.getItem() instanceof ItemBow ) {
            tasks.addTask( 2, aiArrowAttack );
        }
        else {
            tasks.addTask( 2, aiAttackOnCollide[0] );
            tasks.addTask( 3, aiAttackOnCollide[1] );
        }
    }
    
    /// Sets the held item, or an armor slot.
    @Override
    public void setCurrentItemOrArmor( int slot, ItemStack itemStack ) {
        super.setCurrentItemOrArmor( slot, itemStack );
        if( !worldObj.isRemote && slot == 0 ) {
            setCombatTask();
        }
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
        loadRangedAI();
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