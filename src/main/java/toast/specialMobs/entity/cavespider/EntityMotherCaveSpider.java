package toast.specialMobs.entity.cavespider;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.SpecialMobData;

public class EntityMotherCaveSpider extends Entity_SpecialCaveSpider {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "cavespider/mother.png" ),
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "cavespider/mother_eyes.png" )
    };
    
    /// The number of babies spawned on death.
    private byte babies;
    
    public EntityMotherCaveSpider( World world ) {
        super( world );
        getSpecialData().setTextures( EntityMotherCaveSpider.TEXTURES );
        experienceValue += 2;
        babies = (byte) (3 + rand.nextInt( 4 ));
    }
    
    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        getSpecialData().addAttribute( SharedMonsterAttributes.maxHealth, 16.0 );
        getSpecialData().addAttribute( SharedMonsterAttributes.attackDamage, 3.0 );
        getSpecialData().setHealTime( 20 );
        getSpecialData().armor += 6;
        getSpecialData().arrowDamage += 1.5F;
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        if( hit && (rand.nextInt( 3 ) == 0 || rand.nextInt( 1 + looting ) > 0) ) {
            entityDropItem( new ItemStack( Items.spawn_egg, 1, EntityList.getEntityID( new EntityCaveSpider( worldObj ) ) ), 0.0F );
        }
        
        if( !worldObj.isRemote ) {
            EntityBabyCaveSpider baby = null;
            for( int i = babies; i-- > 0; ) {
                baby = new EntityBabyCaveSpider( worldObj );
                baby.copyLocationAndAnglesFrom( this );
                baby.setTarget( getEntityToAttack() );
                baby.onSpawnWithEgg( null );
                worldObj.spawnEntityInWorld( baby );
            }
            if( baby != null ) {
                worldObj.playSoundAtEntity( baby, "random.pop", 1.0F, 2.0F / (rand.nextFloat() * 0.4F + 0.8F) );
                baby.spawnExplosionParticle();
            }
        }
    }
    
    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop( int superRare ) {
        ItemStack itemStack;
        String name;
        if( rand.nextBoolean() ) {
            Item[] armor = {
                    Items.chainmail_helmet, Items.chainmail_chestplate, Items.chainmail_leggings, Items.chainmail_boots
            };
            String[] armorNames = {
                    "Helmet", "Chestplate", "Leggings", "Boots"
            };
            int choice = rand.nextInt( armor.length );
            itemStack = new ItemStack( armor[choice] );
            name = armorNames[choice];
        }
        else {
            Item[] tools = {
                    Items.stone_sword, Items.bow, Items.stone_pickaxe, Items.stone_axe, Items.stone_shovel
            };
            String[] toolNames = {
                    "Sword", "Bow", "Pickaxe", "Axe", "Shovel"
            };
            int choice = rand.nextInt( tools.length );
            itemStack = new ItemStack( tools[choice] );
            name = toolNames[choice];
        }
        
        int maxDamage = Math.max( itemStack.getMaxDamage() - 25, 1 );
        int damage = itemStack.getMaxDamage() - rand.nextInt( rand.nextInt( maxDamage ) + 1 );
        if( damage > maxDamage ) {
            damage = maxDamage;
        }
        else if( damage < 1 ) {
            damage = 1;
        }
        itemStack.setItemDamage( damage );
        
        EffectHelper.setItemName( itemStack, "Partially Digested " + name, 0xa );
        EffectHelper.addItemText( itemStack, "§7§oIt's a bit slimy..." );
        EffectHelper.enchantItem( rand, itemStack, 30 );
        EffectHelper.overrideEnchantment( itemStack, Enchantment.unbreaking, 10 );
        
        entityDropItem( itemStack, 0.0F );
    }
    
    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT( NBTTagCompound tag ) {
        super.writeEntityToNBT( tag );
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
        saveTag.setByte( "Babies", babies );
    }
    
    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT( NBTTagCompound tag ) {
        super.readEntityFromNBT( tag );
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
        if( saveTag.hasKey( "Babies" ) ) {
            babies = saveTag.getByte( "Babies" );
        }
        else if( tag.hasKey( "Babies" ) ) {
            babies = tag.getByte( "Babies" );
        }
    }
}