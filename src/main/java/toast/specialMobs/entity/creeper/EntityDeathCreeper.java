package toast.specialMobs.entity.creeper;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityDeathCreeper extends Entity_SpecialCreeper {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "creeper/death.png" )
    };
    
    public EntityDeathCreeper( World world ) {
        super( world );
        getSpecialData().setTextures( EntityDeathCreeper.TEXTURES );
        setExplodesWhenBurning( true );
        experienceValue += 1;
    }
    
    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        getSpecialData().multAttribute( SharedMonsterAttributes.movementSpeed, 1.2 );
    }
    
    /// The explosion caused by this creeper.
    @Override
    public void explodeByType( boolean powered, boolean griefing ) {
        float power = powered ? (explosionRadius + 2) * 2.0F : (float) (explosionRadius + 2);
        worldObj.createExplosion( this, posX, posY, posZ, power, griefing );
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        for( int i = rand.nextInt( 3 + looting ) + 1; i-- > 0; ) {
            dropItem( Items.gunpowder, 1 );
        }
        if( hit && isBurning() && looting > 0 ) {
            dropItem( Item.getItemFromBlock( Blocks.tallgrass ), 1 );
        }
    }
    
    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop( int superRare ) {
        dropItem( Item.getItemFromBlock( Blocks.tnt ), 1 );
    }
}