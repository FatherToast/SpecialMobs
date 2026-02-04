package toast.specialMobs.entity.skeleton;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntitySniperSkeleton extends Entity_SpecialSkeleton {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "skeleton/sniper.png" ),
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "skeleton/sniper_wither.png" )
    };
    
    public EntitySniperSkeleton( World world ) {
        super( world );
        getSpecialData().setTextures( EntitySniperSkeleton.TEXTURES );
        experienceValue += 2;
    }
    
    /// Override to set the attack AI to use.
    @Override
    protected void initTypeAI() {
        setRangedAI( 1.0, 26, 80, 23.0F );
        setMeleeAI( 1.5 );
    }
    
    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        getSpecialData().addAttribute( SharedMonsterAttributes.followRange, 8.0 );
        getSpecialData().addAttribute( SharedMonsterAttributes.attackDamage, 2.0 );
        getSpecialData().arrowDamage += 2.0F;
        getSpecialData().arrowSpread -= 4.0F;
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        
        for( int i = rand.nextInt( 2 + looting ); i-- > 0; ) {
            dropItem( Items.feather, 1 );
        }
    }
}