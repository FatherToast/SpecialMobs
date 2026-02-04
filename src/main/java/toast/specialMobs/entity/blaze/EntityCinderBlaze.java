package toast.specialMobs.entity.blaze;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.world.World;

public class EntityCinderBlaze extends Entity_SpecialBlaze {
    
    public EntityCinderBlaze( World world ) {
        super( world );
        setSize( 0.5F, 0.9F );
        getSpecialData().resetRenderScale( 0.5F );
    }
    
    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        getSpecialData().addAttribute( SharedMonsterAttributes.attackDamage, -2.0 );
        getSpecialData().multAttribute( SharedMonsterAttributes.movementSpeed, 1.3 );
        setRangedAI( 0, 6, 60, 100, 0.0F );
        getSpecialData().arrowDamage -= 1.0F;
    }
    
    /// Overridden to modify attack effects.
    @Override
    protected void onTypeAttack( Entity target ) {
        target.setFire( 4 );
    }
}