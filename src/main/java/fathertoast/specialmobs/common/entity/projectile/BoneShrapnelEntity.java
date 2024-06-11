package fathertoast.specialmobs.common.entity.projectile;

import fathertoast.specialmobs.common.core.register.SMEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class BoneShrapnelEntity extends AbstractArrow {
    
    /** Ticks in ground remaining before de-spawning. */
    public int life = 150 + random.nextInt( 16 );
    
    public BoneShrapnelEntity(EntityType<? extends BoneShrapnelEntity> entityType, Level level ) { super( entityType, level ); }
    
    //public BoneShrapnelEntity( World world, double x, double y, double z ) { super( SMEntities.BONE_SHRAPNEL.get(), x, y, z, world ); }
    
    public BoneShrapnelEntity( LivingEntity shooter ) { super( SMEntities.BONE_SHRAPNEL.get(), shooter, shooter.level() ); }
    
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket( this ); }
    
    /** Called each tick this arrow is in the ground. */
    @Override
    protected void tickDespawn() {
        // Unlike classic arrows, shrapnel has a very short (and random) de-spawn time and does not get refreshed when disturbed
        if( life-- <= 0 ) discard();
    }
    
    /** Called when the player touches this entity. */
    @Override
    public void playerTouch( Player player ) { }
    
    /** @return The item version of this arrow. */
    @Override
    protected ItemStack getPickupItem() { return new ItemStack( Items.BONE_MEAL ); }
    
    /** Saves data to this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void addAdditionalSaveData( CompoundTag tag ) {
        super.addAdditionalSaveData( tag );
        tag.putShort( "life", (short) life );
    }
    
    /** Loads data from this entity's base NBT compound that is specific to its subclass. */
    @Override
    public void readAdditionalSaveData( CompoundTag tag ) {
        super.readAdditionalSaveData( tag );
        life = tag.getShort( "life" );
    }
}