package toast.specialMobs.entity.witch;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.SpecialMobData;
import toast.specialMobs.entity.spider.EntityBabySpider;
import toast.specialMobs.entity.spider.EntitySmallSpider;
import toast.specialMobs.entity.spider.Entity_SpecialSpider;

public class EntityWildsWitch extends Entity_SpecialWitch
{
    @SuppressWarnings("hiding")
	public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "witch/wilds.png")
    };

    /// The number of spiders this witch can spawn.
    public byte spiderCount;
    /// The number of times this witch can spawn baby spiders.
    public byte babyCount;
    /// The number of baby spiders this witch can spawn at once.
    public byte babiesPerSpawn = 3;



    public EntityWildsWitch(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityWildsWitch.TEXTURES);
        this.getSpecialData().isImmuneToWebs = true;
        this.spiderCount = (byte) (this.rand.nextInt(4) + 1);
        this.babyCount = (byte) (this.rand.nextInt(3) + 2);
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 0.7);
    }

    /// Called when the witch is looking for a potion to drink.
    @Override
	public void tryDrinkPotion() {
    	if (this.potionThrowDelay <= 0) {
    		EntityLivingBase riding = this.ridingEntity instanceof EntityLivingBase ? (EntityLivingBase) this.ridingEntity : null;

    		if (riding != null && riding.isBurning() && !riding.isPotionActive(Potion.fireResistance)) {
                this.drinkPotion(16387); // Splash Fire Resistance
            }
    		else if (this.isBurning() && !this.isPotionActive(Potion.fireResistance)) {
                this.drinkPotion(8195); // Fire Resistance
            }
            else if (this.rand.nextFloat() < 0.2F && riding != null && riding.isInsideOfMaterial(Material.water) && !riding.isPotionActive(Potion.waterBreathing) ||
            		this.rand.nextFloat() < 0.15F && this.isInsideOfMaterial(Material.water) && !this.isPotionActive(Potion.waterBreathing)) {
            	if (riding != null && riding.isInsideOfMaterial(Material.water) && !riding.isPotionActive(Potion.waterBreathing)) {
	            	this.drinkPotion(16397); // Splash Water Breathing
	            }
	            else {
	            	this.drinkPotion(8205); // Water Breathing
	            }
            }
            else if (this.rand.nextFloat() < 0.1F && riding != null && riding.getHealth() < riding.getMaxHealth() ||
            		this.rand.nextFloat() < 0.05F && this.getHealth() < this.getMaxHealth()) {
                if (riding != null && riding.getHealth() < riding.getMaxHealth()) {
					this.drinkPotion(16389); // Splash Instant Health
				}
	            else {
	                this.drinkPotion(8197); // Instant Health
	            }
    		}
            else if (this.spiderCount > 0 && this.ridingEntity == null) {
            	if (this.rand.nextFloat() < 0.1F && this.getAttackTarget() != null) {
            		this.potionThrowDelay = 8;
	                this.spiderCount--;
	                Entity_SpecialSpider spider = new Entity_SpecialSpider(this.worldObj);
	                spider.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
	                if (!this.worldObj.getCollidingBoundingBoxes(spider, spider.boundingBox).isEmpty()) {
	                	// Spider too big, get a smaller one
	                	spider = new EntitySmallSpider(this.worldObj);
	                    spider.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
	                }
	                spider.onSpawnWithEgg((IEntityLivingData) null);
	                spider.isHostile = true;
	                spider.setTarget(this.getAttackTarget());

	                SpecialMobData data = spider.getSpecialData();
	                data.arrowRefireMin = 0;
	                data.arrowRefireMax = 0;
	                data.arrowRange = 0.0F;

	                this.worldObj.spawnEntityInWorld(spider);
	                this.mountEntity(spider);
	                this.worldObj.playSoundAtEntity(spider, "mob.ghast.fireball", 0.5F, 2.0F / (this.rand.nextFloat() * 0.4F + 0.8F));
	            }
            }
            else if (this.rand.nextFloat() < 0.2F && riding != null && this.getAttackTarget() != null && !riding.isPotionActive(Potion.moveSpeed) && this.getAttackTarget().getDistanceSqToEntity(this) > 121.0) {
                this.drinkPotion(16418); // Splash Swiftness II
            }
            else if (this.rand.nextFloat() < 0.1F && riding == null && this.getAttackTarget() != null && !this.isPotionActive(Potion.moveSpeed) && this.getAttackTarget().getDistanceSqToEntity(this) > 121.0) {
                this.drinkPotion(16386); // Splash Swiftness
            }
            else {
            	this.tryDrinkPotionByType();
            }
    	}
    }

    /// Attack the specified entity using a ranged attack.
    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float range) {
        if (this.babyCount > 0 && this.rand.nextInt(4) == 0) {
            this.babyCount--;
            EntityBabySpider baby = null;
            for (int i = this.babiesPerSpawn; i-- > 0;) {
                baby = new EntityBabySpider(this.worldObj);
                baby.copyLocationAndAnglesFrom(this);
                baby.onSpawnWithEgg((IEntityLivingData)null);
                baby.isHostile = true;
                baby.setTarget(this.getAttackTarget());

	            SpecialMobData data = baby.getSpecialData();
	            data.arrowRefireMin = 0;
	            data.arrowRefireMax = 0;
	            data.arrowRange = 0.0F;

                this.worldObj.spawnEntityInWorld(baby);
            }
            if (baby != null) {
                this.worldObj.playSoundAtEntity(baby, "mob.ghast.fireball", 0.5F, 2.0F / (this.rand.nextFloat() * 0.4F + 0.8F));
                baby.spawnExplosionParticle();
            }
        }
        else {
        	super.attackEntityWithRangedAttack(target, range);
        }
    }

    /// Overridden to modify potion attacks. Returns true if the potion was modified.
    @Override
	protected boolean adjustSplashPotionByType(EntityPotion thrownPotion, EntityLivingBase target, float range, float distance) {
        thrownPotion.setPotionDamage(16388); // The default potion // Splash Poison
        if (target.getHealth() <= 2.0F) {
        	super.adjustSplashPotionByType(thrownPotion, target, range, distance); // Default - damage dealing potion
        }
    	return true;
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
			this.dropItem(Items.spider_eye, 1);
		}
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
			this.dropItem(Items.fermented_spider_eye, 1);
		}
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        this.entityDropItem(new ItemStack(Items.spawn_egg, 1, EntityList.getEntityID(new EntitySkeleton(this.worldObj))), 0.0F);
    }

    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation(tag);
        saveTag.setByte("Spiders", this.spiderCount);
        saveTag.setByte("BabyCount", this.babyCount);
        saveTag.setByte("BabiesPerSpawn", this.babiesPerSpawn);
    }

    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation(tag);
        if (saveTag.hasKey("Spiders")) {
            this.spiderCount = saveTag.getByte("Spiders");
        }
        else if (tag.hasKey("Spiders")) {
            this.spiderCount = tag.getByte("Spiders");
        }
        if (saveTag.hasKey("BabyCount")) {
            this.babyCount = saveTag.getByte("BabyCount");
        }
        else if (tag.hasKey("BabyCount")) {
            this.babyCount = tag.getByte("BabyCount");
        }
        if (saveTag.hasKey("BabiesPerSpawn")) {
            this.babiesPerSpawn = saveTag.getByte("BabiesPerSpawn");
        }
        else if (tag.hasKey("BabiesPerSpawn")) {
            this.babiesPerSpawn = tag.getByte("BabiesPerSpawn");
        }
    }
}