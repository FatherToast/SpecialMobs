package fathertoast.specialmobs.common.entity.projectile;

import fathertoast.specialmobs.common.core.register.SMEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class BoneShrapnelEntity extends AbstractArrowEntity {
    
    /** Ticks in ground remaining before de-spawning. */
    public int life = 150 + random.nextInt( 16 );
    
    public BoneShrapnelEntity( EntityType<? extends BoneShrapnelEntity> entityType, World world ) { super( entityType, world ); }
    
    //public BoneShrapnelEntity( World world, double x, double y, double z ) { super( SMEntities.BONE_SHRAPNEL.get(), x, y, z, world ); }
    
    public BoneShrapnelEntity( LivingEntity shooter ) { super( SMEntities.BONE_SHRAPNEL.get(), shooter, shooter.level ); }
    
    @Override
    public IPacket<?> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket( this ); }
    
    /** Called each tick this arrow is in the ground. */
    @Override
    protected void tickDespawn() {
        // Unlike classic arrows, shrapnel has a very short (and random) de-spawn time and does not get refreshed when disturbed
        if( life-- <= 0 ) remove();
    }
    
    /** Called when the player touches this entity. */
    @Override
    public void playerTouch( PlayerEntity player ) { }
    
    /** @return The item version of this arrow. */
    @Override
    protected ItemStack getPickupItem() { return new ItemStack( Items.BONE_MEAL ); }
    
    /** Saves data to this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void addAdditionalSaveData( CompoundNBT tag ) {
        super.addAdditionalSaveData( tag );
        tag.putShort( "life", (short) life );
    }
    
    /** Loads data from this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void readAdditionalSaveData( CompoundNBT tag ) {
        super.readAdditionalSaveData( tag );
        life = tag.getShort( "life" );
    }
}