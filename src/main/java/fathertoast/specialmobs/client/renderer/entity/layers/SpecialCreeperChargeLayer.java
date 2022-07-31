package fathertoast.specialmobs.client.renderer.entity.layers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import fathertoast.specialmobs.common.entity.creeper._SpecialCreeperEntity;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn( Dist.CLIENT )
public class SpecialCreeperChargeLayer<T extends CreeperEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> {
    
    private static final ResourceLocation[] CHARGED = new ResourceLocation[] {
            new ResourceLocation( "textures/entity/creeper/creeper_armor.png" ),
            References.getEntityTexture( "creeper", "super_charged" )
    };
    
    private final M model;
    
    
    public SpecialCreeperChargeLayer( IEntityRenderer<T, M> renderer, M model ) {
        super( renderer );
        this.model = model;
    }
    
    @Override
    public void render( MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, T creeper, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch ) {
        if( creeper.isPowered() || ((_SpecialCreeperEntity) creeper).isSupercharged() ) {
            float tickAndPartial = (float) creeper.tickCount + partialTicks;
            float textureOffset = tickAndPartial * 0.01F;
            
            model.prepareMobModel( creeper, limbSwing, limbSwingAmount, partialTicks );
            this.getParentModel().copyPropertiesTo( model );
            IVertexBuilder ivertexbuilder = buffer.getBuffer( RenderType.energySwirl( this.getTextureLocation( creeper ), textureOffset, textureOffset ) );
            model.setupAnim( creeper, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch );
            model.renderToBuffer( matrixStack, ivertexbuilder, packedLight, OverlayTexture.NO_OVERLAY, 0.5F, 0.5F, 0.5F, 1.0F );
        }
    }
    
    @Override
    protected ResourceLocation getTextureLocation( CreeperEntity creeper ) {
        return ((_SpecialCreeperEntity) creeper).isSupercharged() ? CHARGED[1] : CHARGED[0];
    }
}