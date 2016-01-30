package toast.specialMobs.entity.enderman;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs._SpecialMobs;

public class EntityThiefEnderman extends Entity_SpecialEnderman
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "enderman/thief.png"),
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "enderman/thief_eyes.png")
    };

    public EntityThiefEnderman(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityThiefEnderman.TEXTURES);
        this.experienceValue += 1;
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 1.3);
    }

    /// Overridden to modify attack effects.
    @Override
    protected void onTypeAttack(Entity target) {
        if (target instanceof EntityLivingBase) {
            for (int i = 64; i-- > 0;) if (this.teleportTarget((EntityLivingBase)target)) {
                break;
            }
        }
        for (int i = 64; i-- > 0;) if (this.teleportRandomly()) {
            break;
        }
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        this.dropItem(Items.ender_pearl, 1);
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        Item[] armor = {
                Items.leather_helmet, Items.leather_chestplate, Items.leather_leggings, Items.leather_boots
        };
        String[] armorNames = {
                "Helmet", "Chestplate", "Leggings", "Boots"
        };
        int choice = this.rand.nextInt(armor.length);
        ItemStack itemStack = new ItemStack(armor[choice]);
        String name = armorNames[choice];

        EffectHelper.setItemName(itemStack, "Ender " + name, 0xd);
        EffectHelper.addItemText(itemStack, "\u00a77\u00a7oSeems to distort reality");
        EffectHelper.addItemText(itemStack, "\u00a77\u00a7o... or something");
        EffectHelper.dye(itemStack, 0x000000);
        EffectHelper.enchantItem(this.rand, itemStack, 30);
        EffectHelper.overrideEnchantment(itemStack, Enchantment.projectileProtection, 10);

        this.entityDropItem(itemStack, 0.0F);
    }

    /// Teleports the target to a random nearby location. Returns true if successful.
    private boolean teleportTarget(EntityLivingBase target) {
        double xI = target.posX;
        double yI = target.posY;
        double zI = target.posZ;
        target.posX += (this.rand.nextDouble() - 0.5) * 32.0;
        target.posY += this.rand.nextInt(32) - 16;
        target.posZ += (this.rand.nextDouble() - 0.5) * 32.0;
        boolean canTeleport = false;
        int blockX = (int)Math.floor(target.posX);
        int blockY = (int)Math.floor(target.posY);
        int blockZ = (int)Math.floor(target.posZ);
        if (this.worldObj.blockExists(blockX, blockY, blockZ)) {
            boolean grounded = false;
            Block block;
            while (blockY > 0) {
                block = this.worldObj.getBlock(blockX, blockY - 1, blockZ);
                if (block != null && block.getMaterial().blocksMovement()) {
                    grounded = true;
                    break;
                }
                target.posY--;
                blockY--;
            }
            if (grounded) {
                target.setPosition(target.posX, target.posY, target.posZ);
                if (target.worldObj.getCollidingBoundingBoxes(target, target.boundingBox).isEmpty() && !target.worldObj.isAnyLiquid(target.boundingBox)) {
                    canTeleport = true;
                }
            }
        }
        if (!canTeleport) {
            target.setPosition(xI, yI, zI);
            return false;
        }
        if (!(target instanceof EntityPlayerMP) || ((EntityPlayerMP)target).playerNetServerHandler.func_147362_b().isChannelOpen()) {
            target.setPositionAndUpdate(target.posX, target.posY, target.posZ);
        }

        target.playSound("mob.endermen.portal", 1.0F, 1.0F);
        target.worldObj.playSoundEffect(xI, yI, zI, "mob.endermen.portal", 1.0F, 1.0F);
        return true;
    }
}