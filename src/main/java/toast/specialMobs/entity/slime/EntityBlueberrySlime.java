package toast.specialMobs.entity.slime;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityBlueberrySlime extends Entity_SpecialSlime {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "slime/blueberry.png" )
    };
    
    public EntityBlueberrySlime( World world ) {
        super( world );
        getSpecialData().setTextures( EntityBlueberrySlime.TEXTURES );
        getSpecialData().isImmuneToBurning = true;
        getSpecialData().canBreatheInWater = true;
    }
    
    /// Gets the additional experience this slime type gives.
    @Override
    protected int getTypeXp() {
        return 1;
    }
    
    /// Overridden to modify inherited attribites, except for health.
    @Override
    protected void adjustTypeAttributes() {
        getSpecialData().addAttribute( SharedMonsterAttributes.attackDamage, 2.0 );
    }
    
    @Override
    public boolean handleWaterMovement() {
        if( worldObj.isAnyLiquid( boundingBox ) ) {
            fallDistance = 0.0F;
            extinguish();
        }
        return false;
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        
        if( getSlimeSize() == 1 ) {
            if( hit && (rand.nextInt( 3 ) == 0 || rand.nextInt( 1 + looting ) > 0) ) {
                entityDropItem( new ItemStack( Items.dye, 1, 12 ), 0.0F );
            }
        }
    }
    
    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop( int superRare ) {
        if( getSlimeSize() == 1 ) {
            entityDropItem( new ItemStack( Items.potionitem, 1, superRare > 0 ? 8269 : 8205 ), 0.0F );
        }
    }
}