package toast.specialMobs.entity.spider;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.Properties;
import toast.specialMobs._SpecialMobs;

public class EntityGhostSpider extends Entity_SpecialSpider
{
    /// Useful properties for this class.
    private static final boolean XRAY_GHOSTS = Properties.getBoolean(Properties.STATS, "xray_ghosts");

    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "spider/ghost.png"),
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "spider/ghost_eyes.png")
    };

    public EntityGhostSpider(World world) {
        super(world);
        this.noClip = true;
        this.getSpecialData().setTextures(EntityGhostSpider.TEXTURES);
        this.getSpecialData().canBreatheInWater = true;
        this.getSpecialData().isImmuneToFalling = true;
        this.getSpecialData().ignorePressurePlates = true;
        this.getSpecialData().ignoreWaterPush = true;
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.isHostile = true;
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 1.2);
    }

    /// Called each tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        if (this.worldObj.isDaytime() && !this.worldObj.isRemote) {
            float brightness = this.getBrightness(1.0F);
            if (brightness > 0.5F && this.rand.nextFloat() * 30.0F < (brightness - 0.4F) * 2.0F && this.worldObj.canBlockSeeTheSky((int)Math.floor(this.posX), (int)Math.floor(this.posY), (int)Math.floor(this.posZ))) {
                ItemStack helmet = this.getEquipmentInSlot(4);
                if (helmet != null) {
                    if (helmet.isItemStackDamageable()) {
                        helmet.setItemDamage(helmet.getItemDamageForDisplay() + this.rand.nextInt(2));
                        if (helmet.getItemDamageForDisplay() >= helmet.getMaxDamage()) {
                            this.renderBrokenItemStack(helmet);
                            this.setCurrentItemOrArmor(4, (ItemStack)null);
                        }
                    }
                }
                else {
                    this.setFire(8);
                }
            }
        }
        super.onLivingUpdate();
        if (this.onGround && super.isEntityInsideOpaqueBlock()) {
            this.jump();
        }
    }

    /// Finds the closest player within 16 blocks to attack, or null if this Entity isn't interested in attacking.
    @Override
    protected Entity findPlayerToAttack() {
        Entity player = super.findPlayerToAttack();
        return player != null && (EntityGhostSpider.XRAY_GHOSTS || this.canEntityBeSeen(player)) ? player : null;
    }

    /// Tries to moves the entity by the passed in displacement.
    @Override
    public void moveEntity(double x, double y, double z) {
        double yI = y;
        boolean shouldFall = false;
        if (this.entityToAttack != null && this.entityToAttack.boundingBox.maxY <= this.boundingBox.minY) {
            float dX = (float)(this.entityToAttack.posX - this.posX);
            float dZ = (float)(this.entityToAttack.posZ - this.posZ);
            float range = (this.entityToAttack.width + this.width) / 2.0F;
            if (dX * dX + dZ * dZ < range * range) {
                shouldFall = true;
            }
        }
        if (y < 0.0 && !shouldFall) {
            List list = this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox.addCoord(x, y, z));
            for (int i = 0; i < list.size(); ++i) {
                y = ((AxisAlignedBB)list.get(i)).calculateYOffset(this.boundingBox, y);
            }
        }
        super.moveEntity(x, y, z);
        this.isCollidedHorizontally = false;
        this.isCollidedVertically = yI != y;
        this.onGround = this.isCollidedVertically && yI < 0.0;
        this.isCollided = this.isCollidedVertically;
        this.updateFallState(y, this.onGround);
        if (this.isCollidedVertically) {
            this.motionY = 0.0;
        }
    }

    /// Get this entity's creature type.
    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEAD;
    }

    /// Checks if this entity is inside of an opaque block
    @Override
    public boolean isEntityInsideOpaqueBlock() {
        return false; /// Immune to suffocation.
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
            this.dropItem(Items.slime_ball, 1);
        }
    }
}