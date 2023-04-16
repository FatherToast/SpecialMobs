package fathertoast.specialmobs.common.item;

import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.entity.MobHelper;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class SyringeItem extends Item {
    
    public SyringeItem() {
        super( new Item.Properties().stacksTo( 1 ).rarity( Rarity.UNCOMMON ).defaultDurability( 5 ).setNoRepair().tab( CreativeModeTab.TAB_MISC ) );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Syringe",
                "", "", "", "", "", "" );//TODO
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand ) {
        ItemStack usedItem = player.getItemInHand( hand );
        
        if( !level.isClientSide ) {
            if( player.getCooldowns().isOnCooldown( usedItem.getItem() ) ) {
                return InteractionResultHolder.pass( usedItem );
            }
            else {
                MobHelper.applyEffect( player, MobEffects.MOVEMENT_SPEED, 3, 300 );
                MobHelper.applyEffect( player, MobEffects.DOLPHINS_GRACE, 1, 300 );
                if( Config.MAIN.GENERAL.enableNausea.get() ) MobHelper.applyEffect( player, MobEffects.CONFUSION, 1, 400 );
                
                level.playSound( null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEE_STING, SoundSource.PLAYERS, 0.9F, 1.0F );
                usedItem.hurtAndBreak( 1, player, ( entity ) -> entity.broadcastBreakEvent( hand ) );
                if( !player.isCreative() ) {
                    player.getCooldowns().addCooldown( usedItem.getItem(), 1200 );
                }
                return InteractionResultHolder.success( usedItem );
            }
        }
        return InteractionResultHolder.fail( usedItem );
    }
    
    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand hand ) {
        if( livingEntity instanceof Creeper creeper && !creeper.isPowered() ) {
            if( player.getCooldowns().isOnCooldown( itemStack.getItem() ) ) {
                return InteractionResult.PASS;
            }
            
            MobHelper.charge( creeper );
            livingEntity.level.playSound( null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEE_STING, SoundSource.PLAYERS, 0.9F, 1.0F );
            itemStack.hurtAndBreak( 1, player, ( entity ) -> entity.broadcastBreakEvent( hand ) );
            if( !player.isCreative() ) {
                player.getCooldowns().addCooldown( itemStack.getItem(), 1200 );
            }
            return InteractionResult.sidedSuccess( player.level.isClientSide );
        }
        return InteractionResult.PASS;
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