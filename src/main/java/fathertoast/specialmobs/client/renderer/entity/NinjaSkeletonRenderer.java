package fathertoast.specialmobs.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import fathertoast.specialmobs.client.NinjaModelDataHolder;
import fathertoast.specialmobs.common.entity.ai.INinja;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import org.apache.commons.lang3.ThreadUtils;

import javax.annotation.ParametersAreNonnullByDefault;

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
            renderBlockDisguise( entity.getId(), disguiseBlock, matrixStack, buffer, packedLight );
        }
    }
    
    private void renderBlockDisguise( int entityId, BlockState block, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight ) {
        matrixStack.pushPose();
        
        matrixStack.translate( -0.5, 0.0, -0.5 );
        IModelData modelData = NinjaModelDataHolder.getModelData( entityId );
        blockRenderer.renderBlock( block, matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, modelData );
        
        matrixStack.popPose();
    }
}