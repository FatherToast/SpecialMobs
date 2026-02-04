package toast.specialMobs.entity.enderman;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityBlindingEnderman extends Entity_SpecialEnderman {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "enderman/blinding.png" ),
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "enderman/blinding_eyes.png" )
    };
    
    public EntityBlindingEnderman( World world ) {
        super( world );
        getSpecialData().setTextures( EntityBlindingEnderman.TEXTURES );
        experienceValue += 1;
    }
    
    /// Called every tick while this entity is alive.
    @Override
    public void onLivingUpdate() {
        if( entityToAttack != null && entityToAttack instanceof EntityLivingBase && getDistanceSqToEntity( entityToAttack ) < 256.0 ) {
            try {
                ((EntityLivingBase) entityToAttack).addPotionEffect( new PotionEffect( Potion.blindness.id, 30, 0 ) );
                ((EntityLivingBase) entityToAttack).removePotionEffect( Potion.nightVision.id );
            }
            catch( Exception ex ) {
                _SpecialMobs.console( "[ERROR] Caught exception applying blindness to " + entityToAttack.toString() );
            }
        }
        super.onLivingUpdate();
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        for( int i = rand.nextInt( 2 + looting ); i-- > 0; ) {
            dropItem( Items.dye, 1 );
        }
    }
}