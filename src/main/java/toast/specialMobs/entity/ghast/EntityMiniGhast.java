package toast.specialMobs.entity.ghast;

import net.minecraft.init.Items;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

public class EntityMiniGhast extends EntityMountGhast {
    
    public EntityMiniGhast( World world ) {
        super( world );
        setSize( 1.0F, 1.0F );
        getSpecialData().resetRenderScale( 0.25F );
    }
    
    /// Returns the sound this mob makes while it's alive.
    @Override
    protected String getLivingSound() {
        return null;
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        if( dimension != 0 ) {
            super.dropFewItems( hit, looting );
        }
        else {
            for( int i = rand.nextInt( 3 + looting ); i-- > 0; ) {
                dropItem( Items.gunpowder, 1 );
            }
        }
    }
    
    /// Checks if the entity's current position is a valid location to spawn this entity.
    @Override
    public boolean getCanSpawnHere() {
        return super.getCanSpawnHere() && (dimension != 0 || isValidLightLevel() && worldObj.canBlockSeeTheSky( (int) Math.floor( posX ), (int) Math.floor( posY ), (int) Math.floor( posZ ) ));
    }
    
    /// Checks to make sure the light is not too bright where the mob is spawning.
    protected boolean isValidLightLevel() {
        int x = (int) Math.floor( posX );
        int y = (int) Math.floor( boundingBox.minY );
        int z = (int) Math.floor( posZ );
        
        if( worldObj.getSavedLightValue( EnumSkyBlock.Sky, x, y, z ) > rand.nextInt( 32 ) )
            return false;
        int light = worldObj.getBlockLightValue( x, y, z );
        
        if( worldObj.isThundering() ) {
            int tempSkylightSubtracted = worldObj.skylightSubtracted;
            worldObj.skylightSubtracted = 10;
            light = worldObj.getBlockLightValue( x, y, z );
            worldObj.skylightSubtracted = tempSkylightSubtracted;
        }
        return light <= rand.nextInt( 8 );
    }
}