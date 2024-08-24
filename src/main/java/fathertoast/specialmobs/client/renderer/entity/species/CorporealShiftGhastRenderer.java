package fathertoast.specialmobs.client.renderer.entity.species;

import com.mojang.blaze3d.vertex.PoseStack;
import fathertoast.specialmobs.client.misc.SMRenderTypes;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialGhastEyesLayer;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.entity.SpecialMobData;
import fathertoast.specialmobs.common.entity.ghast.CorporealShiftGhastEntity;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.client.model.GhastModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class CorporealShiftGhastRenderer extends MobRenderer<CorporealShiftGhastEntity, GhastModel<CorporealShiftGhastEntity>> {
    
    private static final Function<ResourceLocation, RenderType> INCORPOREAL = SMRenderTypes::entityCutoutNoCullBlend;
    
    private static final ResourceLocation EYES = References.getEntityEyesTexture( "ghast", "corporeal_shift" );
    private static final ResourceLocation SHOOT_EYES = References.getEntityShootingEyesTexture( "ghast", "corporeal_shift" );
    
    private final float baseShadowRadius;
    
    public CorporealShiftGhastRenderer( EntityRendererProvider.Context context ) {
        super( context, new GhastModel<>( context.bakeLayer( ModelLayers.GHAST ) ), 1.5F );
        addLayer( new SpecialGhastEyesLayer<>( this, EYES, SHOOT_EYES ) );
        baseShadowRadius = shadowRadius;
    }
    
    @Override
    public void render( CorporealShiftGhastEntity ghast, float rotation, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight ) {
        model.renderType = ghast.isCorporeal() ? RenderType::entityCutoutNoCull : INCORPOREAL;
        super.render( ghast, rotation, partialTicks, poseStack, buffer, packedLight );
    }
    
    
    @Override
    public ResourceLocation getTextureLocation( CorporealShiftGhastEntity entity ) {
        final SpecialMobData<?> data = ((ISpecialMob<?>) entity).getSpecialData();
        return entity.isCharging() && data.getTextureAnimation() != null ? data.getTextureAnimation() : data.getTexture();
    }
    
    @Override
    protected void scale( CorporealShiftGhastEntity entity, PoseStack poseStack, float partialTick ) {
        // The base scale of 4.5 is taken from GhastRenderer
        final float scale = 4.5F * ((ISpecialMob<?>) entity).getSpecialData().getRenderScale();
        shadowRadius = baseShadowRadius * scale;
        poseStack.scale( scale, scale, scale );
    }
}