package fathertoast.specialmobs.client.misc;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class SMRenderTypes {

    public static RenderState.AlphaState INCORPOREAL_ALPHA = new RenderState.AlphaState(0.05F);

    public static RenderType entityCutoutNoCullBlend(ResourceLocation resourceLocation, RenderState.AlphaState alphaState) {
        RenderType.State state = RenderType.State.builder()
                .setTextureState(new RenderState.TextureState(resourceLocation, false, false))
                .setTransparencyState(RenderState.TRANSLUCENT_TRANSPARENCY)
                .setDiffuseLightingState(RenderState.DIFFUSE_LIGHTING)
                .setAlphaState(alphaState)
                .setLightmapState(RenderState.LIGHTMAP)
                .setOverlayState(RenderState.OVERLAY)
                .createCompositeState(true);

        return RenderType.create("specialmobs_entity_cutout_no_cull_blend", DefaultVertexFormats.NEW_ENTITY, 7, 256, true, false, state);
    }
}
