package toast.specialMobs.entity.skeleton;

import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs.EnchantmentSpecial;
import toast.specialMobs._SpecialMobs;

public class EntityPoisonSkeleton extends Entity_SpecialSkeleton {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "skeleton/poison.png" ),
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "skeleton/poison_wither.png" )
    };
    
    public EntityPoisonSkeleton( World world ) {
        super( world );
        getSpecialData().setTextures( EntityPoisonSkeleton.TEXTURES );
        experienceValue += 1;
    }
    
    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        ItemStack itemStack = getHeldItem();
        if( itemStack != null ) {
            if( itemStack.getItem() instanceof ItemBow ) {
                if( EnchantmentSpecial.poisonBow != null ) {
                    EffectHelper.overrideEnchantment( itemStack, EnchantmentSpecial.poisonBow, rand.nextInt( EnchantmentSpecial.poisonBow.getMaxLevel() ) + 1 );
                }
            }
            else if( EnchantmentSpecial.poisonSword != null ) {
                EffectHelper.overrideEnchantment( itemStack, EnchantmentSpecial.poisonSword, rand.nextInt( EnchantmentSpecial.poisonSword.getMaxLevel() ) + 1 );
            }
        }
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        
        if( hit && (rand.nextInt( 3 ) == 0 || rand.nextInt( 1 + looting ) > 0) ) {
            dropItem( Items.spider_eye, 1 );
        }
    }
}