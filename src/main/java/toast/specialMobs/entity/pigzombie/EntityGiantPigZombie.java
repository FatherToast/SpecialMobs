package toast.specialMobs.entity.pigzombie;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.world.World;

public class EntityGiantPigZombie extends Entity_SpecialPigZombie {
    
    public EntityGiantPigZombie( World world ) {
        super( world );
        stepHeight = 1.0F;
        setSize( 0.9F, 2.7F );
        func_146069_a( 1.0F ); // Set size scale
        getSpecialData().resetRenderScale( 1.5F );
        experienceValue += 2;
    }
    
    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        getSpecialData().addAttribute( SharedMonsterAttributes.maxHealth, 20.0 );
        getSpecialData().addAttribute( SharedMonsterAttributes.attackDamage, 2.0 );
        getSpecialData().arrowDamage += 2.0F;
        getSpecialData().arrowRange += 2.0F;
    }
    
    /// If true, this entity is a baby.
    @Override
    public boolean isChild() {
        return false;
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        
        for( int i = rand.nextInt( 2 ) + 1; i-- > 0; ) {
            dropItem( Items.rotten_flesh, 1 );
        }
    }
}