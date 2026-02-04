package toast.specialMobs.entity.enderman;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.Properties;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.ISpecialMob;
import toast.specialMobs.entity.SpecialMobData;

public class Entity_SpecialEnderman extends EntityEnderman implements ISpecialMob {
    
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( "textures/entity/enderman/enderman.png" ),
            new ResourceLocation( "textures/entity/enderman/enderman_eyes.png" )
    };
    
    static {
        if( !Properties.getBoolean( Properties.STATS, "enderman_griefing" ) ) {
            for( Object key : Block.blockRegistry.getKeys() ) {
                EntityEnderman.setCarriable( Block.getBlockFromName( (String) key ), false );
            }
        }
    }
    
    /// This mob's special mob data.
    private SpecialMobData specialData;
    
    public Entity_SpecialEnderman( World world ) {
        super( world );
    }
    
    /// Used to initialize data watcher variables.
    @Override
    protected void entityInit() {
        specialData = new SpecialMobData( this, Entity_SpecialEnderman.TEXTURES );
        super.entityInit();
    }
    
    /// Returns this mob's special data.
    @Override // ISpecialMob
    public SpecialMobData getSpecialData() {
        return specialData;
    }
    
    /// Called to modify inherited attributes.
    @Override // ISpecialMob
    public void adjustEntityAttributes() {
        float prevMax = getMaxHealth();
        adjustTypeAttributes();
        setHealth( getMaxHealth() + getHealth() - prevMax );
    }
    
    /// Overridden to modify inherited attribites.
    protected void adjustTypeAttributes() {
        // Override to alter attributes.
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
        return getSpecialData().onSpawnWithEgg( data, new EntityEnderman( worldObj ) );
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
        // Override to alter attack.
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
    
    /// Called to remove this entity from the world.
    @Override
    public void setDead() {
        if( !worldObj.isRemote ) {
            Block carried = func_146080_bZ();
            if( carried != null ) {
                carried.dropBlockAsItem( worldObj, (int) Math.floor( posX ), (int) Math.floor( posY ), (int) Math.floor( posZ ), getCarryingData(), 0 );
                func_146081_a( Blocks.air );
            }
        }
        super.setDead();
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