package toast.specialMobs.entity.zombie;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import toast.specialMobs.DataWatcherHelper;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.EntitySpecialFishHook;
import toast.specialMobs.entity.IAngler;

public class EntityFishingZombie extends Entity_SpecialZombie implements IAngler {
    
    /// The position of hasFishingRod within the data watcher.
    private static final byte DW_FISHING_ROD = DataWatcherHelper.instance.ZOMBIE_FISHING.nextKey();
    
    /// Ticks until this fishing zombie can cast its lure again.
    public int rodTime = 0;
    /// This fishing zombie's lure entity.
    private EntitySpecialFishHook fishHook = null;
    
    public EntityFishingZombie( World world ) {
        super( world );
        experienceValue += 2;
    }
    
    /// Used to initialize dataWatcher variables.
    @Override
    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject( EntityFishingZombie.DW_FISHING_ROD, (byte) 1 );
    }
    
    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        getSpecialData().multAttribute( SharedMonsterAttributes.movementSpeed, 0.9 );
        setCanPickUpLoot( false );
        
        ItemStack itemStack = new ItemStack( Items.fishing_rod );
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
    
    /// Set/get functions for this angler's fishHook.
    @Override
    /// IAngler
    public void setFishHook( EntitySpecialFishHook hook ) {
        fishHook = hook;
        setFishingRod( hook == null );
    }
    
    @Override
    /// IAngler
    public EntitySpecialFishHook getFishHook() {
        return fishHook;
    }
    
    /// Get/set functions for heldItem. fishing rod == true, stick == false.
    public void setFishingRod( boolean rod ) {
        dataWatcher.updateObject( EntityFishingZombie.DW_FISHING_ROD, (byte) (rod ? 1 : 0) );
    }
    
    public boolean getFishingRod() {
        return dataWatcher.getWatchableObjectByte( EntityFishingZombie.DW_FISHING_ROD ) == 1;
    }
    
    /// Returns the heldItem.
    @Override
    public ItemStack getHeldItem() {
        ItemStack held = super.getHeldItem();
        if( worldObj.isRemote && held != null && held.getItem() instanceof ItemFishingRod && !getFishingRod() )
            return new ItemStack( Items.stick );
        return held;
    }
    
    /// Called every tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        if( rodTime > 0 ) {
            rodTime--;
        }
        if( !worldObj.isRemote && rodTime <= 0 && getFishingRod() ) {
            Entity target = getAttackTarget();
            if( target != null ) {
                float distanceSq = (float) getDistanceSqToEntity( target );
                if( distanceSq > 9.0F && distanceSq < 100.0F && getEntitySenses().canSee( target ) ) {
                    new EntitySpecialFishHook( worldObj, this, target );
                    worldObj.spawnEntityInWorld( getFishHook() );
                    worldObj.playSoundAtEntity( this, "random.bow", 0.5F, 0.4F / (rand.nextFloat() * 0.4F + 0.8F) );
                    setFishingRod( false );
                    rodTime = rand.nextInt( 11 ) + 32;
                }
            }
        }
        super.onLivingUpdate();
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        
        for( int i = rand.nextInt( 2 + looting ); i-- > 0; ) {
            dropItem( Items.fish, 1 );
        }
    }
}