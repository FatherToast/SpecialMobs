package toast.specialMobs.entity.creeper;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.MobHelper;
import toast.specialMobs._SpecialMobs;

public class EntityDirtCreeper extends Entity_SpecialCreeper {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "creeper/dirt.png" )
    };
    
    public EntityDirtCreeper( World world ) {
        super( world );
        getSpecialData().setTextures( EntityDirtCreeper.TEXTURES );
        getSpecialData().isImmuneToBurning = true;
        experienceValue += 1;
    }
    
    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        getSpecialData().armor += 6;
    }
    
    /// The explosion caused by this creeper.
    @Override
    public void explodeByType( boolean powered, boolean griefing ) {
        float power = powered ? explosionRadius * 2.0F : (float) explosionRadius;
        worldObj.createExplosion( this, posX, posY, posZ, power, false );
        if( griefing ) {
            MobHelper.dirtExplode( this, (int) power );
        }
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        for( int i = rand.nextInt( 3 + looting ); i-- > 0; ) {
            dropItem( Item.getItemFromBlock( Blocks.dirt ), 1 );
        }
        if( hit && (rand.nextInt( 3 ) == 0 || rand.nextInt( 1 + looting ) > 0) ) {
            dropItem( Items.bread, 1 );
        }
    }
}