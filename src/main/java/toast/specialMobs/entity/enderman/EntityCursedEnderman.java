package toast.specialMobs.entity.enderman;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs.EffectHelper;
import toast.specialMobs._SpecialMobs;

public class EntityCursedEnderman extends Entity_SpecialEnderman {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "enderman/cursed.png" ),
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "enderman/cursed_eyes.png" )
    };
    
    public EntityCursedEnderman( World world ) {
        super( world );
        getSpecialData().setTextures( EntityCursedEnderman.TEXTURES );
        experienceValue += 1;
    }
    
    /// Overridden to modify inherited attributes.
    @Override
    public void adjustTypeAttributes() {
        getSpecialData().addAttribute( SharedMonsterAttributes.maxHealth, 20.0 );
    }
    
    /// Overridden to modify attack effects.
    @Override
    protected void onTypeAttack( Entity target ) {
        if( target instanceof EntityLivingBase ) {
            int time;
            switch( target.worldObj.difficultySetting ) {
                case PEACEFUL:
                    return;
                case EASY:
                    time = 20;
                    break;
                case NORMAL:
                    time = 50;
                    break;
                default:
                    time = 120;
            }
            time *= 20;
            EffectHelper.stackEffect( (EntityLivingBase) target, Potion.weakness, time, 0, 4 );
            EffectHelper.stackEffect( (EntityLivingBase) target, Potion.digSlowdown, time, 0, 4 );
        }
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        if( hit && (rand.nextInt( 3 ) == 0 || rand.nextInt( 1 + looting ) > 0) ) {
            Item drop = null;
            switch( rand.nextInt( 5 ) ) {
                case 0:
                    drop = Items.gunpowder;
                    break;
                case 1:
                    drop = Items.sugar;
                    break;
                case 2:
                    drop = Items.spider_eye;
                    break;
                case 3:
                    drop = Items.fermented_spider_eye;
                    break;
                case 4:
                    drop = Items.speckled_melon;
                    break;
            }
            if( drop != null ) {
                dropItem( drop, 1 );
            }
        }
    }
}