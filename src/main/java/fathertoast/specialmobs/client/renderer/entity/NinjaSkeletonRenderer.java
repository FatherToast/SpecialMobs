package fathertoast.specialmobs.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import fathertoast.specialmobs.common.entity.ai.INinja;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@OnlyIn( Dist.CLIENT )
public class NinjaSkeletonRenderer extends SpecialSkeletonRenderer {

    private final BlockRendererDispatcher blockRenderer;
    
    public NinjaSkeletonRenderer( EntityRendererManager rendererManager ) {
        super( rendererManager );
        blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }
    
    @Override
    public void render( AbstractSkeletonEntity entity, float rotation, float partialTicks,
                        MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight ) {
        final BlockState disguiseBlock = ((INinja) entity).getHiddenDragon();
        if( disguiseBlock == null ) {
            super.render( entity, rotation, partialTicks, matrixStack, buffer, packedLight );
        }
        else {
            shadowRadius = 0.0F;
            renderBlockDisguise( disguiseBlock, entity.blockPosition(), entity.level, matrixStack, buffer, entity.getRandom() );
        }
    }
    
    private void renderBlockDisguise( BlockState block, BlockPos pos, IBlockDisplayReader displayReader, MatrixStack matrixStack, IRenderTypeBuffer buffer, Random random ) {
        matrixStack.pushPose();
        
        matrixStack.translate( -0.5, 0.0, -0.5 );
        blockRenderer.renderModel( block, pos, displayReader, matrixStack, buffer.getBuffer(RenderType.cutout()), false, random, EmptyModelData.INSTANCE );
        
        matrixStack.popPose();
    }
}