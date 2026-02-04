package toast.specialMobs.entity.creeper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

import java.util.List;

public class EntityGravityCreeper extends Entity_SpecialCreeper {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "creeper/gravity.png" )
    };
    
    public EntityGravityCreeper( World world ) {
        super( world );
        getSpecialData().setTextures( EntityGravityCreeper.TEXTURES );
        getSpecialData().isImmuneToFalling = true;
        getSpecialData().ignorePressurePlates = true;
        getSpecialData().immuneToPotions.add( Potion.jump.id );
        experienceValue += 1;
    }
    
    // Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        getSpecialData().addAttribute( SharedMonsterAttributes.maxHealth, 20.0 );
    }
    
    @Override
    public void onExplodingUpdate() {
        if( !worldObj.isRemote ) {
            boolean powered = getPowered();
            float radius = powered ? explosionRadius * 3.0F : explosionRadius * 1.5F;
            Entity entityHit;
            double vX, vZ, v;
            List entitiesInRange = worldObj.getEntitiesWithinAABBExcludingEntity( this, boundingBox.expand( radius * 2.0, radius * 2.0, radius * 2.0 ) );
            
            for( Object o : entitiesInRange ) {
                entityHit = (Entity) o;
                
                if( getDistanceSqToEntity( entityHit ) <= radius * radius ) {
                    vX = posX - entityHit.posX;
                    vZ = posZ - entityHit.posZ;
                    v = Math.sqrt( vX * vX + vZ * vZ );
                    entityHit.motionX = vX * radius * 0.05 / (v * v);
                    entityHit.motionZ = vZ * radius * 0.05 / (v * v);
                    entityHit.onGround = false;
                    
                    if( entityHit instanceof EntityPlayerMP ) {
                        try {
                            ((EntityPlayerMP) entityHit).playerNetServerHandler.sendPacket( new S12PacketEntityVelocity( entityHit ) );
                        }
                        catch( Exception ex ) {
                            // noinspection all
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    
    // The explosion caused by this creeper.
    @Override
    public void explodeByType( boolean powered, boolean griefing ) {
        float power = powered ? (explosionRadius + 2) * 2.0F : (float) (explosionRadius + 2);
        worldObj.createExplosion( this, posX, posY, posZ, power, griefing );
    }
    
    // Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        for( int i = rand.nextInt( 3 + looting ); i-- > 0; ) {
            dropItem( Items.gunpowder, 1 );
        }
        if( hit && (rand.nextInt( 3 ) == 0 || rand.nextInt( 1 + looting ) > 0) ) {
            dropItem( Items.gold_nugget, 1 );
        }
    }
    
    // Called 2.5% of the time when this entity is killed. 20% chance that superRare == 1, otherwise superRare == 0.
    @Override
    protected void dropRareDrop( int superRare ) {
        dropItem( Items.apple, 1 );
    }
}