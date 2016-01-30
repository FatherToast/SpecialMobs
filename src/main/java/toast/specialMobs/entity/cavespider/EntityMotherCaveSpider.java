package toast.specialMobs.entity.cavespider;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs._SpecialMobs;

public class EntityMotherCaveSpider extends Entity_SpecialCaveSpider
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "cavespider/mother.png"),
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "cavespider/mother_eyes.png")
    };

    /// The number of babies spawned on death.
    private byte babies;

    public EntityMotherCaveSpider(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityMotherCaveSpider.TEXTURES);
        this.experienceValue += 2;
        this.babies = (byte) (3 + this.rand.nextInt(4));
    }

    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 16.0);
        this.getSpecialData().addAttribute(SharedMonsterAttributes.attackDamage, 3.0);
        this.getSpecialData().setHealTime(20);
        this.getSpecialData().armor += 6;
        this.getSpecialData().arrowDamage += 1.5F;
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
            this.entityDropItem(new ItemStack(Items.spawn_egg, 1, EntityList.getEntityID(new EntityCaveSpider(this.worldObj))), 0.0F);
        }

        if (!this.worldObj.isRemote) {
            EntityBabyCaveSpider baby = null;
            for (int i = this.babies; i-- > 0;) {
                baby = new EntityBabyCaveSpider(this.worldObj);
                baby.copyLocationAndAnglesFrom(this);
                baby.setTarget(this.getEntityToAttack());
                baby.onSpawnWithEgg((IEntityLivingData)null);
                this.worldObj.spawnEntityInWorld(baby);
            }
            if (baby != null) {
            	this.worldObj.playSoundAtEntity(baby, "random.pop", 1.0F, 2.0F / (this.rand.nextFloat() * 0.4F + 0.8F));
                baby.spawnExplosionParticle();
            }
        }
    }

    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        ItemStack itemStack;
        String name;
        if (this.rand.nextBoolean()) {
            Item[] armor = {
                    Items.chainmail_helmet, Items.chainmail_chestplate, Items.chainmail_leggings, Items.chainmail_boots
            };
            String[] armorNames = {
                    "Helmet", "Chestplate", "Leggings", "Boots"
            };
            int choice = this.rand.nextInt(armor.length);
            itemStack = new ItemStack(armor[choice]);
            name = armorNames[choice];
        }
        else {
            Item[] tools = {
                    Items.stone_sword, Items.bow, Items.stone_pickaxe, Items.stone_axe, Items.stone_shovel
            };
            String[] toolNames = {
                    "Sword", "Bow", "Pickaxe", "Axe", "Shovel"
            };
            int choice = this.rand.nextInt(tools.length);
            itemStack = new ItemStack(tools[choice]);
            name = toolNames[choice];
        }

        int maxDamage = Math.max(itemStack.getMaxDamage() - 25, 1);
        int damage = itemStack.getMaxDamage() - this.rand.nextInt(this.rand.nextInt(maxDamage) + 1);
        if (damage > maxDamage) {
            damage = maxDamage;
        }
        else if (damage < 1) {
            damage = 1;
        }
        itemStack.setItemDamage(damage);

        EffectHelper.setItemName(itemStack, "Partially Digested " + name, 0xa);
        EffectHelper.addItemText(itemStack, "\u00a77\u00a7oIt's a bit slimy...");
        EffectHelper.enchantItem(this.rand, itemStack, 30);
        EffectHelper.overrideEnchantment(itemStack, Enchantment.unbreaking, 10);

        this.entityDropItem(itemStack, 0.0F);
    }

    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setByte("Babies", this.babies);
    }

    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        if (tag.hasKey("Babies")) {
            this.babies = tag.getByte("Babies");
        }
    }
}