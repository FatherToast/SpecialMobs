package toast.specialMobs.entity.creeper;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs.MobHelper;
import toast.specialMobs._SpecialMobs;

public class EntityDrowningCreeper extends Entity_SpecialCreeper
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "creeper/drowning.png")
    };

    public EntityDrowningCreeper(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityDrowningCreeper.TEXTURES);
        this.getSpecialData().isImmuneToBurning = true;
        this.getSpecialData().canBreatheInWater = true;
        this.getSpecialData().ignoreWaterPush = true;
        this.experienceValue += 4;
    }

    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 20.0);
    }

    /// The explosion caused by this creeper.
    @Override
    public void explodeByType(boolean powered, boolean griefing) {
        float power = powered ? this.explosionRadius * 2.0F : (float)this.explosionRadius;
        if (griefing) {
        	this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, 1.0F, true);
            MobHelper.drowningExplode(this, (int)power);
        }
        else {
        	this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, power, false);
        }
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
            this.dropItem(Items.bucket, 1);
        }
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
            this.dropItem(Items.gold_nugget, 1);
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        ItemStack itemStack = new ItemStack(Items.leather_helmet);
        EffectHelper.setItemName(itemStack, "Diving Helmet", 0x9);
        EffectHelper.addItemText(itemStack, "\u00a77\u00a7oBelongs to: Scuba Steve");
        EffectHelper.addItemText(itemStack, "\u00a77\u00a7oIf found, please return to \u00a7khome");
        EffectHelper.dye(itemStack, 0x99ccff);
        EffectHelper.enchantItem(itemStack, Enchantment.respiration, 10);
        EffectHelper.enchantItem(itemStack, Enchantment.aquaAffinity, 1);
        EffectHelper.enchantItem(itemStack, Enchantment.unbreaking, 3);
        this.entityDropItem(itemStack, 0.0F);
    }
}