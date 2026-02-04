package toast.specialMobs.entity.spider;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs.MobHelper;
import toast.specialMobs._SpecialMobs;

import java.util.ArrayList;

public class EntityHungrySpider extends Entity_SpecialSpider {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "spider/hungry.png" ),
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "spider/hungry_eyes.png" )
    };
    
    /// The feeding level of this hungry spider.
    private byte feedingLevel;
    /// The amount of times this hungry spider has gained health.
    private int gainedHealth;
    /// The items this spider has eaten.
    private final ArrayList<ItemStack> stomach = new ArrayList<ItemStack>();
    
    public EntityHungrySpider( World world ) {
        super( world );
        getSpecialData().setTextures( EntityHungrySpider.TEXTURES );
        getSpecialData().resetRenderScale( 0.8F );
        experienceValue += 4;
    }
    
    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        getSpecialData().addAttribute( SharedMonsterAttributes.maxHealth, 4.0 );
        getSpecialData().addAttribute( SharedMonsterAttributes.attackDamage, -1.0 );
        getSpecialData().setHealTime( 40 );
        getSpecialData().arrowRange = 0.0F;
        setCanPickUpLoot( true );
    }
    
    /// Overridden to modify attack effects.
    @Override
    public void onTypeAttack( Entity target ) {
        if( target instanceof EntityPlayer ) {
            ItemStack drop = MobHelper.removeRandomItem( (EntityPlayer) target );
            if( drop != null ) {
                if( canPickUpLoot() ) {
                    setCurrentItemOrArmor( 0, drop );
                }
                else {
                    entityDropItem( drop, 0.0F );
                    worldObj.playSoundAtEntity( this, "random.burp", 0.5F, rand.nextFloat() * 0.1F + 0.9F );
                }
            }
        }
    }
    
    /// Sets the held item, or an armor slot.
    @Override
    public void setCurrentItemOrArmor( int slot, ItemStack itemStack ) {
        if( worldObj.isRemote )
            return;
        if( itemStack != null && gainedHealth < 64 ) {
            gainedHealth++;
            float maxHealth = getMaxHealth();
            getSpecialData().addAttribute( SharedMonsterAttributes.maxHealth, 4.0 );
            setHealth( getHealth() + getMaxHealth() - maxHealth );
            if( feedingLevel < 7 ) {
                getSpecialData().addAttribute( SharedMonsterAttributes.attackDamage, 1.0 );
                getSpecialData().arrowDamage += 0.5F;
            }
            setFeedingLevel( feedingLevel + 1, true );
        }
        if( gainedHealth >= 64 ) {
            setCanPickUpLoot( false );
        }
        
        if( itemStack != null ) {
            if( itemStack.getItem() instanceof ItemFood ) {
                heal( ((ItemFood) itemStack.getItem()).func_150905_g( itemStack ) );
            }
            else {
                stomach.add( itemStack );
            }
            worldObj.playSoundAtEntity( this, "random.burp", 0.5F, rand.nextFloat() * 0.1F + 0.9F );
        }
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        if( hit && (rand.nextInt( 3 ) == 0 || rand.nextInt( 1 + looting ) > 0) ) {
            dropItem( Items.apple, 1 );
        }
        
        for( ItemStack itemStack : stomach ) {
            entityDropItem( itemStack, 0.0F );
        }
        stomach.clear();
    }
    
    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop( int superRare ) {
        ItemStack itemStack = new ItemStack( Items.potionitem, 1, 8196 );
        EffectHelper.setItemName( itemStack, "Potion of Hunger", 0xf );
        EffectHelper.addPotionEffect( itemStack, Potion.damageBoost, 1200, 0 );
        EffectHelper.addPotionEffect( itemStack, Potion.regeneration, 1200, 0 );
        EffectHelper.addPotionEffect( itemStack, Potion.hunger, 600, 1 );
        entityDropItem( itemStack, 0.0F );
    }
    
    /// Sets the feeding level of this hungry spider.
    private void setFeedingLevel( int level, boolean updateScale ) {
        if( level < 0 ) {
            level = 0;
        }
        else if( level > 7 ) {
            level = 7;
        }
        int diff = level - feedingLevel;
        if( diff != 0 ) {
            feedingLevel = (byte) level;
            setSize( 1.0F + 0.12857F * level, 0.8F + 0.07142F * level );
            if( updateScale ) {
                getSpecialData().setRenderScale( getSpecialData().getRenderScale() + 0.1F * diff );
            }
        }
    }
    
    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT( NBTTagCompound tag ) {
        super.writeEntityToNBT( tag );
        tag.setByte( "FeedLevel", feedingLevel );
        tag.setByte( "GrowCount", (byte) gainedHealth );
        
        NBTTagList tagList = new NBTTagList();
        NBTTagCompound tagItem;
        for( ItemStack itemStack : stomach ) {
            tagItem = new NBTTagCompound();
            itemStack.writeToNBT( tagItem );
            tagList.appendTag( tagItem );
        }
        tag.setTag( "Stomach", tagList );
    }
    
    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT( NBTTagCompound tag ) {
        super.readEntityFromNBT( tag );
        if( tag.hasKey( "FeedLevel" ) ) {
            setFeedingLevel( tag.getByte( "FeedLevel" ), false );
        }
        if( tag.hasKey( "GrowCount" ) ) {
            gainedHealth = tag.getByte( "GrowCount" ) & 0xff;
        }
        
        if( tag.hasKey( "Stomach" ) ) {
            NBTTagList tagList = tag.getTagList( "Stomach", new NBTTagCompound().getId() );
            stomach.clear();
            for( int i = 0; i < tagList.tagCount(); i++ ) {
                stomach.add( ItemStack.loadItemStackFromNBT( tagList.getCompoundTagAt( i ) ) );
            }
        }
    }
}