package toast.specialMobs.entity.spider;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityToughSpider extends Entity_SpecialSpider {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "spider/tough.png" ),
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "spider/tough_eyes.png" )
    };
    
    public EntityToughSpider( World world ) {
        super( world );
        setSize( 1.5F, 1.0F );
        getSpecialData().setTextures( EntityToughSpider.TEXTURES );
        getSpecialData().resetRenderScale( 1.2F );
        experienceValue += 2;
    }
    
    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        getSpecialData().addAttribute( SharedMonsterAttributes.maxHealth, 16.0 );
        getSpecialData().addAttribute( SharedMonsterAttributes.attackDamage, 2.0 );
        getSpecialData().multAttribute( SharedMonsterAttributes.movementSpeed, 0.7 );
        getSpecialData().armor += 20;
        getSpecialData().arrowDamage += 1.0F;
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        
        for( int i = rand.nextInt( 2 + looting ); i-- > 0; ) {
            dropItem( Items.flint, 1 );
        }
    }
    
    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop( int superRare ) {
        dropItem( Items.iron_ingot, 1 );
    }
}