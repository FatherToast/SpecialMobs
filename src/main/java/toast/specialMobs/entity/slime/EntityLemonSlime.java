package toast.specialMobs.entity.slime;

import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs.MobHelper;
import toast.specialMobs._SpecialMobs;

public class EntityLemonSlime extends Entity_SpecialSlime {
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "slime/lemon.png" )
    };
    
    public EntityLemonSlime( World world ) {
        super( world );
        getSpecialData().setTextures( EntityLemonSlime.TEXTURES );
        getSpecialData().isImmuneToFire = isImmuneToFire = true;
    }
    
    /// Gets the additional experience this slime type gives.
    @Override
    protected int getTypeXp() {
        return 2;
    }
    
    /// Overridden to modify attack effects.
    @Override
    protected void onTypeAttack( Entity target ) {
        MobHelper.lightningExplode( target, 0 );
        double vX = posX - target.posX;
        double vZ = posZ - target.posZ;
        double vH = Math.sqrt( vX * vX + vZ * vZ );
        motionX = vX / vH * 1.41 + motionX * 0.21;
        motionY = 0.41 * 1.41;
        motionZ = vZ / vH * 1.41 + motionZ * 0.21;
        onGround = false;
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        
        if( getSlimeSize() == 1 ) {
            for( int i = rand.nextInt( 2 + looting ); i-- > 0; ) {
                dropItem( Items.redstone, 1 );
            }
            if( hit && (rand.nextInt( 3 ) == 0 || rand.nextInt( 1 + looting ) > 0) ) {
                entityDropItem( new ItemStack( Items.dye, 1, 11 ), 0.0F );
            }
        }
    }
    
    /// Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop( int superRare ) {
        if( getSlimeSize() == 1 ) {
            ItemStack itemStack = new ItemStack( Items.potionitem, 1, 8195 );
            EffectHelper.setItemName( itemStack, "Potion of Haste", 0xf );
            EffectHelper.addPotionEffect( itemStack, Potion.digSpeed, 3600 / (superRare + 1), superRare );
            entityDropItem( itemStack, 0.0F );
        }
    }
}