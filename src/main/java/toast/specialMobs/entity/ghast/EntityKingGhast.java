package toast.specialMobs.entity.ghast;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs._SpecialMobs;

public class EntityKingGhast extends Entity_SpecialGhast
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "ghast/king.png"),
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "ghast/king_shooting.png")
    };

    public EntityKingGhast(World world) {
        super(world);
        this.setSize(6.0F, 6.0F);
        this.getSpecialData().setTextures(EntityKingGhast.TEXTURES);
        this.getSpecialData().resetRenderScale(1.5F);
        this.experienceValue += 4;
    }

    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 30.0);
        this.getSpecialData().addAttribute(SharedMonsterAttributes.attackDamage, 4.0);
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 0.6);
        this.getSpecialData().setHealTime(20);
        this.getSpecialData().armor += 10;
    }

    /// Returns the multiplier this ghast has for its explosion size.
    @Override
    protected float getTypeExplosionMult() {
        return 2.5F;
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        for (int i = this.rand.nextInt(2 + looting); i-- > 0;) {
            this.dropItem(Items.gold_ingot, 1);
        }
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
            this.dropItem(Items.emerald, 1);
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        ItemStack itemStack = new ItemStack(Items.golden_helmet);
        EffectHelper.setItemName(itemStack, "King's Crown", 0xe);
        EffectHelper.enchantItem(this.rand, itemStack, 30);
        EffectHelper.overrideEnchantment(itemStack, Enchantment.thorns, 1);
        EffectHelper.overrideEnchantment(itemStack, Enchantment.fortune, 3);
        this.entityDropItem(itemStack, 0.0F);
    }
}