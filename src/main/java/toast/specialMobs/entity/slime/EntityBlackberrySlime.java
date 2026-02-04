package toast.specialMobs.entity.slime;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;

public class EntityBlackberrySlime extends Entity_SpecialSlime {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "slime/blackberry.png" )
    };
    
    private int fuseTime = 0;
    
    public EntityBlackberrySlime( World world ) {
        super( world );
        getSpecialData().setTextures( EntityBlackberrySlime.TEXTURES );
    }
    
    /// Gets the additional experience this slime type gives.
    @Override
    protected int getTypeXp() {
        return 2;
    }
    
    /// Overridden to modify inherited max health.
    @Override
    protected void adjustHealthAttribute() {
        getSpecialData().addAttribute( SharedMonsterAttributes.maxHealth, 6.0 );
    }
    
    /// Called each tick to update behavior.
    @Override
    protected void attackEntityByType( EntityPlayer target ) {
        float explosionPower = getSlimeSize();
        if( target != null && target.getDistanceSqToEntity( this ) < 9.0F + (explosionPower - 1.0F) * 2.0F ) {
            if( fuseTime == 0 ) {
                playSound( "creeper.primed", 1.0F, 0.5F );
            }
            else if( fuseTime >= 30 ) {
                worldObj.createExplosion( this, posX, posY, posZ, explosionPower, worldObj.getGameRules().getGameRuleBooleanValue( "mobGriefing" ) );
                setDead();
            }
            getSpecialData().setRenderScale( getSpecialData().getRenderScale() + 0.013F );
            fuseTime++;
            moveStrafing = moveForward = 0.0F;
            onGround = false;
        }
        else if( fuseTime > 0 ) {
            fuseTime--;
            getSpecialData().setRenderScale( getSpecialData().getRenderScale() - 0.013F );
            moveStrafing = moveForward = 0.0F;
            onGround = false;
        }
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        if( getSlimeSize() == 1 ) {
            for( int i = rand.nextInt( 3 ) + looting; i-- > 0; ) {
                dropItem( Items.gunpowder, 1 );
            }
            if( hit && (rand.nextInt( 3 ) == 0 || rand.nextInt( 1 + looting ) > 0) ) {
                dropItem( Items.dye, 1 );
            }
        }
    }
    
    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT( NBTTagCompound tag ) {
        super.writeEntityToNBT( tag );
        tag.setByte( "FuseTime", (byte) fuseTime );
    }
    
    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT( NBTTagCompound tag ) {
        super.readEntityFromNBT( tag );
        if( tag.hasKey( "FuseTime" ) ) {
            fuseTime = tag.getByte( "FuseTime" );
        }
    }
}