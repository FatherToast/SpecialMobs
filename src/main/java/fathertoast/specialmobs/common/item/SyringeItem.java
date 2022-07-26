package fathertoast.specialmobs.common.item;

import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SyringeItem extends Item {
    
    public SyringeItem() {
        super( new Item.Properties().stacksTo( 1 ).rarity( Rarity.UNCOMMON ).defaultDurability( 5 ).setNoRepair().tab( ItemGroup.TAB_MISC ) );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Syringe",
                "", "", "", "", "", "" );//TODO
    }
    
    @Override
    public ActionResult<ItemStack> use( World world, PlayerEntity player, Hand hand ) {
        ItemStack usedItem = player.getItemInHand( hand );
        
        if( !world.isClientSide ) {
            if( player.getCooldowns().isOnCooldown( usedItem.getItem() ) ) {
                return ActionResult.pass( usedItem );
            }
            else {
                MobHelper.applyEffect( player, Effects.MOVEMENT_SPEED, 3, 300 );
                MobHelper.applyEffect( player, Effects.DOLPHINS_GRACE, 1, 300 );
                if( Config.MAIN.GENERAL.enableNausea.get() ) MobHelper.applyEffect( player, Effects.CONFUSION, 1, 400 );
                
                world.playSound( null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEE_STING, SoundCategory.PLAYERS, 0.9F, 1.0F );
                usedItem.hurtAndBreak( 1, player, ( entity ) -> entity.broadcastBreakEvent( hand ) );
                if( !player.isCreative() ) {
                    player.getCooldowns().addCooldown( usedItem.getItem(), 1200 );
                }
                return ActionResult.success( usedItem );
            }
        }
        return ActionResult.fail( usedItem );
    }
    
    @Override
    public ActionResultType interactLivingEntity( ItemStack itemStack, PlayerEntity player, LivingEntity livingEntity, Hand hand ) {
        if( livingEntity instanceof CreeperEntity && !((CreeperEntity) livingEntity).isPowered() ) {
            if( player.getCooldowns().isOnCooldown( itemStack.getItem() ) ) {
                return ActionResultType.PASS;
            }
            
            MobHelper.charge( (CreeperEntity) livingEntity );
            livingEntity.level.playSound( null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEE_STING, SoundCategory.PLAYERS, 0.9F, 1.0F );
            itemStack.hurtAndBreak( 1, player, ( entity ) -> entity.broadcastBreakEvent( hand ) );
            if( !player.isCreative() ) {
                player.getCooldowns().addCooldown( itemStack.getItem(), 1200 );
            }
            return ActionResultType.sidedSuccess( player.level.isClientSide );
        }
        return ActionResultType.PASS;
    }
    
    // Not enchantable
    @Override
    public boolean isEnchantable( ItemStack itemStack ) {
        return false;
    }
    
    // Very not enchantable
    @Override
    public boolean isBookEnchantable( ItemStack stack, ItemStack book ) {
        return false;
    }
    
    // Really truly MEGA not enchantable
    @SuppressWarnings( "unused" )
    public boolean isEvenALittleBitEnchantable( ItemStack stack, Supplier<Supplier<Object>> objectSupplierSupplier ) {
        return false;
    }
}