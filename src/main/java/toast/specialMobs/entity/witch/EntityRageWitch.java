package toast.specialMobs.entity.witch;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs._SpecialMobs;

public class EntityRageWitch extends Entity_SpecialWitch
{
    @SuppressWarnings("hiding")
	public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "witch/rage.png")
    };

    public EntityRageWitch(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityRageWitch.TEXTURES);
    }

    /// Override to set the attack AI to use.
    @Override
	protected void initTypeAI() {
        this.setMeleeAI();
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 4.0);

        ItemStack itemStack = new ItemStack(Items.golden_sword);
        float tension = this.worldObj.func_147462_b(this.posX, this.posY, this.posZ);
        if (this.rand.nextFloat() < 0.25F * tension) {
            try {
            	EnchantmentHelper.addRandomEnchantment(this.rand, itemStack, (int) (5.0F + tension * this.rand.nextInt(18)));
            }
            catch (Exception ex) {
            	_SpecialMobs.console("Error applying enchantments! entity:" + this.toString());
            	ex.printStackTrace();
            }
        }
        this.setCurrentItemOrArmor(0, itemStack);
    }

    /// Overridden to modify potion drinking ai.
    @Override
	protected void tryDrinkPotionByType() {
    	if (this.rand.nextFloat() < 0.2F && this.getAttackTarget() != null && !this.isPotionActive(Potion.resistance) && !this.isPotionActive(Potion.damageBoost)) {
            this.drinkPotion(this.makeRagePotion());
        }
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
			this.dropItem(Items.blaze_powder, 1);
		}
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        this.entityDropItem(this.makeRagePotion(), 0.0F);
    }

    private ItemStack makeRagePotion() {
    	ItemStack potion = new ItemStack(Items.potionitem, 1, 8201);
        EffectHelper.setItemName(potion, "Potion of Rage", 0xf);
        EffectHelper.addPotionEffect(potion, Potion.damageBoost, 300, 0);
        EffectHelper.addPotionEffect(potion, Potion.resistance, 1500, 0);
        return potion;
    }
}