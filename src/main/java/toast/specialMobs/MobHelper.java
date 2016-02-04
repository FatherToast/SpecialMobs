package toast.specialMobs;

import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockOre;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.Explosion;
import toast.specialMobs.entity.ISpecialMob;
import toast.specialMobs.entity.pigzombie.Entity_SpecialPigZombie;
import toast.specialMobs.network.MessageExplosion;

public abstract class MobHelper
{
    // Clears all melee attack AIs.
    public static void clearMeleeAttackAI(EntityLiving entity) {
        for (EntityAITaskEntry entry : (EntityAITaskEntry[])entity.tasks.taskEntries.toArray(new EntityAITaskEntry[0])) if (entry.action.getClass().equals(EntityAIAttackOnCollide.class)) {
            entity.tasks.removeTask(entry.action);
        }
    }
    // Clears all ranged attack AIs.
    public static void clearRangedAttackAI(EntityLiving entity) {
        for (EntityAITaskEntry entry : (EntityAITaskEntry[])entity.tasks.taskEntries.toArray(new EntityAITaskEntry[0])) if (entry.action.getClass().equals(EntityAIArrowAttack.class)) {
            entity.tasks.removeTask(entry.action);
        }
    }

    // Returns true if the mob has a recognized ranged attack AI.
    public static boolean hasRangedAttack(EntityLiving entity) {
        for (EntityAITaskEntry entry : (EntityAITaskEntry[])entity.tasks.taskEntries.toArray(new EntityAITaskEntry[0])) if (entry.action instanceof EntityAIArrowAttack)
            return true;
        return entity instanceof Entity_SpecialPigZombie && ((Entity_SpecialPigZombie)entity).willShootBow() && entity.getHeldItem() != null && entity.getHeldItem().getItem() instanceof ItemBow;
    }

    /*
    // Clears the entity's AI tasks.
    public static void clearAI(EntityLiving entity) {
        for (EntityAITaskEntry entry : (EntityAITaskEntry[])entity.tasks.taskEntries.toArray(new EntityAITaskEntry[0]))
            entity.tasks.removeTask(entry.action);
    }

    // Clears the entity's AI target tasks.
    public static void clearTargetAI(EntityLiving entity) {
        for (EntityAITaskEntry entry : (EntityAITaskEntry[])entity.targetTasks.taskEntries.toArray(new EntityAITaskEntry[0]))
            entity.targetTasks.removeTask(entry.action);
    }
     */

    // Drops arrows from the entity if it should drop arrows.
    public static void dropFewArrows(EntityLivingBase entity, boolean recentlyHit, int looting) {
        if (entity.getHeldItem() == null || !(entity.getHeldItem().getItem() instanceof ItemBow))
            return;
        for (int i = entity.getRNG().nextInt(3 + looting); i-- > 0;) {
            entity.dropItem(Items.arrow, 1);
        }
    }

    // Causes a creeper explosion that places dirt instead of destroying blocks.
    public static void darkExplode(Entity exploder, int radius) {
        Explosion explosion = new Explosion(exploder.worldObj, exploder, exploder.posX, exploder.posY, exploder.posZ, radius);
        float blastPower = explosion.explosionSize * (0.7F + exploder.worldObj.rand.nextFloat() * 0.6F);
        HashSet<ChunkPosition> affectedBlocks = new HashSet<ChunkPosition>();
        radius <<= 2;
        int bX = (int)exploder.posX;
        int bY = (int)exploder.posY;
        int bZ = (int)exploder.posZ;
        float resistance;
        Block block;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.sqrt(x * x + y * y + z * z) > radius) {
                        continue;
                    }
                    block = exploder.worldObj.getBlock(bX + x, bY + y, bZ + z);
                    if (block != null && block.getLightValue() > 1 && !(block instanceof BlockLiquid) && !(block instanceof BlockFire) && block != Blocks.lit_redstone_ore && !(block instanceof BlockOre)) {
                        resistance = exploder.func_145772_a(explosion, exploder.worldObj, bX + x, bY + y, bZ + z, block) + 0.3F;
                        if (blastPower - resistance * 0.3F > 0.0F && exploder.func_145774_a(explosion, exploder.worldObj, bX + x, bY + y, bZ + z, block, blastPower)) {
                            affectedBlocks.add(new ChunkPosition(bX + x, bY + y, bZ + z));
                        }
                    }
                }
            }
        }
        explosion.affectedBlockPositions.addAll(affectedBlocks);
        explosion.doExplosionB(false);
        _SpecialMobs.CHANNEL.sendToDimension(new MessageExplosion(explosion), exploder.dimension);
    }

    // Causes a creeper explosion that places dirt instead of destroying blocks.
    public static void dirtExplode(Entity exploder, int radius) {
        int bX = (int)exploder.posX;
        int bY = (int)exploder.posY;
        int bZ = (int)exploder.posZ;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.sqrt(x * x + y * y + z * z) > radius) {
                        continue;
                    }
                    if (Blocks.dirt.canPlaceBlockAt(exploder.worldObj, bX + x, bY + y, bZ + z)) {
                        exploder.worldObj.setBlock(bX + x, bY + y, bZ + z, Blocks.dirt, 0, 2);
                    }

                }
            }
        }
    }

    // Causes a creeper explosion that places dirt instead of destroying blocks.
    public static void drowningExplode(Entity exploder, int radius) {
        radius += 3;
        int bX = (int)exploder.posX;
        int bY = (int)exploder.posY;
        int bZ = (int)exploder.posZ;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double dist = Math.sqrt(x * x + y * y + z * z);
                    if (dist <= radius) {
                        if (dist > radius - 1) {
                            if (Blocks.cobblestone.canPlaceBlockAt(exploder.worldObj, bX + x, bY + y, bZ + z)) {
                            	if (exploder.worldObj.rand.nextFloat() < 0.25F) {
                            		exploder.worldObj.setBlock(bX + x, bY + y, bZ + z, Blocks.monster_egg, 1, 2);
                            	}
                            	else {
                            		exploder.worldObj.setBlock(bX + x, bY + y, bZ + z, Blocks.cobblestone, 0, 2);
                            	}
                            }
                        }
                        else if (Blocks.water.canPlaceBlockAt(exploder.worldObj, bX + x, bY + y, bZ + z)) {
                            exploder.worldObj.setBlock(bX + x, bY + y, bZ + z, Blocks.water, 0, 2);
                        }
                    }
                }
            }
        }
    }

    // Causes a creeper explosion that shoots out falling gravel.
    public static void gravelExplode(Entity exploder, float power) {
    	power += 4.0F;
        int count = (int) Math.ceil(power * power * 3.5F);
        EntityFallingBlock gravel;
        float speed;
        float pitch, yaw;
        for (int i = 0; i < count; i++) {
        	gravel = new EntityFallingBlock(exploder.worldObj, exploder.posX, exploder.posY + exploder.height / 2.0F, exploder.posZ, Blocks.gravel);
        	gravel.field_145812_b = 1; // time alive, if it starts at 0, the entity will normally die instantly
        	gravel.field_145813_c = false; // drop item if can't place
        	gravel.func_145806_a(true); // setHurtEntities
        	gravel.fallDistance = 3.0F;

        	speed = (power * 0.7F + exploder.worldObj.rand.nextFloat() * power) / 20.0F;
        	pitch = exploder.worldObj.rand.nextFloat() * (float) Math.PI;
        	yaw = exploder.worldObj.rand.nextFloat() * 2.0F * (float) Math.PI;
        	gravel.motionX = MathHelper.cos(yaw) * speed;
        	gravel.motionY = MathHelper.sin(pitch) * (power + exploder.worldObj.rand.nextFloat() * power) / 18.0F;
        	gravel.motionZ = MathHelper.sin(yaw) * speed;
            exploder.worldObj.spawnEntityInWorld(gravel);
        }
    }

    // Causes a creeper explosion that spawns lightning.
    public static void lightningExplode(Entity exploder, int radius) {
    	MobHelper.lightningExplode(exploder, exploder.posX, exploder.posY, exploder.posZ, radius);
    }
    public static void lightningExplode(Entity exploder, double posX, double posY, double posZ, int radius) {
        radius /= 3;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                exploder.worldObj.spawnEntityInWorld(new EntityLightningBolt(exploder.worldObj, posX + x, posY, posZ + z));
            }
        }
        _SpecialMobs.CHANNEL.sendToDimension(new MessageExplosion(posX, posY, posZ, radius, "lightning"), exploder.dimension);
    }

    // Removes the player's currently held item and returns it.
    public static ItemStack removeHeldItem(EntityPlayer player) {
        ItemStack heldItem = player.inventory.getCurrentItem();
        if (heldItem != null) {
            player.inventory.setInventorySlotContents(player.inventory.currentItem, (ItemStack)null);
        }
        return heldItem;
    }

    // Removes a random item stack from the player's inventory and returns it.
    public static ItemStack removeRandomItem(EntityPlayer player) {
        int count = 0;
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            if (player.inventory.getStackInSlot(i) != null) {
                count++;
            }
        }
        if (count > 0) {
            count = _SpecialMobs.random.nextInt(count);
            ItemStack item;
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                item = player.inventory.getStackInSlot(i);
                if (item != null && --count < 0) {
                    player.inventory.setInventorySlotContents(i, (ItemStack)null);
                    return item;
                }
            }
        }
        return null;
    }

    // Returns true if the damage from a source is a critical hit.
    public static boolean isCritical(DamageSource damageSource) {
    	if (damageSource.getSourceOfDamage() instanceof EntityArrow)
    		return ((EntityArrow) damageSource.getSourceOfDamage()).getIsCritical();
    	return damageSource.getEntity() != null && !damageSource.getEntity().isInWater() && damageSource.getEntity().fallDistance > 0.0F;
    }

    // Returns true if the entity can be replaced by a special version.
    public static boolean canReplace(EntityLiving entity) {
        if (!entity.isNoDespawnRequired() && !(entity instanceof ISpecialMob) && entity.getEntityData().getByte("smi") == 0)
            return true;
        return false;
    }
}