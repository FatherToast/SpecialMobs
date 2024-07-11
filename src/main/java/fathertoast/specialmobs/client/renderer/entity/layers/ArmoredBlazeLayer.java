package fathertoast.specialmobs.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.entity.blaze.ArmoredBlazeEntity;
import net.minecraft.client.model.BlazeModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Blaze;

public class ArmoredBlazeLayer extends RenderLayer<Blaze, BlazeModel<Blaze>> {

    private static final ResourceLocation ARMOR_TEXTURE = SpecialMobs.resourceLoc("textures/entity/blaze/armored_overlay.png");

    private final BlazeModel<Blaze> layerModel;

    public ArmoredBlazeLayer(RenderLayerParent<Blaze, BlazeModel<Blaze>> renderer, BlazeModel<Blaze> model ) {
        super( renderer );
        layerModel = model;
    }

    @Override
    public void render( PoseStack poseStack, MultiBufferSource buffer, int packedLight, Blaze entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch ) {

        if ( entity instanceof ArmoredBlazeEntity armoredBlaze && !armoredBlaze.hasArmor() )
            return;

        coloredCutoutModelCopyLayerRender( getParentModel(), layerModel, ARMOR_TEXTURE, poseStack, buffer,
                packedLight, entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTicks,
                1.0F, 1.0F, 1.0F ); // RGB
    }
}
