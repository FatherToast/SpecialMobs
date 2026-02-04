package toast.specialMobs.entity.creeper;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.MobHelper;
import toast.specialMobs._SpecialMobs;

public class EntityDarkCreeper extends Entity_SpecialCreeper {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "creeper/dark.png" ),
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "creeper/dark_eyes.png" )
    };
    
    public EntityDarkCreeper( World world ) {
        super( world );
        getSpecialData().setTextures( EntityDarkCreeper.TEXTURES );
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
            MobHelper.darkExplode( this, (int) power );
        }
        
        if( powered ) {
            long time = worldObj.getWorldTime();
            int dayTime = (int) (time % 24000L);
            time -= dayTime;
            if( dayTime < 13000 ) {
                time += 13000L;
            }
            else {
                time += 37000L;
            }
            worldObj.setWorldTime( time );
        }
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        
        if( hit && (rand.nextInt( 3 ) == 0 || rand.nextInt( 1 + looting ) > 0) ) {
            for( int i = rand.nextInt( 8 + looting ) + 1; i-- > 0; ) {
                dropItem( Item.getItemFromBlock( Blocks.torch ), 1 );
            }
        }
    }
    
    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop( int superRare ) {
        entityDropItem( new ItemStack( Items.potionitem, 1, 8198 ), 0.0F );
    }
}