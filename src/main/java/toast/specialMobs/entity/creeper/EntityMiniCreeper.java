package toast.specialMobs.entity.creeper;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.world.World;

public class EntityMiniCreeper extends Entity_SpecialCreeper {
    
    public EntityMiniCreeper( World world ) {
        super( world );
        setSize( 0.5F, 0.9F );
        getSpecialData().resetRenderScale( 0.5F );
        experienceValue -= 1;
    }
    
    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        getSpecialData().multAttribute( SharedMonsterAttributes.movementSpeed, 1.3 );
    }
    
    /// The explosion caused by this creeper.
    @Override
    public void explodeByType( boolean powered, boolean griefing ) {
        float power = powered ? (float) explosionRadius : explosionRadius / 2.0F;
        worldObj.createExplosion( this, posX, posY, posZ, power, griefing );
    }
}