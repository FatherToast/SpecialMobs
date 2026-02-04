package toast.specialMobs.entity.blaze;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityWildfireBlaze extends Entity_SpecialBlaze {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "blaze/wildfire.png" )
    };
    
    /// The number of cinders this blaze can spawn.
    public byte babyCount;
    /// The number of cinders spawned on death.
    @SuppressWarnings( "FieldMayBeFinal" )
    private byte babies;
    
    public EntityWildfireBlaze( World world ) {
        super( world );
        setSize( 0.9F, 2.7F );
        getSpecialData().setTextures( EntityWildfireBlaze.TEXTURES );
        getSpecialData().resetRenderScale( 1.5F );
        experienceValue += 2;
        babyCount = (byte) (rand.nextInt( 7 ) + 4);
        babies = (byte) (3 + rand.nextInt( 4 ));
    }
    
    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        getSpecialData().addAttribute( SharedMonsterAttributes.maxHealth, 20.0 );
        setRangedAI( 1, 0, 30, 50, 20.0F );
        getSpecialData().arrowSpread *= 0.1F;
    }
    
    // Called to attack the target entity with a fireball.
    @Override
    public void shootFireballAtEntity( Entity target, float distance ) {
        if( babyCount > 0 && rand.nextInt( 3 ) != 0 ) {
            babyCount--;
            EntityCinderBlaze baby = new EntityCinderBlaze( worldObj );
            baby.copyLocationAndAnglesFrom( this );
            baby.onSpawnWithEgg( null );
            baby.setTarget( getEntityToAttack() );
            worldObj.spawnEntityInWorld( baby );
            worldObj.playAuxSFXAtEntity( null, 1009, (int) posX, (int) posY, (int) posZ, 0 );
        }
        else {
            super.shootFireballAtEntity( target, distance );
        }
    }
    
    /// Overridden to modify attack effects.
    @Override
    protected void onTypeAttack( Entity target ) {
        target.setFire( 8 );
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        if( hit ) {
            for( int i = rand.nextInt( 2 + looting ); i-- > 0; ) {
                dropItem( Items.coal, 1 );
            }
            if( rand.nextInt( 3 ) == 0 || rand.nextInt( 1 + looting ) > 0 ) {
                entityDropItem( new ItemStack( Items.spawn_egg, 1, EntityList.getEntityID( new EntityBlaze( worldObj ) ) ), 0.0F );
            }
        }
        
        if( !worldObj.isRemote ) {
            EntityCinderBlaze baby = null;
            for( int i = babies; i-- > 0; ) {
                baby = new EntityCinderBlaze( worldObj );
                baby.copyLocationAndAnglesFrom( this );
                baby.onSpawnWithEgg( null );
                baby.setTarget( getEntityToAttack() );
                worldObj.spawnEntityInWorld( baby );
            }
            if( baby != null ) {
                worldObj.playAuxSFXAtEntity( null, 1009, (int) posX, (int) posY, (int) posZ, 0 );
                baby.spawnExplosionParticle();
            }
        }
    }
}