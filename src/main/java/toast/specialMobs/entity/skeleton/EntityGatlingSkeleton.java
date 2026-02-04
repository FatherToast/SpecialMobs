package toast.specialMobs.entity.skeleton;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityGatlingSkeleton extends Entity_SpecialSkeleton {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "skeleton/gatling.png" ),
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "skeleton/gatling_wither.png" )
    };
    
    public EntityGatlingSkeleton( World world ) {
        super( world );
        getSpecialData().setTextures( EntityGatlingSkeleton.TEXTURES );
        experienceValue += 4;
    }
    
    /// Override to set the attack AI to use.
    @Override
    protected void initTypeAI() {
        setRangedAI( 0.7, 10, 20, 15.0F );
        setMeleeAI( 1.2 );
    }
    
    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        getSpecialData().addAttribute( SharedMonsterAttributes.maxHealth, 20.0 );
        getSpecialData().addAttribute( SharedMonsterAttributes.attackDamage, 2.0 );
        getSpecialData().arrowDamage /= 4.0F;
        getSpecialData().arrowSpread += 2.0F;
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        for( int i = rand.nextInt( 2 ) + 1; i-- > 0; ) {
            dropItem( Items.arrow, 1 );
        }
        if( hit && (rand.nextInt( 3 ) == 0 || rand.nextInt( 1 + looting ) > 0) ) {
            dropItem( Items.gunpowder, 1 );
        }
    }
}