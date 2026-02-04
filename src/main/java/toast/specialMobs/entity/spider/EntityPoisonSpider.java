package toast.specialMobs.entity.spider;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import toast.specialMobs.entity.cavespider.Entity_SpecialCaveSpider;

public class EntityPoisonSpider extends Entity_SpecialSpider {
    
    public EntityPoisonSpider( World world ) {
        super( world );
        getSpecialData().setTextures( Entity_SpecialCaveSpider.TEXTURES );
        experienceValue += 1;
    }
    
    /// Overridden to modify attack effects.
    @Override
    public void onTypeAttack( Entity target ) {
        if( target instanceof EntityLivingBase ) {
            int time;
            switch( target.worldObj.difficultySetting ) {
                case PEACEFUL:
                    return;
                case EASY:
                    time = 3;
                    break;
                case NORMAL:
                    time = 7;
                    break;
                default:
                    time = 15;
            }
            time *= 20;
            ((EntityLivingBase) target).addPotionEffect( new PotionEffect( Potion.poison.id, time, 0 ) );
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