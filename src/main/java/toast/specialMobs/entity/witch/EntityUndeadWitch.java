package toast.specialMobs.entity.witch;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
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
import toast.specialMobs.entity.skeleton.Entity_SpecialSkeleton;

public class EntityUndeadWitch extends Entity_SpecialWitch
{
    @SuppressWarnings("hiding")
	public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "witch/undead.png")
    };

    /// The number of skeletons this witch can spawn.
    public byte skeletonCount;

    public EntityUndeadWitch(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityUndeadWitch.TEXTURES);
        this.skeletonCount = (byte) (this.rand.nextInt(3) + 3);
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().multAttribute(SharedMonsterAttributes.movementSpeed, 1.1);

        this.setCurrentItemOrArmor(0, new ItemStack(Items.bone));
    }

    /// Get this entity's creature type.
    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEAD;
    }

    /// Called when the witch is looking for a potion to drink.
    @Override
	public void tryDrinkPotion() {
    	if (this.potionThrowDelay <= 0) {
    		if (this.isBurning() && !this.isPotionActive(Potion.fireResistance)) {
                this.drinkPotion(8195); // Fire Resistance
            }
            else if (this.rand.nextFloat() < 0.15F && this.isInsideOfMaterial(Material.water) && !this.isPotionActive(Potion.waterBreathing)) {
            	this.drinkPotion(8205); // Water Breathing
            }
            else if (this.rand.nextFloat() < 0.025F && this.getHealth() < this.getMaxHealth()) {
                this.drinkPotion(16396); // Splash Instant Damage
            }
            else if (this.rand.nextFloat() < 0.025F && this.getHealth() < this.getMaxHealth()) {
                this.drinkPotion(16428); // Splash Instant Damage II
            }
            else if (this.rand.nextFloat() < 0.2F && this.getAttackTarget() != null && !this.isPotionActive(Potion.moveSpeed) && this.getAttackTarget().getDistanceSqToEntity(this) > 121.0) {
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
        if (this.skeletonCount > 0 && this.rand.nextInt(4) == 0) {
            this.skeletonCount--;
            Entity_SpecialSkeleton skeleton = new Entity_SpecialSkeleton(this.worldObj);
            skeleton.copyLocationAndAnglesFrom(this);
            skeleton.setAttackTarget(this.getAttackTarget());

            skeleton.onSpawnWithEgg((IEntityLivingData) null);
            for (int i = 0; i < 4; i++) {
				skeleton.setCurrentItemOrArmor(i, null);
			}
			skeleton.setCurrentItemOrArmor(4, new ItemStack(Items.leather_helmet, 1, Items.leather_helmet.getMaxDamage() - 2 - this.rand.nextInt(5)));

			skeleton.worldObj.spawnEntityInWorld(skeleton);
            this.worldObj.playSoundAtEntity(skeleton, "mob.ghast.fireball", 0.5F, 2.0F / (this.rand.nextFloat() * 0.4F + 0.8F));
        }
        else {
        	super.attackEntityWithRangedAttack(target, range);
        }
    }

    /// Overridden to modify potion attacks. Returns true if the potion was modified.
    @Override
	protected boolean adjustSplashPotionByType(EntityPotion thrownPotion, EntityLivingBase target, float range, float distance) {
    	super.adjustSplashPotionByType(thrownPotion, target, range, distance);
    	return true; // Only throws default potion
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
			this.entityDropItem(new ItemStack(Items.spawn_egg, 1, EntityList.getEntityID(new EntitySkeleton(this.worldObj))), 0.0F);
		}
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
    	this.dropItem(Items.skull, 1);
    }

    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation(tag);
        saveTag.setByte("Skeletons", this.skeletonCount);
    }

    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation(tag);
        if (saveTag.hasKey("Skeletons")) {
            this.skeletonCount = saveTag.getByte("Skeletons");
        }
        else if (tag.hasKey("Skeletons")) {
            this.skeletonCount = tag.getByte("Skeletons");
        }
    }
}