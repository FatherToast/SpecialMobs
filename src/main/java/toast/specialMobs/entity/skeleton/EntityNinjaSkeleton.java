package toast.specialMobs.entity.skeleton;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.DataWatcherHelper;
import toast.specialMobs.EffectHelper;
import toast.specialMobs.EnchantmentSpecial;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.EntityAINinja;
import toast.specialMobs.entity.INinja;

public class EntityNinjaSkeleton extends Entity_SpecialSkeleton implements INinja {
    
    // The data watcher key for whether this entity is frozen in place (hiding).
    public static final byte DW_FROZEN = DataWatcherHelper.instance.SKELETON_NINJA.nextKey();
    // The data watcher key for the block this is hiding as.
    public static final byte DW_HIDING_BLOCK = DataWatcherHelper.instance.SKELETON_NINJA.nextKey();
    // The data watcher key for the metadata of the block this is hiding as.
    public static final byte DW_HIDING_DATA = DataWatcherHelper.instance.SKELETON_NINJA.nextKey();
    
    // The entity state id for reveal particle effect.
    public static final byte HU_REVEAL_FX = 11;
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "skeleton/ninja.png" ),
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "skeleton/ninja_wither.png" )
    };
    
    public boolean canHide = true;
    
    public EntityNinjaSkeleton( World world ) {
        super( world );
        tasks.addTask( -1, new EntityAINinja( this ) );
        getSpecialData().setTextures( EntityNinjaSkeleton.TEXTURES );
        experienceValue += 2;
    }
    
    @Override
    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject( EntityNinjaSkeleton.DW_FROZEN, (byte) 0 );
        dataWatcher.addObject( EntityNinjaSkeleton.DW_HIDING_BLOCK, 0 );
        dataWatcher.addObject( EntityNinjaSkeleton.DW_HIDING_DATA, (byte) 0 );
    }
    
    // Gets whether the ninja should not move.
    @Override
    public boolean getFrozen() {
        return dataWatcher.getWatchableObjectByte( EntityNinjaSkeleton.DW_FROZEN ) != 0;
    }
    
    // Sets the ninja as an immovable object.
    @Override
    public void setFrozen( boolean frozen ) {
        if( frozen != getFrozen() ) {
            dataWatcher.updateObject( EntityNinjaSkeleton.DW_FROZEN, (byte) (frozen ? 1 : 0) );
            if( frozen ) {
                posX = Math.floor( posX ) + 0.5;
                posY = Math.floor( posY );
                posZ = Math.floor( posZ ) + 0.5;
            }
        }
    }
    
    // Gets the block being hidden as, or null if not hiding.
    @Override
    public Block getHidingBlock() {
        if( isEntityAlive() ) {
            int id = dataWatcher.getWatchableObjectInt( EntityNinjaSkeleton.DW_HIDING_BLOCK );
            if( id > 0 ) {
                Block block = Block.getBlockById( id );
                if( block != Blocks.air )
                    return block;
            }
        }
        return null;
    }
    
    // Gets the metadata of the block being hidden as, if any.
    @Override
    public int getHidingData() {
        return dataWatcher.getWatchableObjectByte( EntityNinjaSkeleton.DW_HIDING_DATA );
    }
    
    // Sets the block being hidden as, set to null or air to cancel hiding.
    @Override
    public void setHidingBlock( Block block, int data ) {
        int id;
        if( block == null ) {
            id = 0;
        }
        else {
            id = Block.getIdFromBlock( block );
        }
        if( id != dataWatcher.getWatchableObjectInt( EntityNinjaSkeleton.DW_HIDING_BLOCK ) ) {
            if( id == 0 ) {
                worldObj.playSoundAtEntity( this, "mob.zombie.infect", 1.0F, 0.5F );
                worldObj.setEntityState( this, EntityNinjaSkeleton.HU_REVEAL_FX );
            }
            dataWatcher.updateObject( EntityNinjaSkeleton.DW_HIDING_BLOCK, id );
        }
        if( data != getHidingData() ) {
            dataWatcher.updateObject( EntityNinjaSkeleton.DW_HIDING_DATA, (byte) data );
        }
        canHide = false;
    }
    
    /// Override to set the attack AI to use.
    @Override
    protected void initTypeAI() {
        setRangedAI( 1.0, 20, 30, 9.0F );
        setMeleeAI( 1.2 );
    }
    
    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        if( rand.nextBoolean() ) {
            ItemStack sword = new ItemStack( Items.iron_sword );
            try {
                EffectHelper.enchantItem( rand, sword, 30 );
            }
            catch( Exception ex ) {
                // noinspection all
                ex.printStackTrace();
            }
            EffectHelper.setItemName( sword, "Katana" );
            setCurrentItemOrArmor( 0, sword );
        }
        setCanPickUpLoot( true );
    }
    
    /// Called each tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        if( !worldObj.isRemote ) {
            if( canHide ) {
                setHiding();
            }
            else {
                if( onGround && (getEntityToAttack() == null || getEntityToAttack() instanceof EntityPlayer && ((EntityPlayer) getEntityToAttack()).capabilities.isCreativeMode) && getHidingBlock() == null ) {
                    canHide = true;
                }
            }
        }
        
        super.onLivingUpdate();
    }
    
    /// Overridden to modify attack effects.
    @Override
    protected void onTypeAttack( Entity target ) {
        setHidingBlock( null, 0 );
        setTarget( target );
    }
    
    /// Called when the entity is attacked.
    @Override
    public boolean attackEntityFrom( DamageSource damageSource, float damage ) {
        if( super.attackEntityFrom( damageSource, damage ) ) {
            setHidingBlock( null, 0 );
            return true;
        }
        return false;
    }
    
    /// Called when the entity is right-clicked by a player.
    @Override
    public boolean interact( EntityPlayer player ) {
        if( !worldObj.isRemote && getHidingBlock() != null ) {
            setHidingBlock( null, 0 );
            setTarget( player );
        }
        return super.interact( player );
    }
    
    /// Returns this entity's idle sound or null if it does not have one.
    @Override
    protected String getLivingSound() {
        return null;
    }
    
    /// Returns true if this entity makes footstep sounds and can trample crops.
    @Override
    protected boolean canTriggerWalking() {
        return false;
    }
    
    /// Moves this entity.
    @Override
    public void moveEntity( double x, double y, double z ) {
        if( !getFrozen() ) {
            super.moveEntity( x, y, z );
        }
        else {
            motionY = 0.0;
        }
    }
    
    /// Returns true if this entity should push and be pushed by other entities when colliding.
    @Override
    public boolean canBePushed() {
        return !getFrozen();
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        for( int i = rand.nextInt( 3 + looting ); i-- > 0; ) {
            dropItem( Items.paper, 1 );
        }
        for( int i = rand.nextInt( 2 + looting ); i-- > 0; ) {
            dropItem( Items.dye, 1 );
        }
    }
    
    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop( int superRare ) {
        ItemStack drop = new ItemStack( Items.iron_sword );
        try {
            EffectHelper.enchantItem( rand, drop, 30 );
            if( EnchantmentSpecial.painSword != null ) {
                EffectHelper.overrideEnchantment( drop, EnchantmentSpecial.painSword, 2 );
            }
            else {
                EffectHelper.overrideEnchantment( drop, Enchantment.sharpness, 2 );
            }
            EffectHelper.overrideEnchantment( drop, Enchantment.unbreaking, 5 );
        }
        catch( Exception ex ) {
            // noinspection all
            ex.printStackTrace();
        }
        EffectHelper.setItemName( drop, "Kusanagi-no-Tsurugi", 0xd );
        entityDropItem( drop, 0.0F );
    }
    
    @Override
    public void handleHealthUpdate( byte b ) {
        if( b == EntityNinjaSkeleton.HU_REVEAL_FX ) {
            spawnExplosionParticle();
        }
        else {
            super.handleHealthUpdate( b );
        }
    }
    
    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT( NBTTagCompound tag ) {
        super.writeEntityToNBT( tag );
        Block hidingBlock = getHidingBlock();
        if( hidingBlock != null ) {
            tag.setString( "HidingBlock", Block.blockRegistry.getNameForObject( hidingBlock ) );
            tag.setByte( "HidingData", (byte) (getHidingData() & 0xf) );
        }
        else {
            tag.setString( "HidingBlock", "\f" );
            tag.setByte( "HidingData", (byte) 0 );
        }
    }
    
    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT( NBTTagCompound tag ) {
        super.readEntityFromNBT( tag );
        if( tag.hasKey( "HidingBlock" ) ) {
            String hidingBlock = tag.getString( "HidingBlock" );
            if( hidingBlock.equals( "\f" ) ) {
                setHidingBlock( null, 0 );
            }
            else {
                setHidingBlock( Block.getBlockFromName( hidingBlock ), tag.getByte( "HidingData" ) );
            }
        }
    }
    
    /// Randomly picks a block to hide as.
    public void setHiding() {
        switch( rand.nextInt( 100 ) ) {
            case 0:
                setHidingBlock( Blocks.chest, rand.nextInt( 4 ) + 2 );
                return;
            case 1:
                setHidingBlock( Blocks.log, 0 );
                return;
            case 2:
                setHidingBlock( Blocks.sponge, 0 );
                return;
            case 3:
                setHidingBlock( Blocks.deadbush, 0 );
                return;
            case 4:
                setHidingBlock( Blocks.leaves, 0 );
                return;
        }
        
        Block hidingBlock;
        int hidingData;
        int x, y, z;
        
        x = (int) Math.floor( posX );
        y = (int) Math.floor( posY ) - 1;
        z = (int) Math.floor( posZ );
        hidingBlock = worldObj.getBlock( x, y, z );
        
        
        if( hidingBlock == Blocks.stone ) {
            switch( rand.nextInt( 32 ) ) {
                case 0:
                    setHidingBlock( hidingBlock, 0 );
                    return;
                case 1:
                    setHidingBlock( Blocks.gravel, 0 );
                    return;
                case 2:
                    setHidingBlock( Blocks.coal_ore, 0 );
                    return;
                case 3:
                    setHidingBlock( Blocks.iron_ore, 0 );
                    return;
                case 4:
                    setHidingBlock( Blocks.lapis_ore, 0 );
                    return;
                case 5:
                    setHidingBlock( Blocks.gold_ore, 0 );
                    return;
                case 6:
                    setHidingBlock( Blocks.redstone_ore, 0 );
                    return;
                case 7:
                    setHidingBlock( Blocks.diamond_ore, 0 );
                    return;
                case 8:
                    setHidingBlock( Blocks.emerald_ore, 0 );
                    return;
            }
        }
        else if( hidingBlock == Blocks.grass ) {
            switch( rand.nextInt( 10 ) ) {
                case 0:
                    setHidingBlock( hidingBlock, 0 );
                    return;
                case 1:
                    setHidingBlock( Blocks.log, rand.nextInt( 4 ) );
                    return;
                case 2:
                    setHidingBlock( Blocks.pumpkin, rand.nextInt( 4 ) );
                    return;
                case 3:
                    setHidingBlock( Blocks.melon_block, 0 );
                    return;
                case 4:
                    setHidingBlock( Blocks.tallgrass, rand.nextInt( 2 ) + 1 );
                    return;
                case 5:
                    setHidingBlock( Blocks.leaves, rand.nextInt( 4 ) );
                    return;
            }
        }
        else if( hidingBlock == Blocks.sand ) {
            switch( rand.nextInt( 6 ) ) {
                case 0:
                    setHidingBlock( hidingBlock, 0 );
                    return;
                case 1:
                    setHidingBlock( Blocks.cactus, 0 );
                    return;
                case 2:
                    setHidingBlock( Blocks.deadbush, 0 );
                    return;
            }
        }
        
        for( int i = 16; i-- > 0; ) {
            x = (int) Math.floor( posX ) + rand.nextInt( 17 ) - 8;
            y = (int) Math.floor( posY ) + rand.nextInt( 4 ) - 2;
            z = (int) Math.floor( posZ ) + rand.nextInt( 17 ) - 8;
            hidingBlock = worldObj.getBlock( x, y, z );
            hidingData = worldObj.getBlockMetadata( x, y, z );
            if( hidingBlock != Blocks.air && !(hidingBlock instanceof BlockLiquid) ) {
                setHidingBlock( hidingBlock, hidingData );
                return;
            }
        }
        
        setHidingBlock( Blocks.log, 0 );
    }
}