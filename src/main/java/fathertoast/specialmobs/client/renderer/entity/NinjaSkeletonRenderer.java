package fathertoast.specialmobs.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.MatrixApplyingVertexBuilder;
import com.mojang.blaze3d.vertex.VertexBuilderUtils;
import fathertoast.specialmobs.common.entity.ai.INinja;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.tileentity.ChestTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;
import java.util.SortedSet;

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

        INinja ninja = (INinja) entity;
        final BlockState disguiseBlock = ninja.getHiddenDragon();

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
        blockRenderer.renderModel(block, pos, displayReader, matrixStack, buffer.getBuffer(RenderType.cutout()), false, random, EmptyModelData.INSTANCE);
        matrixStack.popPose();
    }
}