package fathertoast.specialmobs.client.misc;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class SMRenderTypes {

    public static RenderType entityCutoutNoCullBlend(ResourceLocation resourceLocation) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
                .setShaderState(RenderStateShard.ShaderStateShard.RENDERTYPE_ENTITY_ALPHA_SHADER)
                .setLightmapState(RenderStateShard.LightmapStateShard.LIGHTMAP)
                .setOverlayState(RenderStateShard.OverlayStateShard.OVERLAY)
                .setShaderState(RenderStateShard.ShaderStateShard.RENDERTYPE_ENTITY_ALPHA_SHADER)
                .createCompositeState(true);

        return RenderType.create("specialmobs_entity_cutout_no_cull_blend", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, state);
    }
}
