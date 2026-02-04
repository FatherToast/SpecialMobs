package toast.specialMobs.entity.enderman;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.world.World;

public class EntityMiniEnderman extends Entity_SpecialEnderman {
    
    public EntityMiniEnderman( World world ) {
        super( world );
        stepHeight = 0.5F;
        setSize( 0.5F, 0.9F );
        getSpecialData().resetRenderScale( 0.35F );
    }
    
    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        getSpecialData().addAttribute( SharedMonsterAttributes.attackDamage, -2.0 );
        getSpecialData().multAttribute( SharedMonsterAttributes.movementSpeed, 1.3 );
    }
}