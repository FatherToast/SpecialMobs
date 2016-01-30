package toast.specialMobs.entity.ghast;

import net.minecraft.init.Items;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

public class EntityMiniGhast extends EntityMountGhast
{
    public EntityMiniGhast(World world) {
        super(world);
        this.setSize(1.0F, 1.0F);
        this.getSpecialData().resetRenderScale(0.25F);
    }

    /// Returns the sound this mob makes while it's alive.
    @Override
    protected String getLivingSound() {
        return null;
    }

    /// Called when this entity is killed.
    @Override
    protected void dropFewItems(boolean hit, int looting) {
        if (this.dimension != 0) {
            super.dropFewItems(hit, looting);
        }
        else {
            for (int i = this.rand.nextInt(3 + looting); i-- > 0;) {
                this.dropItem(Items.gunpowder, 1);
            }
        }
    }

    /// Checks if the entity's current position is a valid location to spawn this entity.
    @Override
    public boolean getCanSpawnHere() {
        return super.getCanSpawnHere() && (this.dimension != 0 || this.isValidLightLevel() && this.worldObj.canBlockSeeTheSky((int)Math.floor(this.posX), (int)Math.floor(this.posY), (int)Math.floor(this.posZ)));
    }

    /// Checks to make sure the light is not too bright where the mob is spawning.
    protected boolean isValidLightLevel() {
        int x = (int)Math.floor(this.posX);
        int y = (int)Math.floor(this.boundingBox.minY);
        int z = (int)Math.floor(this.posZ);
        if (this.worldObj.getSavedLightValue(EnumSkyBlock.Sky, x, y, z) > this.rand.nextInt(32))
            return false;
        int light = this.worldObj.getBlockLightValue(x, y, z);
        if (this.worldObj.isThundering()) {
            int tempSkylightSubtracted = this.worldObj.skylightSubtracted;
            this.worldObj.skylightSubtracted = 10;
            light = this.worldObj.getBlockLightValue(x, y, z);
            this.worldObj.skylightSubtracted = tempSkylightSubtracted;
        }
        return light <= this.rand.nextInt(8);
    }
}