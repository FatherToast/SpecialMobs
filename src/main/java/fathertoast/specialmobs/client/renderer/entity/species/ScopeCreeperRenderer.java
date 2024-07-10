package fathertoast.specialmobs.client.renderer.entity.species;

import com.mojang.blaze3d.vertex.PoseStack;
import fathertoast.specialmobs.client.renderer.entity.layers.SMModelLayers;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialCreeperChargeLayer;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobEyesLayer;
import fathertoast.specialmobs.client.renderer.entity.model.ScopeCreeperModel;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Creeper;

public class ScopeCreeperRenderer extends MobRenderer<Creeper, ScopeCreeperModel<Creeper>> {

    private final float baseShadowRadius;

    public ScopeCreeperRenderer( EntityRendererProvider.Context context ) {
        super( context, new ScopeCreeperModel<>( context.bakeLayer( SMModelLayers.SCOPE_CREEPER ) ), 0.5F );

        baseShadowRadius = shadowRadius;
        addLayer( new SpecialMobEyesLayer<>( this ) );
        addLayer( new SpecialCreeperChargeLayer<>( this, context.getModelSet() ) );
    }

    @Override
    public ResourceLocation getTextureLocation( Creeper entity ) {
        return ((ISpecialMob<?>) entity).getSpecialData().getTexture();
    }

    @Override
    protected float getWhiteOverlayProgress( Creeper creeper, float partialTick ) {
        float swelling = creeper.getSwelling( partialTick );
        return (int)( swelling * 10.0F ) % 2 == 0 ? 0.0F : Mth.clamp( swelling, 0.5F, 1.0F );
    }

    @Override
    protected void scale( Creeper entity, PoseStack poseStack, float partialTick ) {
        super.scale( entity, poseStack, partialTick );

        final float scale = ((ISpecialMob<?>) entity).getSpecialData().getRenderScale();
        shadowRadius = baseShadowRadius * scale;
        poseStack.scale( scale, scale, scale );
    }
}
