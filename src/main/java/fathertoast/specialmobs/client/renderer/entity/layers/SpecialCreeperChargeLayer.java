package fathertoast.specialmobs.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fathertoast.specialmobs.common.entity.creeper._SpecialCreeperEntity;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CreeperPowerLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn( Dist.CLIENT )
public class SpecialCreeperChargeLayer extends CreeperPowerLayer {
    
    private static final ResourceLocation[] CHARGED = new ResourceLocation[] {
            new ResourceLocation( "textures/entity/creeper/creeper_armor.png" ),
            References.getEntityTexture( "creeper", "super_charged" )
    };
    
    private final CreeperModel<Creeper> model;

    
    public SpecialCreeperChargeLayer( RenderLayerParent<Creeper, CreeperModel<Creeper>> renderer, EntityModelSet modelSet ) {
        super( renderer, modelSet );
        this.model = new CreeperModel<>( modelSet.bakeLayer( ModelLayers.CREEPER_ARMOR ));
    }
    
    @Override
    public void render( PoseStack poseStack, MultiBufferSource buffer, int packedLight, Creeper creeper, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch ) {
        if( creeper.isPowered() || ((_SpecialCreeperEntity) creeper).isSupercharged() ) {
            float tickAndPartial = (float) creeper.tickCount + partialTicks;
            float textureOffset = tickAndPartial * 0.01F;
            
            model.prepareMobModel( creeper, limbSwing, limbSwingAmount, partialTicks );
            this.getParentModel().copyPropertiesTo( model );
            VertexConsumer vertexConsumer = buffer.getBuffer( RenderType.energySwirl( this.getTextureLocation( creeper ), textureOffset, textureOffset ) );
            model.setupAnim( creeper, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch );
            model.renderToBuffer( poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0.5F, 0.5F, 0.5F, 1.0F );
        }
    }

    @Override
    protected ResourceLocation getTextureLocation( Creeper creeper ) {
        return ((_SpecialCreeperEntity) creeper).isSupercharged() ? CHARGED[1] : CHARGED[0];
    }
}