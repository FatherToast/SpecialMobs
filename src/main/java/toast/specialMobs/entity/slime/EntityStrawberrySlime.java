package toast.specialMobs.entity.slime;

import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityStrawberrySlime extends Entity_SpecialSlime {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "slime/strawberry.png" )
    };
    
    public EntityStrawberrySlime( World world ) {
        super( world );
        getSpecialData().setTextures( EntityStrawberrySlime.TEXTURES );
        getSpecialData().isImmuneToFire = isImmuneToFire = true;
        getSpecialData().isDamagedByWater = true;
    }
    
    /// Gets the additional experience this slime type gives.
    @Override
    protected int getTypeXp() {
        return 1;
    }
    
    /// Overridden to modify attack effects.
    @Override
    protected void onTypeAttack( Entity target ) {
        target.setFire( getSlimeSize() * 4 );
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        
        if( getSlimeSize() == 1 ) {
            for( int i = rand.nextInt( 2 + looting ); i-- > 0; ) {
                dropItem( Items.fire_charge, 1 );
            }
            if( hit && (rand.nextInt( 3 ) == 0 || rand.nextInt( 1 + looting ) > 0) ) {
                entityDropItem( new ItemStack( Items.dye, 1, 1 ), 0.0F );
            }
        }
    }
    
    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop( int superRare ) {
        if( getSlimeSize() == 1 ) {
            entityDropItem( new ItemStack( Items.potionitem, 1, superRare > 0 ? 8259 : 8195 ), 0.0F );
        }
    }
}