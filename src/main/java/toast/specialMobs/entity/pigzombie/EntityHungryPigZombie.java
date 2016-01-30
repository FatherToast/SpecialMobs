package toast.specialMobs.entity.pigzombie;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs.MobHelper;
import toast.specialMobs._SpecialMobs;

public class EntityHungryPigZombie extends Entity_SpecialPigZombie
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "pigzombie/hungry.png")
    };

    public EntityHungryPigZombie(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityHungryPigZombie.TEXTURES);
        this.experienceValue += 1;
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 10.0);
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 1.3);
        this.getSpecialData().setHealTime(20);
        this.setCanPickUpLoot(false);
    }

    /// If this returns false, this mob will not shoot with a bow.
    @Override
    public boolean willShootBow() {
        return false;
    }

    /// Overridden to modify attack effects.
    @Override
    protected void onTypeAttack(Entity target) {
        if (target instanceof EntityPlayer) {
            ItemStack itemStack = MobHelper.removeRandomItem((EntityPlayer)target);
            if (itemStack != null) {
                if (itemStack.getItem() instanceof ItemFood) {
                    this.heal(((ItemFood)itemStack.getItem()).func_150905_g(itemStack) * itemStack.stackSize);
                }
                else {
                    this.entityDropItem(itemStack, 0.0F);
                }
                this.worldObj.playSoundAtEntity(this, "random.burp", 0.5F, this.rand.nextFloat() * 0.1F + 0.9F);
            }
        }
        this.heal(2.0F);
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
            this.dropItem(Items.apple, 1);
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        if (this.rand.nextBoolean()) {
            ItemStack itemStack = new ItemStack(Items.potionitem, 1, 8196);
            EffectHelper.setItemName(itemStack, "Potion of Hunger", 0xf);
            EffectHelper.addPotionEffect(itemStack, Potion.damageBoost, 1200, 0);
            EffectHelper.addPotionEffect(itemStack, Potion.regeneration, 1200, 0);
            EffectHelper.addPotionEffect(itemStack, Potion.hunger, 600, 1);
            this.entityDropItem(itemStack, 0.0F);
        }
        else {
            this.dropItem(Items.golden_apple, 1);
        }
    }
}