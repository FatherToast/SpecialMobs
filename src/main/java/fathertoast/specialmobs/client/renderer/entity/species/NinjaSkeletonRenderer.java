package fathertoast.specialmobs.client.renderer.entity.species;

import com.mojang.blaze3d.vertex.PoseStack;
import fathertoast.specialmobs.client.renderer.entity.family.SpecialSkeletonRenderer;
import fathertoast.specialmobs.common.entity.ai.INinja;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class NinjaSkeletonRenderer extends SpecialSkeletonRenderer {
    
    private final BlockRenderDispatcher blockRenderer;
    
    public NinjaSkeletonRenderer( EntityRendererProvider.Context context ) {
        super( context );
        blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }
    
    @Override
    public void render(AbstractSkeleton entity, float rotation, float partialTicks,
                       PoseStack matrixStack, MultiBufferSource buffer, int packedLight ) {
        
        INinja ninja = (INinja) entity;
        final BlockState disguiseBlock = ninja.getHiddenDragon();
        
        if( disguiseBlock == null ) {
            super.render( entity, rotation, partialTicks, matrixStack, buffer, packedLight );
        }
        else {
            shadowRadius = 0.0F;
            renderBlockDisguise( disguiseBlock, entity.blockPosition(), entity.level(), matrixStack, buffer, entity.getRandom() );
        }
    }
    
    private void renderBlockDisguise( BlockState block, BlockPos pos, LevelReader displayReader, PoseStack poseStack, MultiBufferSource buffer, RandomSource random ) {
        poseStack.pushPose();
        poseStack.translate( -0.5, 0.0, -0.5 );
        blockRenderer.renderBatched( block, pos, displayReader, poseStack, buffer.getBuffer( RenderType.cutout() ), false, random, ModelData.EMPTY, null );
        poseStack.popPose();
    }
}