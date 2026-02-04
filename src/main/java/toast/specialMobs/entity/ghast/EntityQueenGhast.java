package toast.specialMobs.entity.ghast;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import toast.specialMobs._SpecialMobs;
import toast.specialMobs.entity.SpecialMobData;

public class EntityQueenGhast extends Entity_SpecialGhast {
    
    @SuppressWarnings( "hiding" )
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "ghast/queen.png" ),
            new ResourceLocation( _SpecialMobs.TEXTURE_PATH + "ghast/queen_shooting.png" )
    };
    
    /// The number of babies this ghast can spawn.
    public byte babyCount;
    /// The number of babies spawned on death.
    private byte babies;
    
    public EntityQueenGhast( World world ) {
        super( world );
        setSize( 5.0F, 5.0F );
        getSpecialData().setTextures( EntityQueenGhast.TEXTURES );
        getSpecialData().resetRenderScale( 1.25F );
        experienceValue += 2;
        babyCount = (byte) (rand.nextInt( 7 ) + 4);
        babies = (byte) (3 + rand.nextInt( 4 ));
    }
    
    /// Overridden to modify inherited attribites.
    @Override
    protected void adjustTypeAttributes() {
        getSpecialData().addAttribute( SharedMonsterAttributes.maxHealth, 30.0 );
        getSpecialData().addAttribute( SharedMonsterAttributes.attackDamage, 4.0 );
        getSpecialData().multAttribute( SharedMonsterAttributes.movementSpeed, 0.3 );
        getSpecialData().setHealTime( 20 );
        getSpecialData().armor += 6;
    }
    
    /// Called to attack the target entity with a fireball.
    @Override
    public void shootFireballAtEntity( Entity target ) {
        if( babyCount > 0 && rand.nextInt( 3 ) != 0 ) {
            babyCount--;
            EntityBabyGhast baby = new EntityBabyGhast( worldObj );
            baby.copyLocationAndAnglesFrom( this );
            baby.targetedEntity = targetedEntity;
            baby.onSpawnWithEgg( null );
            worldObj.spawnEntityInWorld( baby );
            worldObj.playSoundAtEntity( baby, "random.pop", 1.0F, 2.0F / (rand.nextFloat() * 0.4F + 0.8F) );
        }
        else {
            super.shootFireballAtEntity( target );
        }
    }
    
    /// Called when this entity is killed.
    @Override
    protected void dropFewItems( boolean hit, int looting ) {
        super.dropFewItems( hit, looting );
        for( int i = rand.nextInt( 2 + looting ); i-- > 0; ) {
            dropItem( Items.gold_ingot, 1 );
        }
        if( hit && (rand.nextInt( 3 ) == 0 || rand.nextInt( 1 + looting ) > 0) ) {
            dropItem( Items.emerald, 1 );
        }
        if( hit && (rand.nextInt( 3 ) == 0 || rand.nextInt( 1 + looting ) > 0) ) {
            entityDropItem( new ItemStack( Items.spawn_egg, 1, EntityList.getEntityID( new EntityGhast( worldObj ) ) ), 0.0F );
        }
        
        if( !worldObj.isRemote ) {
            EntityBabyGhast baby = null;
            for( int i = babies; i-- > 0; ) {
                baby = new EntityBabyGhast( worldObj );
                baby.copyLocationAndAnglesFrom( this );
                baby.targetedEntity = targetedEntity;
                baby.onSpawnWithEgg( null );
                worldObj.spawnEntityInWorld( baby );
            }
            if( baby != null ) {
                worldObj.playSoundAtEntity( baby, "random.pop", 1.0F, 2.0F / (rand.nextFloat() * 0.4F + 0.8F) );
                baby.spawnExplosionParticle();
            }
        }
    }
    
    /// Saves this entity to NBT.
    @Override
    public void writeEntityToNBT( NBTTagCompound tag ) {
        super.writeEntityToNBT( tag );
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
        saveTag.setByte( "BabyCount", babyCount );
        saveTag.setByte( "Babies", babies );
    }
    
    /// Reads this entity from NBT.
    @Override
    public void readEntityFromNBT( NBTTagCompound tag ) {
        super.readEntityFromNBT( tag );
        NBTTagCompound saveTag = SpecialMobData.getSaveLocation( tag );
        if( saveTag.hasKey( "BabyCount" ) ) {
            babyCount = saveTag.getByte( "BabyCount" );
        }
        else if( tag.hasKey( "BabyCount" ) ) {
            babyCount = tag.getByte( "BabyCount" );
        }
        if( saveTag.hasKey( "Babies" ) ) {
            babies = saveTag.getByte( "Babies" );
        }
        else if( tag.hasKey( "Babies" ) ) {
            babies = tag.getByte( "Babies" );
        }
    }
}