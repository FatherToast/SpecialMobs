package fathertoast.specialmobs.common.item;

import fathertoast.specialmobs.common.bestiary.SpecialMob;
import fathertoast.specialmobs.common.block.SlabFireBlock;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;

public class SlabFireChargeItem extends FireChargeItem {
    
    public SlabFireChargeItem() {
        super( new Item.Properties() );
    }
    
    @SpecialMob.LanguageProvider
    public static String[] getTranslations( String langKey ) {
        return References.translations( langKey, "Slab Fire Charge",
                "", "", "", "", "", "" );//TODO
    }
    
    @Override
    public Rarity getRarity( ItemStack itemStack ) {
        return Rarity.UNCOMMON;
    }
    
    @Override
    public InteractionResult useOn( UseOnContext context ) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState( pos );
        boolean used = false;
        
        // Check specific interactions for lightable blocks.
        // If all checks fail, place a slab fire block. Otherwise, light the block.
        if( !CampfireBlock.canLight( state ) && !CandleBlock.canLight( state ) && !CandleCakeBlock.canLight( state ) ) {
            pos = pos.relative( context.getClickedFace() );
            if( BaseFireBlock.canBePlacedAt( level, pos, context.getHorizontalDirection() ) ) {
                playSound( level, pos );
                level.setBlockAndUpdate( pos, SlabFireBlock.getState( level, pos ) );
                level.gameEvent( context.getPlayer(), GameEvent.BLOCK_PLACE, pos );
                used = true;
            }
        }
        else {
            playSound( level, pos );
            level.setBlockAndUpdate( pos, state.setValue( BlockStateProperties.LIT, true ) );
            level.gameEvent( context.getPlayer(), GameEvent.BLOCK_CHANGE, pos );
            used = true;
        }
        
        // Only shrink stack if successfully used
        if( used ) {
            context.getItemInHand().shrink( 1 );
            return InteractionResult.sidedSuccess( level.isClientSide );
        }
        else return InteractionResult.FAIL;
    }
}
