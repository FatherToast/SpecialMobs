package toast.specialMobs.entity.witch;

import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs.MobHelper;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.ISpecialMob;
import toast.specialMobs.entity.SpecialMobData;

import java.util.List;
import java.util.UUID;

public class Entity_SpecialWitch extends EntityWitch implements ISpecialMob {
    
    // Drinking potion speed penalty modifier.
    private static final UUID drinkingSpeedPenaltyUUID = UUID.fromString( "5CD17E52-A79A-43D3-A529-90FDE04B181E" );
    private static final AttributeModifier drinkingSpeedPenaltyModifier = new AttributeModifier( Entity_SpecialWitch.drinkingSpeedPenaltyUUID, "Drinking speed penalty", -0.25, 0 ).setSaved( false );
    
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( "textures/entity/witch.png" )
    };
    
    /// Used to trick the witch into thinking it has no potion to drink.
    public boolean fakeNoItem;
    /// True if drinking a potion.
    public boolean drinkingPotion;
    /// Set to max item use duration when drinking starts. When it counts down to 0, drink the held potion.
    public int potionDrinkDelay;
    /// Number of ticks before another potion can be thrown on self.
    public int potionThrowDelay;
    
    /// True if this witch is using melee ai.
    private boolean hasMeleeAI;
    
    /// When the witch is drinking a potion, it stores its actual held item here.
    public ItemStack sheathedItem;
    
    /// This mob's special mob data.
    private SpecialMobData specialData;
    
    public Entity_SpecialWitch( World world ) {
        super( world );
        getSpecialData().immuneToPotions.add( Potion.poison.id );
        getSpecialData().immuneToPotions.add( Potion.weakness.id );
        
        MobHelper.clearRangedAttackAI( this );
        initTypeAI();
    }
    
    /// Used to initialize data watcher variables.
    @Override
    protected void entityInit() {
        specialData = new SpecialMobData( this, Entity_SpecialWitch.TEXTURES );
        super.entityInit();
    }
    
    /// Override to set the attack AI to use.
    protected void initTypeAI() {
        setRangedAI( 60, 60, 10.0F );
    }
    
    /// Helper methods to set the attack AI more easily.
    protected void loadRangedAI() {
        if( !hasMeleeAI ) {
            MobHelper.clearRangedAttackAI( this );
            SpecialMobData data = getSpecialData();
            tasks.addTask( 2, new EntityAIArrowAttack( this, data.arrowMoveSpeed, data.arrowRefireMin, data.arrowRefireMax, data.arrowRange ) );
        }
    }
    
    protected void setRangedAI( int minDelay, int maxDelay, float range ) {
        hasMeleeAI = false;
        SpecialMobData data = getSpecialData();
        data.arrowMoveSpeed = 1.0F;
        data.arrowRefireMin = (short) minDelay;
        data.arrowRefireMax = (short) maxDelay;
        data.arrowRange = range;
        tasks.addTask( 2, new EntityAIArrowAttack( this, data.arrowMoveSpeed, data.arrowRefireMin, data.arrowRefireMax, data.arrowRange ) );
    }
    
    protected void setMeleeAI() {
        hasMeleeAI = true;
        tasks.addTask( 2, new EntityAIAttackOnCollide( this, EntityPlayer.class, 1.0, false ) );
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
    
    /// Called when the witch is looking for a potion to drink.
    public void tryDrinkPotion() {
        if( potionThrowDelay <= 0 ) {
            if( isBurning() && !isPotionActive( Potion.fireResistance ) ) {
                drinkPotion( 8195 ); // Fire Resistance
            }
            else if( rand.nextFloat() < 0.15F && isInsideOfMaterial( Material.water ) && !isPotionActive( Potion.waterBreathing ) ) {
                drinkPotion( 8205 ); // Water Breathing
            }
            else if( rand.nextFloat() < 0.05F && getHealth() < getMaxHealth() ) {
                drinkPotion( 8197 ); // Instant Health
            }
            else if( rand.nextFloat() < 0.2F && getAttackTarget() != null && !isPotionActive( Potion.moveSpeed ) && getAttackTarget().getDistanceSqToEntity( this ) > 121.0 ) {
                drinkPotion( 16386 ); // Splash Swiftness
            }
            else {
                tryDrinkPotionByType();
            }
        }
    }
    
    /// Overridden to modify potion drinking ai.
    protected void tryDrinkPotionByType() {
        /// Override to add more potion drinking ais.
    }
    
    /// Starts drinking a potion. If the potion is a splash potion, throw it straight down.
    public void drinkPotion( int damage ) {
        drinkPotion( new ItemStack( Items.potionitem, 1, damage ) );
    }
    
    public void drinkPotion( int damage, Potion potionType, int duration, int amplifier ) {
        ItemStack potion = new ItemStack( Items.potionitem, 1, damage );
        EffectHelper.addPotionEffect( potion, potionType, duration, amplifier );
        drinkPotion( potion );
    }
    
    public void drinkPotion( ItemStack potion ) {
        if( potion == null ) {
            // Cancel drinking the current potion and re-equip the sheathed item
            potionDrinkDelay = 0;
            
            if( drinkingPotion ) {
                drinkingPotion = false;
                
                setCurrentItemOrArmor( 0, sheathedItem );
                sheathedItem = null;
                getEntityAttribute( SharedMonsterAttributes.movementSpeed ).removeModifier( Entity_SpecialWitch.drinkingSpeedPenaltyModifier );
            }
        }
        else if( ItemPotion.isSplash( potion.getItemDamage() ) ) {
            // It is a splash potion, throw it straight down to "drink"
            potionThrowDelay = 32;
            
            EntityPotion thrownPotion = new EntityPotion( worldObj, this, potion );
            thrownPotion.rotationPitch += 20.0F;
            thrownPotion.motionX = thrownPotion.motionZ = 0.0;
            thrownPotion.motionY = -0.2;
            worldObj.spawnEntityInWorld( thrownPotion );
        }
        else {
            // Start drinking normal potion
            drinkingPotion = true;
            potionDrinkDelay = potion.getMaxItemUseDuration();
            
            sheathedItem = getHeldItem();
            setCurrentItemOrArmor( 0, potion );
            IAttributeInstance attribute = getEntityAttribute( SharedMonsterAttributes.movementSpeed );
            attribute.removeModifier( Entity_SpecialWitch.drinkingSpeedPenaltyModifier );
            attribute.applyModifier( Entity_SpecialWitch.drinkingSpeedPenaltyModifier );
        }
    }
    
    /// Called each tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        fakeNoItem = true;
        super.onLivingUpdate();
        getSpecialData().onUpdate();
        
        if( !worldObj.isRemote && isEntityAlive() ) {
            potionThrowDelay--;
            
            if( drinkingPotion ) {
                if( potionDrinkDelay < 25 && potionDrinkDelay % 4 == 0 ) {
                    playSound( "random.drink", 0.2F, worldObj.rand.nextFloat() * 0.1F + 0.9F );
                }
                
                if( potionDrinkDelay-- <= 0 ) {
                    ItemStack potion = getHeldItem();
                    drinkPotion( null );
                    
                    if( potion != null && potion.getItem() instanceof ItemPotion ) {
                        // noinspection unchecked
                        List<? extends PotionEffect> list = ((ItemPotion) potion.getItem()).getEffects( potion );
                        
                        if( list != null ) {
                            for( PotionEffect effect : list ) {
                                addPotionEffect( new PotionEffect( effect ) );
                            }
                        }
                    }
                }
            }
            else {
                tryDrinkPotion();
            }
        }
    }
    
    /// Set whether this witch is drinking a potion.
    @Override
    public void setAggressive( boolean value ) {
        // Do nothing
    }
    
    /// Return whether this witch is drinking a potion.
    @Override
    public boolean getAggressive() {
        return true;
    }
    
    @Override
    public ItemStack getHeldItem() {
        if( !worldObj.isRemote && fakeNoItem )
            return null;
        return super.getHeldItem();
    }
    
    @Override
    public void setCurrentItemOrArmor( int slot, ItemStack equipment ) {
        if( !worldObj.isRemote && fakeNoItem ) {
            fakeNoItem = false;
        }
        else {
            super.setCurrentItemOrArmor( slot, equipment );
        }
    }
    
    /// Called when this entity is first spawned to initialize it.
    @Override
    public IEntityLivingData onSpawnWithEgg( IEntityLivingData data ) {
        return getSpecialData().onSpawnWithEgg( data, new EntityWitch( worldObj ) );
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
    
    /// Attack the specified entity using a ranged attack.
    @Override
    public void attackEntityWithRangedAttack( EntityLivingBase target, float range ) {
        if( !drinkingPotion ) {
            EntityPotion thrownPotion = new EntityPotion( worldObj, this, 0 );
            double dX = target.posX + target.motionX - posX;
            double dY = target.posY + target.getEyeHeight() - 1.1 - posY;
            double dZ = target.posZ + target.motionZ - posZ;
            float distance = MathHelper.sqrt_double( dX * dX + dZ * dZ );
            
            thrownPotion = adjustSplashPotion( thrownPotion, target, range, distance );
            
            thrownPotion.rotationPitch += 20.0F;
            thrownPotion.setThrowableHeading( dX, dY + distance * 0.2F, dZ, 0.75F, 8.0F );
            worldObj.spawnEntityInWorld( thrownPotion );
        }
    }
    
    /// Changes the default splash potion into another befitting the situation.
    protected EntityPotion adjustSplashPotion( EntityPotion thrownPotion, EntityLivingBase target, float range, float distance ) {
        if( adjustSplashPotionByType( thrownPotion, target, range, distance ) )
            return thrownPotion;
        if( distance >= 8.0F && !target.isPotionActive( Potion.moveSlowdown ) ) {
            thrownPotion.setPotionDamage( 16394 ); // Splash Slowness
            return thrownPotion;
        }
        if( target.getHealth() >= 8.0F && !target.isPotionActive( Potion.poison ) ) {
            thrownPotion.setPotionDamage( 16388 ); // Splash Poison
            return thrownPotion;
        }
        if( distance <= 3.0F && !target.isPotionActive( Potion.weakness ) && rand.nextFloat() < 0.25F ) {
            thrownPotion.setPotionDamage( 16392 ); // Splash Weakness
            return thrownPotion;
        }
        return thrownPotion;
    }
    
    /// Overridden to modify potion attacks. Returns true if the potion was modified.
    protected boolean adjustSplashPotionByType( EntityPotion thrownPotion, EntityLivingBase target, float range, float distance ) {
        if( target.getCreatureAttribute() == EnumCreatureAttribute.UNDEAD ) {
            // The default potion vs. undead
            thrownPotion.setPotionDamage( 16389 ); // Splash Instant Health
        }
        else {
            // The default potion vs. living
            thrownPotion.setPotionDamage( 16396 ); // Splash Instant Damage
        }
        return false;
    }
    
    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT( NBTTagCompound tag ) {
        super.writeEntityToNBT( tag );
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
        getSpecialData().isImmuneToFire = isImmuneToFire;
        getSpecialData().writeToNBT( saveTag );
        
        tag.setBoolean( "DrinkingPotion", drinkingPotion );
        NBTTagCompound sheathedTag = new NBTTagCompound();
        if( sheathedItem != null ) {
            sheathedItem.writeToNBT( sheathedTag );
        }
        tag.setTag( "SheathedItem", sheathedTag );
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
        
        drinkingPotion = tag.getBoolean( "DrinkingPotion" );
        sheathedItem = ItemStack.loadItemStackFromNBT( tag.getCompoundTag( "SheathedItem" ) );
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        if( drinkingPotion ) {
            ItemStack potion = getHeldItem();
            drinkPotion( null );
            if( potion != null && rand.nextFloat() < 0.15F ) {
                entityDropItem( potion, 0.0F );
            }
        }
        
        super.dropFewItems( hit, looting );
        if( _SpecialMobs.debug ) {
            dropRareDrop( Math.max( 0, rand.nextInt( 5 ) - 3 ) );
        }
    }
    
    @Override
    protected String getLivingSound() {
        return _SpecialMobs.NAMESPACE + super.getLivingSound();
    }
    
    @Override
    protected String getHurtSound() {
        return _SpecialMobs.NAMESPACE + super.getHurtSound();
    }
    
    @Override
    protected String getDeathSound() {
        return _SpecialMobs.NAMESPACE + super.getDeathSound();
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