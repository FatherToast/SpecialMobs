package toast.specialMobs.entity.ghast;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.world.World;

public class EntityBabyGhast extends EntityMeleeGhast {
    
    public EntityBabyGhast( World world ) {
        super( world );
        setSize( 1.0F, 1.0F );
        getSpecialData().resetRenderScale( 0.25F );
        experienceValue = 1;
    }
    
    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        getSpecialData().addAttribute( SharedMonsterAttributes.attackDamage, -2.0 );
        getSpecialData().multAttribute( SharedMonsterAttributes.movementSpeed, 1.5 );
    }
    
    /// Returns the sound this mob makes while it's alive.
    @Override
    protected String getLivingSound() {
        return null;
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        for( int i = rand.nextInt( 2 + looting ); i-- > 0; ) {
            dropItem( Items.gunpowder, 1 );
        }
    }
}