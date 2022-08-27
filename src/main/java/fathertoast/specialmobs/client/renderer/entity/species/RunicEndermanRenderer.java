package fathertoast.specialmobs.client.renderer.entity.species;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobEyesLayer;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobOverlayLayer;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import fathertoast.specialmobs.common.entity.enderman.RunicEndermanEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

public class RunicEndermanRenderer extends MobRenderer<RunicEndermanEntity, RunicEndermanModel<RunicEndermanEntity>> {

    public static final ResourceLocation CRYSTAL_BEAM_LOCATION = new ResourceLocation("textures/entity/end_crystal/end_crystal_beam.png");
    private static final RenderType BEAM = RenderType.entitySmoothCutout(CRYSTAL_BEAM_LOCATION);

    private final float baseShadowRadius;


    public RunicEndermanRenderer(EntityRendererManager rendererManager) {
        super(rendererManager, new RunicEndermanModel<>(0.0F), 0.5F);

        baseShadowRadius = shadowRadius;
        addLayer( new SpecialMobEyesLayer<>( this ) );
        addLayer( new SpecialMobOverlayLayer<>( this, new RunicEndermanModel<>( 0.25F ) ) );
    }

    @Override
    public void render(RunicEndermanEntity enderman, float rotation, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        if (enderman.getBeamTargetId().isPresent()) {
            Entity entity = enderman.level.getEntity(enderman.getBeamTargetId().getAsInt());

            if (entity != null) {
                matrixStack.pushPose();
                float x = (float)(entity.getX() - MathHelper.lerp(partialTicks, enderman.xo, enderman.getX()));
                float y = (float)(entity.getY() - MathHelper.lerp(partialTicks, enderman.yo, enderman.getY()));
                float z = (float)(entity.getZ() - MathHelper.lerp(partialTicks, enderman.zo, enderman.getZ()));
                renderBeamAttack(x, y, z, partialTicks, enderman.tickCount, matrixStack, buffer, packedLight);
                matrixStack.popPose();
            }
        }

        super.render(enderman, rotation, partialTicks, matrixStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(RunicEndermanEntity enderman) {
        return ((ISpecialMob<?>) enderman).getSpecialData().getTexture();
    }

    @Override
    protected void scale(RunicEndermanEntity entity, MatrixStack matrixStack, float partialTick ) {
        super.scale( entity, matrixStack, partialTick );

        final float scale = ((ISpecialMob<?>) entity).getSpecialData().getRenderScale();
        shadowRadius = baseShadowRadius * scale;
        matrixStack.scale( scale, scale, scale );
    }

    /** Copy-paste Ender Crystal render code. {@link EnderDragonRenderer#renderCrystalBeams(float, float, float, float, int, MatrixStack, IRenderTypeBuffer, int)} */
    private void renderBeamAttack(float x, float y, float z, float partialTicks, int tickCount, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        float f = MathHelper.sqrt(x * x + z * z);
        float xyzMul = x * x + y * y + z * z;
        float f1 = MathHelper.sqrt(xyzMul);

        matrixStack.pushPose();
        matrixStack.translate(0.0D, 2.0D, 0.0D);
        matrixStack.mulPose(Vector3f.YP.rotation((float)(-Math.atan2(z, x)) - ((float)Math.PI / 2F)));
        matrixStack.mulPose(Vector3f.XP.rotation((float)(-Math.atan2(f, y)) - ((float)Math.PI / 2F)));

        IVertexBuilder ivertexbuilder = buffer.getBuffer(BEAM);

        float f2 = 0.0F - ((float)tickCount + partialTicks) * 0.01F;
        float f3 = MathHelper.sqrt(xyzMul) / 32.0F - ((float)tickCount + partialTicks) * 0.01F;
        float f4 = 0.0F;
        float f5 = 0.75F;
        float f6 = 0.0F;

        MatrixStack.Entry entry = matrixStack.last();
        Matrix4f matrix4f = entry.pose();
        Matrix3f matrix3f = entry.normal();

        for(int j = 1; j <= 8; ++j) {
            float f7 = MathHelper.sin((float)j * ((float)Math.PI * 2F) / 8.0F) * 0.75F;
            float f8 = MathHelper.cos((float)j * ((float)Math.PI * 2F) / 8.0F) * 0.75F;
            float f9 = (float)j / 8.0F;

            ivertexbuilder.vertex(matrix4f, f4 * 0.2F, f5 * 0.2F, 0.0F).color(0, 0, 0, 255).uv(f6, f2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            ivertexbuilder.vertex(matrix4f, f4, f5, f1).color(255, 100, 255, 255).uv(f6, f3).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            ivertexbuilder.vertex(matrix4f, f7, f8, f1).color(255, 100, 255, 255).uv(f9, f3).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            ivertexbuilder.vertex(matrix4f, f7 * 0.2F, f8 * 0.2F, 0.0F).color(0, 0, 0, 255).uv(f9, f2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            f4 = f7;
            f5 = f8;
            f6 = f9;
        }
        matrixStack.popPose();
    }
}
