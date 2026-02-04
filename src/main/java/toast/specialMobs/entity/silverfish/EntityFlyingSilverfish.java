package toast.specialMobs.entity.silverfish;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityFlyingSilverfish extends Entity_SpecialSilverfish {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "silverfish/flying.png" )
    };
    
    public EntityFlyingSilverfish( World world ) {
        super( world );
        getSpecialData().setTextures( EntityFlyingSilverfish.TEXTURES );
        getSpecialData().isImmuneToFalling = true;
        experienceValue += 2;
    }
    
    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        getSpecialData().multAttribute( SharedMonsterAttributes.movementSpeed, 1.3 );
    }
    
    /// Called each tick this entity's attack target can be seen.
    @Override
    protected void attackEntity( Entity target, float distanceSq ) {
        if( onGround && distanceSq >= 6.0F && distanceSq < 12.0F && rand.nextInt( 10 ) == 0 ) {
            double vX = target.posX - posX;
            double vZ = target.posZ - posZ;
            double vH = Math.sqrt( vX * vX + vZ * vZ );
            motionX = vX / vH * 2.0 + motionX * 0.2;
            motionZ = vZ / vH * 2.0 + motionZ * 0.2;
            motionY = 0.4 * 2.0;
        }
        else {
            super.attackEntity( target, distanceSq );
        }
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        if( hit && (rand.nextInt( 3 ) == 0 || rand.nextInt( 1 + looting ) > 0) ) {
            dropItem( Items.feather, 1 );
        }
    }
}