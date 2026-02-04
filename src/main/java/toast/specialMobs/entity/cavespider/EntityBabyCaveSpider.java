package toast.specialMobs.entity.cavespider;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.world.World;

public class EntityBabyCaveSpider extends Entity_SpecialCaveSpider {
    
    public EntityBabyCaveSpider( World world ) {
        super( world );
        setSize( 0.6F, 0.4F );
        experienceValue = 1;
        getSpecialData().resetRenderScale( 0.5F );
    }
    
    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        getSpecialData().addAttribute( SharedMonsterAttributes.maxHealth, -8.0 );
        getSpecialData().addAttribute( SharedMonsterAttributes.attackDamage, -1.0 );
        getSpecialData().multAttribute( SharedMonsterAttributes.movementSpeed, 1.3 );
        getSpecialData().arrowDamage -= 1.0F;
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        for( int i = rand.nextInt( 2 + looting ); i-- > 0; ) {
            dropItem( Items.string, 1 );
        }
    }
}