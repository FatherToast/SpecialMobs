package toast.specialMobs.entity.creeper;

import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.MobHelper;
import toast.specialMobs._SpecialMobs;

public class EntityLightningCreeper extends Entity_SpecialCreeper {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] { new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "creeper/lightning.png" ) };
    
    public EntityLightningCreeper( World world ) {
        super( world );
        getSpecialData().setTextures( EntityLightningCreeper.TEXTURES );
        getSpecialData().isImmuneToFire = isImmuneToFire = true;
        experienceValue += 1;
    }
    
    /// The explosion caused by this creeper.
    @Override
    public void explodeByType( boolean powered, boolean griefing ) {
        float power = powered ? explosionRadius * 2.0F : explosionRadius / 3.0F;
        worldObj.createExplosion( this, posX, posY, posZ, power, griefing );
        MobHelper.lightningExplode( this, explosionRadius );
        
        if( powered ) {
            int duration = rand.nextInt( 12000 ) + 3600;
            if( !worldObj.getWorldInfo().isThundering() || worldObj.getWorldInfo().getThunderTime() < duration ) {
                worldObj.getWorldInfo().setThunderTime( duration );
                worldObj.getWorldInfo().setThundering( true );
            }
            duration += 1200;
            if( !worldObj.getWorldInfo().isRaining() || worldObj.getWorldInfo().getRainTime() < duration ) {
                worldObj.getWorldInfo().setRainTime( duration );
                worldObj.getWorldInfo().setRaining( true );
            }
        }
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        
        for( int i = rand.nextInt( 2 + looting ); i-- > 0; ) {
            dropItem( Items.redstone, 1 );
        }
    }
}