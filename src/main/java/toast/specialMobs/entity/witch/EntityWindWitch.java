package toast.specialMobs.entity.witch;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs._SpecialMobs;

public class EntityWindWitch extends Entity_SpecialWitch
{
    @SuppressWarnings("hiding")
	public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "witch/wind.png")
    };

    /// Ticks before this entity can teleport.
    public int teleportDelay;

    public EntityWindWitch(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityWindWitch.TEXTURES);
        this.getSpecialData().isImmuneToFalling = true;
        this.experienceValue += 2;
    }

    /// Override to set the attack AI to use.
    @Override
	protected void initTypeAI() {
        this.setMeleeAI();
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.attackDamage, 2.0);
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 1.2);

        ItemStack itemStack = new ItemStack(Items.wooden_sword);
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

    /// Called every tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        if (!this.worldObj.isRemote && this.isEntityAlive() && this.teleportDelay-- <= 0 && this.getAttackTarget() != null && this.rand.nextInt(20) == 0) {
        	if (this.getAttackTarget().getDistanceSqToEntity(this) > 36.0) {
            	this.removePotionEffect(Potion.invisibility.id);
                for (int i = 0; i < 16; i++) {
		            if (this.teleportToEntity(this.getAttackTarget())) {
						this.teleportDelay = 60;
						break;
					}
                }
        	}
        	else {
        		this.addPotionEffect(new PotionEffect(Potion.invisibility.id, 30));
                for (int i = 0; i < 16; i++) {
    			    if (this.teleportRandomly()) {
    			    	this.teleportDelay = 30;
    			    	break;
    			    }
    			}
        	}
        }
        super.onLivingUpdate();
    }

    /// Damages this entity from the damageSource by the given amount. Returns true if this entity is damaged.
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float damage) {
    	if (!this.worldObj.isRemote && damageSource.getEntity() != null) {
    		if ((this.teleportDelay -= 15) <= 0 && (damageSource instanceof EntityDamageSourceIndirect || this.rand.nextBoolean())) {
	            double xI = this.posX;
	            double yI = this.posY;
	            double zI = this.posZ;

	            for (int i = 0; i < 64; i++) {
				    if (this.teleportRandomly()) {
				    	this.teleportDelay = 30;
			    		this.addPotionEffect(new PotionEffect(Potion.invisibility.id, 30));
				    	if (damageSource instanceof EntityDamageSourceIndirect)
				    		return true;
			    		boolean hit = super.attackEntityFrom(damageSource, damage);

				    	if (this.getHealth() <= 0.0F) {
				    		this.setPosition(xI, yI, zI);
				    	}
				        return hit;
				    }
				}
    		}
    		else {
				this.removePotionEffect(Potion.invisibility.id);
			}
		}
        return super.attackEntityFrom(damageSource, damage);
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
            this.dropItem(Items.feather, 1);
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
    	ItemStack potion = new ItemStack(Items.potionitem, 1, 8206);
        EffectHelper.setItemName(potion, "Potion of Hiding", 0xf);
        EffectHelper.addPotionEffect(potion, Potion.invisibility, 1200, 0);
        EffectHelper.addPotionEffect(potion, Potion.blindness, 1200, 0);
        this.entityDropItem(potion, 0.0F);
    }

    /// Teleports this enderman to a random nearby location. Returns true if this entity teleports.
    protected boolean teleportRandomly() {
        double x = this.posX + (this.rand.nextDouble() - 0.5) * 20.0;
        double y = this.posY + (this.rand.nextInt(12) - 4);
        double z = this.posZ + (this.rand.nextDouble() - 0.5) * 20.0;
        return this.teleportTo(x, y, z);
    }

    /// Teleports this enderman to the given entity. Returns true if this entity teleports.
    protected boolean teleportToEntity(Entity entity) {
        double x = entity.posX + (this.rand.nextDouble() - 0.5) * 8.0;
        double y = entity.posY + this.rand.nextInt(8) - 2;
        double z = entity.posZ + (this.rand.nextDouble() - 0.5) * 8.0;
        return this.teleportTo(x, y, z);
    }

    /// Teleports this enderman to the given coordinates. Returns true if this entity teleports.
    protected boolean teleportTo(double x, double y, double z) {
        double xI = this.posX;
        double yI = this.posY;
        double zI = this.posZ;
        this.setPosition(x, y, z);
        if (this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).size() != 0 || this.worldObj.isAnyLiquid(this.boundingBox)) {
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
            this.worldObj.spawnParticle("smoke", dX, dY, dZ, vX, vY, vZ);
        }
        return true;
    }
}