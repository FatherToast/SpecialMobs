package fathertoast.specialmobs.common.item;

import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.projectile.IncorporealFireballEntity;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class IncorporealFireChargeItem extends Item {
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Incorporeal Fire Charge",
                "", "", "", "", "", "" );//TODO
    }
    
    public IncorporealFireChargeItem() { super( new Item.Properties() ); }
    
    @Override
    public InteractionResultHolder<ItemStack> use( Level level, Player player, InteractionHand hand ) {
        final ItemStack item = player.getItemInHand( hand );
        if( player.getCooldowns().isOnCooldown( item.getItem() ) ) return InteractionResultHolder.pass( item );
        
        final Entity target = pickEntity( player, 127.0 );
        if( target instanceof LivingEntity) {
            if( !level.isClientSide() ) {
                level.addFreshEntity( new IncorporealFireballEntity( level, player, (LivingEntity) target,
                        player.getX(), player.getEyeY(), player.getZ() ) );
                level.playSound( null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F );
                
                if( !player.getAbilities().instabuild ) {
                    item.shrink( 1 );
                }
                player.getCooldowns().addCooldown( item.getItem(), 10 );
                return InteractionResultHolder.consume( item );
            }
            return InteractionResultHolder.pass( item );
        }
        
        return InteractionResultHolder.fail( item );
    }
    
    @Nullable
    private static Entity pickEntity( Player player, double range ) {
        final Vec3 eyePos = player.getEyePosition( 1.0F );
        final Vec3 viewVec = player.getViewVector( 1.0F ).scale( range );
        
        final AABB box = player.getBoundingBox().expandTowards( viewVec ).inflate( 1.0 );
        final EntityHitResult result = ProjectileUtil.getEntityHitResult( player.level(), player, eyePos, eyePos.add( viewVec ), box,
                ( entity ) -> !entity.isSpectator() && entity.isAlive() && entity.isPickable() && !player.isPassengerOfSameVehicle( entity ) );
        
        return result == null ? null : result.getEntity();
    }
}