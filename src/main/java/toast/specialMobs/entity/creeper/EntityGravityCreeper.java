package toast.specialMobs.entity.creeper;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityGravityCreeper extends Entity_SpecialCreeper {

    @SuppressWarnings("hiding")
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "creeper/gravity.png")
    };

    public EntityGravityCreeper(World world) {
        super(world);
        this.getSpecialData().setTextures(EntityGravityCreeper.TEXTURES);
        this.getSpecialData().isImmuneToFalling = true;
        this.getSpecialData().ignorePressurePlates = true;
        this.getSpecialData().immuneToPotions.add(Potion.jump.id);
        this.experienceValue += 1;
    }

    // Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        this.getSpecialData().addAttribute(SharedMonsterAttributes.maxHealth, 20.0);
    }

    @Override
    public void onExplodingUpdate() {
        if (!this.worldObj.isRemote) {
        	boolean powered = this.getPowered();
        	float radius = powered ? this.explosionRadius * 3.0F : this.explosionRadius * 1.5F;
            Entity entityHit;
            double vX, vZ, v;
            List entitiesInRange = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(radius * 2.0, radius * 2.0, radius * 2.0));
            for (int i = 0; i < entitiesInRange.size(); i++) {
            	entityHit = (Entity) entitiesInRange.get(i);
                if (this.getDistanceSqToEntity(entityHit) <= radius * radius) {
                    vX = this.posX - entityHit.posX;
                    vZ = this.posZ - entityHit.posZ;
                    v = Math.sqrt(vX * vX + vZ * vZ);
                    entityHit.motionX = vX * radius * 0.05 / (v * v);
                    entityHit.motionZ = vZ * radius * 0.05 / (v * v);
                    entityHit.onGround = false;
                    if (entityHit instanceof EntityPlayerMP) {
                        try {
                            ((EntityPlayerMP) entityHit).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(entityHit));
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    // The explosion caused by this creeper.
    @Override
    public void explodeByType(boolean powered, boolean griefing) {
        float power = powered ? (this.explosionRadius + 2) * 2.0F : (float)(this.explosionRadius + 2);
        this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, power, griefing);
    }

    // Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        super.dropFewItems(hit, looting);
        for (int i = this.rand.nextInt(3 + looting); i-- > 0;) {
            this.dropItem(Items.gunpowder, 1);
        }
        if (hit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + looting) > 0)) {
            this.dropItem(Items.gold_nugget, 1);
        }
    }

    // Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop(int superRare) {
        this.dropItem(Items.apple, 1);
    }
}