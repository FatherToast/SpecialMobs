package fathertoast.specialmobs.client.renderer.entity.misc;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import fathertoast.specialmobs.client.renderer.entity.layers.SMModelLayers;
import fathertoast.specialmobs.client.renderer.entity.model.MobBoatModel;
import fathertoast.specialmobs.client.renderer.entity.model.MobRaftModel;
import fathertoast.specialmobs.common.entity.misc.MobBoat;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.WaterPatchModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import org.joml.Quaternionf;

import java.util.Map;
import java.util.stream.Stream;

public class MobBoatRenderer extends EntityRenderer<MobBoat> {

    private final Map<Boat.Type, Pair<ResourceLocation, ListModel<MobBoat>>> boatResources;

    public MobBoatRenderer( EntityRendererProvider.Context context ) {
        super( context );
        shadowRadius = 0.8F;
        boatResources = Stream.of(Boat.Type.values()).collect( ImmutableMap.toImmutableMap( (type) -> type, (t) -> {
            return Pair.of( new ResourceLocation( getTextureLocation( t ) ), createBoatModel( context, t ) );
        }));
    }

    private ListModel<MobBoat> createBoatModel( EntityRendererProvider.Context context, Boat.Type type ) {
        ModelLayerLocation layerLocation = SMModelLayers.createBoatModelName( type );
        ModelPart modelPart = context.bakeLayer( layerLocation );

        if ( type == Boat.Type.BAMBOO ) {
            return new MobRaftModel( modelPart );
        }
        else {
            return new MobBoatModel( modelPart );
        }
    }

    private static String getTextureLocation( Boat.Type type ) {
        return "textures/entity/boat/" + type.getName() + ".png";
    }

    @Override
    public void render( MobBoat mobBoat, float rotation, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight ) {
        poseStack.pushPose();
        poseStack.translate( 0.0F, 0.375F, 0.0F );
        poseStack.mulPose( Axis.YP.rotationDegrees( 180.0F - rotation ) );

        float hurtTime = (float) mobBoat.getHurtTime() - partialTicks;
        float boatDamage = mobBoat.getDamage() - partialTicks;

        if ( boatDamage < 0.0F ) {
            boatDamage = 0.0F;
        }

        if ( hurtTime > 0.0F ) {
            poseStack.mulPose( Axis.XP.rotationDegrees( Mth.sin( hurtTime ) * hurtTime * boatDamage / 10.0F * (float) mobBoat.getHurtDir() ) );
        }
        float bubbleAngle = mobBoat.getBubbleAngle( partialTicks );

        if ( !Mth.equal( bubbleAngle, 0.0F ) ) {
            poseStack.mulPose( ( new Quaternionf() ).setAngleAxis( mobBoat.getBubbleAngle( partialTicks ) * ( (float) Math.PI / 180F ), 1.0F, 0.0F, 1.0F ) );
        }
        Pair<ResourceLocation, ListModel<MobBoat>> pair = getModelWithLocation( mobBoat );
        ResourceLocation textureLoc = pair.getFirst();
        ListModel<MobBoat> boatModel = pair.getSecond();

        poseStack.scale( -1.0F, -1.0F, 1.0F );
        poseStack.mulPose( Axis.YP.rotationDegrees( 90.0F ) );

        boatModel.setupAnim( mobBoat, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F );
        VertexConsumer buffer = bufferSource.getBuffer( boatModel.renderType( textureLoc ) );
        boatModel.renderToBuffer( poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F );

        if ( !mobBoat.isUnderWater() ) {
            VertexConsumer waterMask = bufferSource.getBuffer( RenderType.waterMask() );

            if ( boatModel instanceof WaterPatchModel waterPatchModel ) {
                waterPatchModel.waterPatch().render( poseStack, waterMask, packedLight, OverlayTexture.NO_OVERLAY );
            }
        }
        poseStack.popPose();
        super.render( mobBoat, rotation, partialTicks, poseStack, bufferSource, packedLight );
    }

    @Override
    @Deprecated
    public ResourceLocation getTextureLocation( MobBoat mobBoat ) {
        return getModelWithLocation(mobBoat).getFirst();
    }

    public Pair<ResourceLocation, ListModel<MobBoat>> getModelWithLocation( MobBoat boat ) {
        return boatResources.get( boat.getVariant() );
    }
}
