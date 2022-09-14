package fathertoast.specialmobs.common.item;

import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.entity.projectile.IncorporealFireballEntity;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class IncorporealFireChargeItem extends Item {
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Incorporeal Fire Charge",
                "", "", "", "", "", "" );//TODO
    }
    
    public IncorporealFireChargeItem() { super( new Item.Properties().tab( ItemGroup.TAB_MISC ) ); }
    
    @Override
    public ActionResult<ItemStack> use( World world, PlayerEntity player, Hand hand ) {
        final ItemStack item = player.getItemInHand( hand );
        if( player.getCooldowns().isOnCooldown( item.getItem() ) ) return ActionResult.pass( item );
        
        final Entity target = pickEntity( player, 127.0 );
        if( target instanceof LivingEntity ) {
            if( !world.isClientSide() ) {
                world.addFreshEntity( new IncorporealFireballEntity( world, player, (LivingEntity) target,
                        player.getX(), player.getEyeY(), player.getZ() ) );
                world.playSound( null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F );
                
                if( !player.abilities.instabuild ) {
                    item.shrink( 1 );
                }
                player.getCooldowns().addCooldown( item.getItem(), 10 );
                return ActionResult.consume( item );
            }
            return ActionResult.pass( item );
        }
        
        return ActionResult.fail( item );
    }
    
    @Nullable
    private static Entity pickEntity( PlayerEntity player, double range ) {
        final Vector3d eyePos = player.getEyePosition( 1.0F );
        final Vector3d viewVec = player.getViewVector( 1.0F ).scale( range );
        
        final AxisAlignedBB bb = player.getBoundingBox().expandTowards( viewVec ).inflate( 1.0 );
        final EntityRayTraceResult result = ProjectileHelper.getEntityHitResult( player.level, player, eyePos, eyePos.add( viewVec ), bb,
                ( entity ) -> !entity.isSpectator() && entity.isAlive() && entity.isPickable() && !player.isPassengerOfSameVehicle( entity ) );
        
        return result == null ? null : result.getEntity();
    }
}