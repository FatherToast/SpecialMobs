package toast.specialMobs.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

public class EntityAINinja extends EntityAIBase {

    // The owner of this AI.
    protected EntityLiving theEntity;
    // The owner of this AI cast as a ninja.
    protected INinja ninjaEntity;

    public EntityAINinja(EntityLiving entity) {
        this.theEntity = entity;
        this.ninjaEntity = (INinja) entity;
        this.setMutexBits(7);
    }

    // Returns whether the AI should begin execution.
    @Override
    public boolean shouldExecute() {
    	if (this.ninjaEntity.getHidingBlock() == null)
    		return false;
    	int length = this.theEntity.worldObj.playerEntities.size();
    	try {
    		EntityPlayer player;
    		float dX, dZ;
    		float angleFromPlayer;
    		for (int i = 0; i < length; i++) {
    			player = (EntityPlayer) this.theEntity.worldObj.playerEntities.get(i);
    			dX = (float) (this.theEntity.posX - player.posX);
    			dZ = (float) (this.theEntity.posZ - player.posZ);
    			angleFromPlayer = (float) Math.atan2(dX, -dZ) * 180.0F / (float) Math.PI;
    			if (Math.abs(angleFromPlayer - MathHelper.wrapAngleTo180_float(player.rotationYawHead)) > 90.0F)
    				return true;
    		}
    	}
    	catch (Exception ex) {
    		// Do nothing
    	}
    	return false;
    }

    // Returns whether an in-progress EntityAIBase should continue executing
    @Override
    public boolean continueExecuting() {
    	return this.shouldExecute();
    }

    // Determine if this AI task is interruptible by a higher priority task.
    @Override
    public boolean isInterruptible() {
    	return false;
    }

    // Called once when the AI begins execution.
    @Override
    public void startExecuting() {
        this.theEntity.getNavigator().clearPathEntity();
        this.theEntity.motionY = 0.0;
        this.ninjaEntity.setFrozen(true);
    }

    /// Resets the task.
    @Override
    public void resetTask() {
        this.ninjaEntity.setFrozen(false);
    }
}
