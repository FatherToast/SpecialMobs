package toast.specialMobs.entity.skeleton;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs.MobHelper;
import toast.specialMobs._SpecialMobs;

public class EntityThiefSkeleton extends Entity_SpecialSkeleton {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "skeleton/thief.png" ),
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "skeleton/thief_wither.png" )
    };
    
    public EntityThiefSkeleton( World world ) {
        super( world );
        getSpecialData().setTextures( EntityThiefSkeleton.TEXTURES );
        experienceValue += 2;
    }
    
    /// Override to set the attack AI to use.
    @Override
    protected void initTypeAI() {
        setRangedAI( 1.1, 20, 30, 9.0F );
        setMeleeAI( 1.2 );
    }
    
    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        getSpecialData().multAttribute( SharedMonsterAttributes.movementSpeed, 1.3 );
        setCurrentItemOrArmor( 0, null );
        setCanPickUpLoot( true );
    }
    
    /// Overridden to modify attack effects.
    @Override
    protected void onTypeAttack( Entity target ) {
        if( target instanceof EntityPlayer ) {
            ItemStack stolen = MobHelper.removeHeldItem( (EntityPlayer) target );
            
            if( stolen != null ) {
                entityDropItem( stolen, 0.0F );
            }
        }
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
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        
        if( hit && (rand.nextInt( 3 ) == 0 || rand.nextInt( 1 + looting ) > 0) ) {
            dropItem( Items.gold_nugget, 1 );
        }
    }
    
    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop( int superRare ) {
        ItemStack itemStack;
        String name;
        
        if( rand.nextBoolean() ) {
            Item[] armor = {
                    Items.leather_helmet, Items.leather_chestplate, Items.leather_leggings, Items.leather_boots
            };
            String[] armorNames = {
                    "Cap", "Tunic", "Pants", "Boots"
            };
            int choice = rand.nextInt( armor.length );
            itemStack = new ItemStack( armor[choice] );
            name = armorNames[choice];
            EffectHelper.addItemText( itemStack, "§7§oGuaranteed 22% sneakier than" );
            EffectHelper.addItemText( itemStack, "§7§othe next leading brand!" );
            EffectHelper.dye( itemStack, 0x0a141f );
            EffectHelper.enchantItem( rand, itemStack, 30 );
        }
        else {
            Item[] tools = {
                    Items.golden_sword, Items.bow, Items.fishing_rod, Items.golden_pickaxe, Items.golden_axe, Items.golden_shovel
            };
            String[] toolNames = {
                    "Knife", "Bow", "Fishing Rod", "Pickaxe", "Axe", "Shovel"
            };
            int choice = rand.nextInt( tools.length );
            itemStack = new ItemStack( tools[choice] );
            name = toolNames[choice];
            EffectHelper.addItemText( itemStack, "§7§o\"Finders keepers\"" );
            
            if( tools[choice] instanceof ItemFishingRod ) {
                EffectHelper.enchantItem( itemStack, Enchantment.field_151370_z, 5 );
            }
            else {
                EffectHelper.enchantItem( itemStack, Enchantment.looting, 5 );
                
                if( !(tools[choice] instanceof ItemBow) ) {
                    EffectHelper.enchantItem( itemStack, Enchantment.fortune, 5 );
                }
            }
        }
        EffectHelper.setItemName( itemStack, "Thief's " + name, 0xe );
        EffectHelper.overrideEnchantment( itemStack, Enchantment.unbreaking, 3 );
        
        entityDropItem( itemStack, 0.0F );
    }
}