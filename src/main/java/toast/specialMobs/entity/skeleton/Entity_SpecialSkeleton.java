package toast.specialMobs.entity.skeleton;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.DataWatcherHelper;
import toast.specialMobs.Properties;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.ISpecialMob;
import toast.specialMobs.entity.SpecialMobData;

import java.util.UUID;

public class Entity_SpecialSkeleton extends EntitySkeleton implements ISpecialMob {
    
    /// Useful properties for this class.
    private static final double BABY_CHANCE = Properties.getDouble( Properties.STATS, "baby_skeleton_chance" );
    private static final double BOW_CHANCE = Properties.getDouble( Properties.STATS, "bow_chance_skeleton" );
    private static final double BOW_CHANCE_WITHER = Properties.getDouble( Properties.STATS, "bow_chance_wither" );
    /// Baby speed boost modifier.
    private static final UUID babySpeedBoostUUID = UUID.fromString( "B9766B59-9566-4402-BC1F-2EE2A276D836" );
    private static final AttributeModifier babySpeedBoostModifier = new AttributeModifier( Entity_SpecialSkeleton.babySpeedBoostUUID, "Baby speed boost", 0.5, 1 );
    /// The position of isBaby within the data watcher.
    private static final byte DW_IS_BABY = DataWatcherHelper.instance.SKELETON.nextKey();
    
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( "textures/entity/skeleton/skeleton.png" ),
            new ResourceLocation( "textures/entity/skeleton/wither_skeleton.png" )
    };
    
    /// Adult width and height.
    private float adultWidth = -1.0F;
    private float adultHeight;
    
    /// This entity's AI attack patterns.
    public EntityAIArrowAttack aiArrowAttack;
    public EntityAIAttackOnCollide aiAttackOnCollide;
    
    /// This mob's special mob data.
    private SpecialMobData specialData;
    
    public Entity_SpecialSkeleton( World world ) {
        super( world );
    }
    
    /// Used to initialize data watcher variables.
    @Override
    protected void entityInit() {
        specialData = new SpecialMobData( this, Entity_SpecialSkeleton.TEXTURES );
        super.entityInit();
        dataWatcher.addObject( Entity_SpecialSkeleton.DW_IS_BABY, (byte) 0 );
        
        initTypeAI();
    }
    
    /// Override to set the attack AI to use.
    protected void initTypeAI() {
        setRangedAI( 1.0, 20, 60, 15.0F );
        setMeleeAI( 1.2 );
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
        aiAttackOnCollide = new EntityAIAttackOnCollide( this, EntityPlayer.class, moveSpeed, false );
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
        if( rand.nextDouble() < Entity_SpecialSkeleton.BABY_CHANCE ) {
            setChild( true );
        }
        if( getSkeletonType() == 1 ) {
            isImmuneToFire = true;
            if( rand.nextDouble() < Entity_SpecialSkeleton.BOW_CHANCE_WITHER ) {
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
        }
        else {
            if( rand.nextDouble() >= Entity_SpecialSkeleton.BOW_CHANCE ) {
                ItemStack itemStack = new ItemStack( Items.stone_sword );
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
        }
        
        float prevMax = getMaxHealth();
        adjustTypeAttributes();
        setHealth( getMaxHealth() + getHealth() - prevMax );
    }
    
    /// Overridden to modify inherited attribites.
    protected void adjustTypeAttributes() {
        /// Override to alter attributes.
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
        if( EnchantmentHelper.getEnchantmentLevel( Enchantment.flame.effectId, getHeldItem() ) > 0 || getSkeletonType() == 1 ) {
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
        return getSpecialData().onSpawnWithEgg( data, new EntitySkeleton( worldObj ) );
    }
    
    /// If true, this entity is a baby.
    @Override
    public boolean isChild() {
        return getChild();
    }
    
    /// Returns true if this entity is a baby.
    public boolean getChild() {
        return dataWatcher.getWatchableObjectByte( Entity_SpecialSkeleton.DW_IS_BABY ) == 1;
    }
    
    /// Sets this mob as a baby.
    public void setChild( boolean value ) {
        dataWatcher.updateObject( Entity_SpecialSkeleton.DW_IS_BABY, (byte) (value ? 1 : 0) );
        
        if( worldObj != null && !worldObj.isRemote ) {
            IAttributeInstance attribute = getEntityAttribute( SharedMonsterAttributes.movementSpeed );
            attribute.removeModifier( Entity_SpecialSkeleton.babySpeedBoostModifier );
            if( value ) {
                attribute.applyModifier( Entity_SpecialSkeleton.babySpeedBoostModifier );
            }
        }
        updateScale( value );
    }
    
    /// Sets the width and height of the entity.
    @Override
    protected void setSize( float width, float height ) {
        boolean alreadyScaled = adultWidth > 0.0F && adultHeight > 0.0F;
        adultWidth = width;
        adultHeight = height;
        
        if( !alreadyScaled ) {
            updateScale();
        }
    }
    
    /// Updates the entity size scaled by the normal adult size.
    protected void updateScale() {
        updateScale( isChild() );
    }
    
    protected void updateScale( boolean isChild ) {
        float scale = !worldObj.isRemote && isChild() ? 0.5F : 1.0F;
        super.setSize( adultWidth * scale, adultHeight * scale );
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
    
    /// Sets this entity's combat AI.
    @Override
    public void setCombatTask() {
        tasks.removeTask( aiAttackOnCollide );
        tasks.removeTask( aiArrowAttack );
        
        ItemStack itemStack = getHeldItem();
        if( itemStack != null && itemStack.getItem() instanceof ItemBow ) {
            tasks.addTask( 4, aiArrowAttack );
        }
        else {
            tasks.addTask( 4, aiAttackOnCollide );
        }
    }
    
    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT( NBTTagCompound tag ) {
        super.writeEntityToNBT( tag );
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
        saveTag.setBoolean( "IsBaby", getChild() );
        
        getSpecialData().isImmuneToFire = isImmuneToFire;
        getSpecialData().writeToNBT( saveTag );
    }
    
    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT( NBTTagCompound tag ) {
        super.readEntityFromNBT( tag );
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
        if( saveTag.hasKey( "IsBaby" ) ) {
            setChild( saveTag.getBoolean( "IsBaby" ) );
        }
        else if( tag.hasKey( "IsBaby" ) ) {
            setChild( tag.getBoolean( "IsBaby" ) );
        }
        
        getSpecialData().readFromNBT( tag );
        getSpecialData().readFromNBT( saveTag );
        isImmuneToFire = getSpecialData().isImmuneToFire;
        loadRangedAI();
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        if( _SpecialMobs.debug ) {
            dropRareDrop( Math.max( 0, rand.nextInt( 5 ) - 3 ) );
        }
    }
    
    /// Set this skeleton's type.
    @Override
    public void setSkeletonType( int type ) {
        super.setSkeletonType( type );
        isImmuneToFire = getSpecialData().isImmuneToFire;
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