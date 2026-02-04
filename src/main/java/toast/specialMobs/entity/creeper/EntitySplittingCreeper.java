package toast.specialMobs.entity.creeper;

import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.SpecialMobData;

public class EntitySplittingCreeper extends Entity_SpecialCreeper {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "creeper/splitting.png" )
    };
    
    /// The number of extra mini creepers spawned on explosion.
    private byte babies;
    
    public EntitySplittingCreeper( World world ) {
        super( world );
        getSpecialData().setTextures( EntitySplittingCreeper.TEXTURES );
        setExplodesWhenShot( true );
        experienceValue += 2;
        babies = (byte) rand.nextInt( 4 );
    }
    
    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        getSpecialData().addAttribute( SharedMonsterAttributes.maxHealth, 20.0 );
    }
    
    /// The explosion caused by this creeper.
    @Override
    public void explodeByType( boolean powered, boolean griefing ) {
        float power = powered ? explosionRadius * 2.0F : (float) explosionRadius;
        worldObj.createExplosion( this, posX, posY, posZ, power - 1.0F, false );
        if( !worldObj.isRemote ) {
            EntityMiniCreeper baby = null;
            for( int i = babies + (int) power; i-- > 0; ) {
                baby = new EntityMiniCreeper( worldObj );
                baby.copyLocationAndAnglesFrom( this );
                baby.setAttackTarget( getAttackTarget() );
                baby.onSpawnWithEgg( (IEntityLivingData) null );
                baby.motionX = (rand.nextDouble() - 0.5) * power / 3.0;
                ///baby.motionY = 0.3 + 0.3 * rand.nextDouble(); Causes floor clip bug
                baby.motionZ = (rand.nextDouble() - 0.5) * power / 3.0;
                baby.onGround = false;
                if( powered ) {
                    baby.getDataWatcher().updateObject( 17, (byte) 1 );
                }
                worldObj.spawnEntityInWorld( baby );
            }
            worldObj.playSoundAtEntity( baby, "random.pop", 1.0F, 2.0F / (rand.nextFloat() * 0.4F + 0.8F) );
        }
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        
        for( int i = rand.nextInt( 3 + looting ); i-- > 0; ) {
            dropItem( Items.gunpowder, 1 );
        }
    }
    
    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT( NBTTagCompound tag ) {
        super.writeEntityToNBT( tag );
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
        saveTag.setByte( "Babies", babies );
    }
    
    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT( NBTTagCompound tag ) {
        super.readEntityFromNBT( tag );
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
        if( saveTag.hasKey( "Babies" ) ) {
            babies = saveTag.getByte( "Babies" );
        }
        else if( tag.hasKey( "Babies" ) ) {
            babies = tag.getByte( "Babies" );
        }
    }
}