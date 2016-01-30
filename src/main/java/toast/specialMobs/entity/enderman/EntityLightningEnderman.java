package toast.specialMobs.entity.enderman;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs.MobHelper;
import toast.specialMobs._SpecialMobs;

public class EntityLightningEnderman extends Entity_SpecialEnderman
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "enderman/lightning.png"),
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "enderman/lightning_eyes.png")
    };

    public EntityLightningEnderman(World world) {
        super(world);
        this.isImmuneToFire = true;
        this.getSpecialData().setTextures(EntityLightningEnderman.TEXTURES);
        this.experienceValue += 2;
    }

    /// Overridden to modify attack effects.
    @Override
    protected void onTypeAttack(Entity target) {
        MobHelper.lightningExplode(target, 0);
        for (int i = 64; i-- > 0;) if (this.teleportRandomly()) {
            break;
        }
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        for (int i = this.rand.nextInt(2 + looting); i-- > 0;) {
            this.dropItem(Items.redstone, 1);
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        ItemStack itemStack = new ItemStack(Items.fishing_rod);
        EffectHelper.setItemName(itemStack, "Lightning Rod", 0x9);
        EffectHelper.addItemText(itemStack, "\u00a77\u00a7oCatch fish at the");
        EffectHelper.addItemText(itemStack, "\u00a77\u00a7ospeed of lightning");
        EffectHelper.enchantItem(itemStack, Enchantment.field_151369_A, 8); // Lure
        this.entityDropItem(itemStack, 0.0F);
    }
}