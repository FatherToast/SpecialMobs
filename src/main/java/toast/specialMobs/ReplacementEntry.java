package toast.specialMobs;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.IMob;
import net.minecraft.nbt.NBTTagCompound;
import toast.specialMobs.entity.ISpecialMob;

public class ReplacementEntry
{
    public EntityLiving entity = null;

    public ReplacementEntry(EntityLiving e) {
        this.entity = e;
    }

    // Replaces the entity with its replacement.
    public void replace() {
        EntityLiving replacement;
        if (this.entity instanceof IMob) {
            this.entity.getEntityData().setByte("smi", (byte) 1);
            int key = _SpecialMobs.monsterKey(EntityList.getEntityString(this.entity));
            if (key < 0 || !Properties.monsterSpawn()[key])
                return;
            replacement = RandomHelper.nextMonster(key, this.entity.worldObj);
            if (replacement == null)
                return;
        }
        else
            return;

        NBTTagCompound entityData = new NBTTagCompound();
        this.entity.writeToNBT(entityData);
        replacement.readFromNBT(entityData);
        if (replacement instanceof ISpecialMob) {
            ((ISpecialMob) replacement).adjustEntityAttributes();
        }
        replacement.getEntityData().setByte("smi", (byte) 1);
        replacement.copyLocationAndAnglesFrom(this.entity);

        if (this.entity.riddenByEntity != null) {
            this.entity.riddenByEntity.setPosition(replacement.posX, replacement.posY, replacement.posZ);
            this.entity.riddenByEntity.mountEntity(replacement);
        }
        replacement.mountEntity(this.entity.ridingEntity);
        replacement.worldObj.spawnEntityInWorld(replacement);
        this.entity.setDead();
    }
}
