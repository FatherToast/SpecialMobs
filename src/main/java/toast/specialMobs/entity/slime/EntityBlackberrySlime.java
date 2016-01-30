package toast.specialMobs.entity.slime;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityBlackberrySlime extends Entity_SpecialSlime
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "slime/blackberry.png")
    };

    private int fuseTime = 0;

    public EntityBlackberrySlime(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityBlackberrySlime.TEXTURES);
    }

    /// Gets the additional experience this slime type gives.
    @Override
	protected int getTypeXp() {
    	return 2;
    }

    /// Overridden to modify inherited max health.
    @Override
	protected void adjustHealthAttribute() {
    	this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 6.0);
    }

    /// Called each tick to update behavior.
    @Override
	protected void attackEntityByType(EntityPlayer target) {
		float explosionPower = this.getSlimeSize();
    	if (target != null && target.getDistanceSqToEntity(this) < 9.0F + (explosionPower - 1.0F) * 2.0F) {
    		if (this.fuseTime == 0) {
                this.playSound("creeper.primed", 1.0F, 0.5F);
    		}
    		else if (this.fuseTime >= 30) {
                this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, explosionPower, this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"));
                this.setDead();
    		}
			this.getSpecialData().setRenderScale(this.getSpecialData().getRenderScale() + 0.013F);
    		this.fuseTime++;
            this.moveStrafing = this.moveForward = 0.0F;
    		this.onGround = false;
    	}
    	else if (this.fuseTime > 0) {
    		this.fuseTime--;
			this.getSpecialData().setRenderScale(this.getSpecialData().getRenderScale() - 0.013F);
            this.moveStrafing = this.moveForward = 0.0F;
    		this.onGround = false;
    	}
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
	        if (this.getSlimeSize() == 1) {
	        for (int i = this.rand.nextInt(3) + looting; i-- > 0;) {
	            this.dropItem(Items.gunpowder, 1);
	        }
	        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
	            this.dropItem(Items.dye, 1);
	        }
        }
    }

    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setByte("FuseTime", (byte) this.fuseTime);
    }

    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        if (tag.hasKey("FuseTime")) {
            this.fuseTime = tag.getByte("FuseTime");
        }
    }
}