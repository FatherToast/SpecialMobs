package toast.specialMobs.entity.creeper;

import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs._SpecialMobs;

public class EntityEnderCreeper extends Entity_SpecialCreeper
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "creeper/ender.png"),
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "creeper/ender_eyes.png")
    };

    /// The speed boost when attacking. Identical to an enderman's speed boost.
    private static final UUID attackingSpeedBoostUUID = UUID.fromString("020E0DFB-87AE-4653-9556-831010E291A0");
    private static final AttributeModifier attackingSpeedBoost = new AttributeModifier(EntityEnderCreeper.attackingSpeedBoostUUID, "Attacking speed boost", 6.2, 0).setSaved(false);

    /// The entity to attack last tick. Used to update the attacking speed boost.
    private Entity lastEntityToAttack;
    /// Ticks since this enderman last teleported.
    private int teleportDelay = 0;
    /// Ticks this enderman has been looked at.
    private int lookDelay = 0;

    public EntityEnderCreeper(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityEnderCreeper.TEXTURES);
        this.experienceValue += 2;
    }

    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 20.0);
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 1.2);
    }

    /// Returns true if this mob should use the new AI.
    @Override
    public boolean isAIEnabled() {
        return false;
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        for (int i = this.rand.nextInt(2 + looting); i-- > 0;) {
            this.dropItem(Items.ender_pearl, 1);
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        ItemStack drop = new ItemStack(Items.fishing_rod);
        EffectHelper.setItemName(drop, "Whip of Destruction", 0xd);
        drop.addEnchantment(Enchantment.sharpness, 1);
        drop.addEnchantment(Enchantment.unbreaking, 10);
        this.entityDropItem(drop, 0.0F);
    }

    /// Returns an EntityPlayer to attack or null if none is found.
    @Override
    protected Entity findPlayerToAttack() {
        EntityPlayer player = this.worldObj.getClosestVulnerablePlayerToEntity(this, 64.0);
        if (player != null) {
            if (this.shouldAttackPlayer(player)) {
                if (this.lookDelay++ == 5) {
                    this.lookDelay = 0;
                    return player;
                }
            }
            else {
                this.lookDelay = 0;
            }
        }
        return null;
    }

    /// Carried from EntityEnderman.class.
    private boolean shouldAttackPlayer(EntityPlayer player) {
        ItemStack itemStack = player.inventory.armorInventory[3];
        if (itemStack != null && itemStack.getItem() == Item.getItemFromBlock(Blocks.pumpkin))
            return false;
        Vec3 lookVec = player.getLook(1.0F).normalize();
        Vec3 posVec = Vec3.createVectorHelper(this.posX - player.posX, this.boundingBox.minY + this.height / 2.0 - player.posY - player.getEyeHeight(), this.posZ - player.posZ);
        double distance = posVec.lengthVector();
        posVec = posVec.normalize();
        double dotProduct = lookVec.dotProduct(posVec);
        return dotProduct > 1.0 - 0.025 / distance ? player.canEntityBeSeen(this) : false;
    }

    /// Called every tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        if (this.lastEntityToAttack != this.entityToAttack) {
            IAttributeInstance attribute = this.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
            attribute.removeModifier(EntityEnderCreeper.attackingSpeedBoost);
            if (this.entityToAttack != null) {
                attribute.applyModifier(EntityEnderCreeper.attackingSpeedBoost);
            }
        }
        this.lastEntityToAttack = this.entityToAttack;

        for (int i = 0; i < 2; i++) {
            this.worldObj.spawnParticle("portal", this.posX + (this.rand.nextDouble() - 0.5) * this.width, this.posY + this.rand.nextDouble() * this.height - 0.25, this.posZ + (this.rand.nextDouble() - 0.5) * this.width, (this.rand.nextDouble() - 0.5) * 2.0, -this.rand.nextDouble(), (this.rand.nextDouble() - 0.5) * 2.0);
        }
        if (this.worldObj.isDaytime() && !this.worldObj.isRemote) {
            float brightness = this.getBrightness(1.0F);
            if (brightness > 0.5F && this.worldObj.canBlockSeeTheSky((int)Math.floor(this.posX), (int)Math.floor(this.posY), (int)Math.floor(this.posZ)) && this.rand.nextFloat() * 30.0F < (brightness - 0.4F) * 2.0F) {
                this.entityToAttack = null;
                this.teleportRandomly();
            }
        }
        if (this.isWet()) {
            this.attackEntityFrom(DamageSource.drown, 1);
            this.entityToAttack = null;
            this.teleportRandomly();
        }
        this.isJumping = false;
        if (this.entityToAttack != null) {
            if (this.entityToAttack.getDistanceSqToEntity(this) < 9.0 && this.canEntityBeSeen(this.entityToAttack)) {
                this.setCreeperState(1);
            }
            else {
                this.setCreeperState(-1);
            }
            this.faceEntity(this.entityToAttack, 100.0F, 100.0F);
        }
        if (!this.worldObj.isRemote && this.isEntityAlive()) {
            if (this.entityToAttack != null) {
                if (this.entityToAttack instanceof EntityPlayer && this.shouldAttackPlayer((EntityPlayer)this.entityToAttack)) {
                    if (this.getCreeperState() < 0 && this.entityToAttack.getDistanceSqToEntity(this) < 16.0) {
                        this.teleportRandomly();
                    }
                    this.teleportDelay = 0;
                }
                else if (this.entityToAttack.getDistanceSqToEntity(this) > 256.0 && this.teleportDelay++ >= 30 && this.teleportToEntity(this.entityToAttack)) {
                    this.teleportDelay = 0;
                }
            }
            else {
                this.teleportDelay = 0;
            }
        }
        super.onLivingUpdate();
    }

    /// Damages this entity from the damageSource by the given amount. Returns true if this entity is damaged.
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float damage) {
        if (damageSource instanceof EntityDamageSourceIndirect) {
            for (int i = 0; i < 64; i++) {
                if (this.teleportRandomly())
                    return true;
            }
        }
        return super.attackEntityFrom(damageSource, damage);
    }

    /// Teleports this enderman to a random nearby location. Returns true if this entity teleports.
    protected boolean teleportRandomly() {
        double x = this.posX + (this.rand.nextDouble() - 0.5) * 64.0;
        double y = this.posY + (this.rand.nextInt(64) - 32);
        double z = this.posZ + (this.rand.nextDouble() - 0.5) * 64.0;
        return this.teleportTo(x, y, z);
    }

    /// Teleports this enderman to the given entity. Returns true if this entity teleports.
    protected boolean teleportToEntity(Entity entity) {
        Vec3 vector = Vec3.createVectorHelper(this.posX - entity.posX, this.boundingBox.minY + this.height / 2.0F - entity.posY + entity.getEyeHeight(), this.posZ - entity.posZ);
        vector = vector.normalize();
        double x = this.posX + (this.rand.nextDouble() - 0.5) * 8.0 - vector.xCoord * 16.0;
        double y = this.posY + (this.rand.nextInt(16) - 8) - vector.yCoord * 16.0;
        double z = this.posZ + (this.rand.nextDouble() - 0.5) * 8.0 - vector.zCoord * 16.0;
        return this.teleportTo(x, y, z);
    }

    /// Teleports this enderman to the given coordinates. Returns true if this entity teleports.
    protected boolean teleportTo(double x, double y, double z) {
        double xI = this.posX;
        double yI = this.posY;
        double zI = this.posZ;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        boolean canTeleport = false;
        int blockX = (int)Math.floor(this.posX);
        int blockY = (int)Math.floor(this.posY);
        int blockZ = (int)Math.floor(this.posZ);
        Block block;
        if (this.worldObj.blockExists(blockX, blockY, blockZ)) {
            boolean canTeleportToBlock = false;
            while (!canTeleportToBlock && blockY > 0) {
                block = this.worldObj.getBlock(blockX, blockY - 1, blockZ);
                if (block != null && block.getMaterial().blocksMovement()) {
                    canTeleportToBlock = true;
                }
                else {
                    --this.posY;
                    --blockY;
                }
            }
            if (canTeleportToBlock) {
                this.setPosition(this.posX, this.posY, this.posZ);
                if (this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).size() == 0 && !this.worldObj.isAnyLiquid(this.boundingBox)) {
                    canTeleport = true;
                }
            }
        }
        if (!canTeleport) {
            this.setPosition(xI, yI, zI);
            return false;
        }
        for (int i = 0; i < 128; i++) {
            double posRelative = i / 127.0;
            float vX = (this.rand.nextFloat() - 0.5F) * 0.2F;
            float vY = (this.rand.nextFloat() - 0.5F) * 0.2F;
            float vZ = (this.rand.nextFloat() - 0.5F) * 0.2F;
            double dX = xI + (this.posX - xI) * posRelative + (this.rand.nextDouble() - 0.5) * this.width * 2.0;
            double dY = yI + (this.posY - yI) * posRelative + this.rand.nextDouble() * this.height;
            double dZ = zI + (this.posZ - zI) * posRelative + (this.rand.nextDouble() - 0.5) * this.width * 2.0;
            this.worldObj.spawnParticle("portal", dX, dY, dZ, vX, vY, vZ);
        }
        this.worldObj.playSoundEffect(xI, yI, zI, "mob.endermen.portal", 1.0F, 1.0F);
        this.worldObj.playSoundAtEntity(this, "mob.endermen.portal", 1.0F, 1.0F);
        return true;
    }
}