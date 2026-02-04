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
    
    public EntityAINinja( EntityLiving entity ) {
        theEntity = entity;
        ninjaEntity = (INinja) entity;
        setMutexBits( 7 );
    }
    
    // Returns whether the AI should begin execution.
    @Override
    public boolean shouldExecute() {
        if( ninjaEntity.getHidingBlock() == null )
            return false;
        int length = theEntity.worldObj.playerEntities.size();
        try {
            EntityPlayer player;
            float dX, dZ;
            float angleFromPlayer;
            
            for( int i = 0; i < length; i++ ) {
                player = (EntityPlayer) theEntity.worldObj.playerEntities.get( i );
                dX = (float) (theEntity.posX - player.posX);
                dZ = (float) (theEntity.posZ - player.posZ);
                angleFromPlayer = (float) Math.atan2( dX, -dZ ) * 180.0F / (float) Math.PI;
                if( Math.abs( angleFromPlayer - MathHelper.wrapAngleTo180_float( player.rotationYawHead ) ) > 90.0F )
                    return true;
            }
        }
        catch( Exception ex ) {
            // Do nothing
        }
        return false;
    }
    
    // Returns whether an in-progress EntityAIBase should continue executing
    @Override
    public boolean continueExecuting() {
        return shouldExecute();
    }
    
    // Determine if this AI task is interruptible by a higher priority task.
    @Override
    public boolean isInterruptible() {
        return false;
    }
    
    // Called once when the AI begins execution.
    @Override
    public void startExecuting() {
        theEntity.getNavigator().clearPathEntity();
        theEntity.motionY = 0.0;
        ninjaEntity.setFrozen( true );
    }
    
    /// Resets the task.
    @Override
    public void resetTask() {
        ninjaEntity.setFrozen( false );
    }
}
