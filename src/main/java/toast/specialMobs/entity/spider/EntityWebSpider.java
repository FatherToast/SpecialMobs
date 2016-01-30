package toast.specialMobs.entity.spider;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityWebSpider extends Entity_SpecialSpider
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "spider/web.png"),
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "spider/web_eyes.png")
    };

    /// The number of webs this spider can sling.
    private byte webCount;

    public EntityWebSpider(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityWebSpider.TEXTURES);
        this.experienceValue += 2;
        this.webCount = (byte) (this.rand.nextInt(5) + 2);
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 4.0);
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 1.2);
    }

    /// Overridden to modify attack effects.
    @Override
    public void onTypeAttack(Entity target) {
        if (!this.worldObj.isRemote && this.webCount > 0 && !(target instanceof EntitySpider) && target instanceof EntityLivingBase) {
            int x = (int)Math.floor(target.posX);
            int y = (int)Math.floor(target.posY);
            int z = (int)Math.floor(target.posZ);
            Block block = this.worldObj.getBlock(x, y, z);
            if (block == null || block.isReplaceable(this.worldObj, x, y, z)) {
                this.worldObj.setBlock(x, y, z, Blocks.web, 0, 2);
                this.webCount--;
            }
            else if (target.height > 1.0F) {
                y++;
                block = this.worldObj.getBlock(x, y, z);
                if (block == null || block.isReplaceable(this.worldObj, x, y, z)) {
                    this.worldObj.setBlock(x, y, z, Blocks.web, 0, 2);
                    this.webCount--;
                }
            }
        }
    }

    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setByte("WebCount", this.webCount);
    }

    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        if (tag.hasKey("WebCount")) {
            this.webCount = tag.getByte("WebCount");
        }
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        this.dropItem(Items.string, 1);
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        this.dropItem(Item.getItemFromBlock(Blocks.web), 1);
    }
}