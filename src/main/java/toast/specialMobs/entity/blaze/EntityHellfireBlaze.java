package toast.specialMobs.entity.blaze;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityHellfireBlaze extends Entity_SpecialBlaze {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "blaze/hellfire.png" )
    };
    
    /// The base explosion strength of this blaze's fireballs.
    public int explosionStrength = 2;
    
    public EntityHellfireBlaze( World world ) {
        super( world );
        getSpecialData().setTextures( EntityHellfireBlaze.TEXTURES );
        experienceValue += 1;
    }
    
    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        getSpecialData().addAttribute( SharedMonsterAttributes.maxHealth, 10.0 );
        setRangedAI( 1, 0, 60, 100, 40.0F );
        getSpecialData().arrowSpread = 0.0F;
    }
    
    // Called to attack the target entity with a fireball.
    @Override
    public void shootFireballAtEntity( Entity target, float distance ) {
        double dX = target.posX - posX;
        double dY = target.boundingBox.minY + target.height / 2.0F - posY - height / 2.0F;
        double dZ = target.posZ - posZ;
        float spread = (float) Math.sqrt( distance ) * getSpecialData().arrowSpread;
        worldObj.playAuxSFXAtEntity( null, 1009, (int) posX, (int) posY, (int) posZ, 0 );
        EntityLargeFireball fireball = new EntityLargeFireball( worldObj, this, dX + rand.nextGaussian() * spread, dY, dZ + rand.nextGaussian() * spread );
        fireball.field_92057_e = explosionStrength;
        fireball.posY = posY + height / 2.0F + 0.5;
        worldObj.spawnEntityInWorld( fireball );
    }
    
    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT( NBTTagCompound tag ) {
        super.writeEntityToNBT( tag );
        tag.setInteger( "ExplosionPower", explosionStrength );
    }
    
    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT( NBTTagCompound tag ) {
        super.readEntityFromNBT( tag );
        if( tag.hasKey( "ExplosionPower" ) ) {
            explosionStrength = tag.getInteger( "ExplosionPower" );
        }
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        
        if( hit ) {
            for( int i = rand.nextInt( 3 + looting ); i-- > 0; ) {
                dropItem( Items.gunpowder, 1 );
            }
        }
    }
}