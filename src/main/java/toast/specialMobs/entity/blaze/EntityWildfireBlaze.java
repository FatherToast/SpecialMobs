package toast.specialMobs.entity.blaze;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityWildfireBlaze extends Entity_SpecialBlaze
{
    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "blaze/wildfire.png")
    };

    /// The number of cinders this blaze can spawn.
    public byte babyCount;
    /// The number of cinders spawned on death.
    private byte babies;

    public EntityWildfireBlaze(World world) {
        super(world);
        this.setSize(0.9F, 2.7F);
        this.getSpecialData().setTextures(EntityWildfireBlaze.TEXTURES);
        this.getSpecialData().resetRenderScale(1.5F);
        this.experienceValue += 2;
        this.babyCount = (byte) (this.rand.nextInt(7) + 4);
        this.babies = (byte) (3 + this.rand.nextInt(4));
    }

    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 20.0);
        this.setRangedAI(1, 0, 30, 50, 20.0F);
        this.getSpecialData().arrowSpread *= 0.1F;
    }

    // Called to attack the target entity with a fireball.
    @Override
	public void shootFireballAtEntity(Entity target, float distance) {
        if (this.babyCount > 0 && this.rand.nextInt(3) != 0) {
            this.babyCount--;
            EntityCinderBlaze baby = new EntityCinderBlaze(this.worldObj);
            baby.copyLocationAndAnglesFrom(this);
            baby.onSpawnWithEgg((IEntityLivingData) null);
            baby.setTarget(this.getEntityToAttack());
            this.worldObj.spawnEntityInWorld(baby);
            this.worldObj.playAuxSFXAtEntity((EntityPlayer) null, 1009, (int) this.posX, (int) this.posY, (int) this.posZ, 0);
        }
        else {
            super.shootFireballAtEntity(target, distance);
        }
    }

    /// Overridden to modify attack effects.
    @Override
	protected void onTypeAttack(Entity target) {
    	target.setFire(8);
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        if (hit) {
	        for (int i = this.rand.nextInt(2 + looting); i-- > 0;) {
	            this.dropItem(Items.coal, 1);
	        }
	        if (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0) {
	            this.entityDropItem(new ItemStack(Items.spawn_egg, 1, EntityList.getEntityID(new EntityBlaze(this.worldObj))), 0.0F);
	        }
        }

        if (!this.worldObj.isRemote) {
        	EntityCinderBlaze baby = null;
            for (int i = this.babies; i-- > 0;) {
                baby = new EntityCinderBlaze(this.worldObj);
                baby.copyLocationAndAnglesFrom(this);
                baby.onSpawnWithEgg((IEntityLivingData) null);
                baby.setTarget(this.getEntityToAttack());
                this.worldObj.spawnEntityInWorld(baby);
            }
            if (baby != null) {
                this.worldObj.playAuxSFXAtEntity((EntityPlayer) null, 1009, (int) this.posX, (int) this.posY, (int) this.posZ, 0);
                baby.spawnExplosionParticle();
            }
        }
    }
}