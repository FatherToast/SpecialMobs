package toast.specialMobs;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.IMob;
import net.minecraft.nbt.NBTTagCompound;
import toast.specialMobs.entity.ISpecialMob;

public class ReplacementEntry {
    
    public EntityLiving entity;
    
    public ReplacementEntry( EntityLiving e ) {
        this.entity = e;
    }
    
    // Replaces the entity with its replacement.
    public void replace() {
        EntityLiving replacement;
        
        if( entity instanceof IMob ) {
            entity.getEntityData().setByte( "smi", (byte) 1 );
            int key = _SpecialMobs.monsterKey( EntityList.getEntityString( entity ) );
            
            if( key < 0 || !Properties.monsterSpawn()[key] )
                return;
            
            replacement = RandomHelper.nextMonster( key, entity.worldObj );
            
            if( replacement == null )
                return;
        }
        else
            return;
        
        NBTTagCompound entityData = new NBTTagCompound();
        entity.writeToNBT( entityData );
        replacement.readFromNBT( entityData );
        replacement.getEntityData().setByte( "smi", (byte) 1 );
        replacement.copyLocationAndAnglesFrom( entity );
        
        if( entity.ridingEntity != null ) {
            Entity riding = entity.ridingEntity;
            entity.mountEntity( null );
            replacement.mountEntity( riding );
        }
        if( entity.riddenByEntity != null ) {
            Entity rider = entity.riddenByEntity;
            rider.mountEntity( null );
            rider.mountEntity( replacement );
        }
        
        if( replacement instanceof ISpecialMob ) {
            ((ISpecialMob) replacement).adjustEntityAttributes();
        }
        replacement.worldObj.spawnEntityInWorld( replacement );
        entity.setDead();
    }
}
