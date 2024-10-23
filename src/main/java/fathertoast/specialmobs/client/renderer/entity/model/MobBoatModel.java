package fathertoast.specialmobs.client.renderer.entity.model;

import com.google.common.collect.ImmutableList;
import fathertoast.specialmobs.common.entity.misc.MobBoat;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.WaterPatchModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Essentially a copy of {@link net.minecraft.client.model.BoatModel}
 */
public class MobBoatModel extends ListModel<MobBoat> implements WaterPatchModel {

    private final ModelPart leftPaddle;
    private final ModelPart rightPaddle;
    private final ModelPart waterPatch;
    private final ImmutableList<ModelPart> parts;


    public MobBoatModel( ModelPart root ) {
        leftPaddle = root.getChild( "left_paddle" );
        rightPaddle = root.getChild( "right_paddle" );
        waterPatch = root.getChild( "water_patch" );
        parts = createPartsBuilder( root ).build();
    }

    protected ImmutableList.Builder<ModelPart> createPartsBuilder( ModelPart root ) {
        ImmutableList.Builder<ModelPart> builder = new ImmutableList.Builder<>();
        builder.add(
                root.getChild("bottom"),
                root.getChild("back"),
                root.getChild("front"),
                root.getChild("right"),
                root.getChild("left"),
                leftPaddle,
                rightPaddle);

        return builder;
    }

    public static void createChildren( PartDefinition partDefinition ) {
        partDefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 0).addBox(-14.0F, -9.0F, -3.0F, 28.0F, 16.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 3.0F, 1.0F, ((float)Math.PI / 2F), 0.0F, 0.0F));
        partDefinition.addOrReplaceChild("back", CubeListBuilder.create().texOffs(0, 19).addBox(-13.0F, -7.0F, -1.0F, 18.0F, 6.0F, 2.0F), PartPose.offsetAndRotation(-15.0F, 4.0F, 4.0F, 0.0F, ((float)Math.PI * 1.5F), 0.0F));
        partDefinition.addOrReplaceChild("front", CubeListBuilder.create().texOffs(0, 27).addBox(-8.0F, -7.0F, -1.0F, 16.0F, 6.0F, 2.0F), PartPose.offsetAndRotation(15.0F, 4.0F, 0.0F, 0.0F, ((float)Math.PI / 2F), 0.0F));
        partDefinition.addOrReplaceChild("right", CubeListBuilder.create().texOffs(0, 35).addBox(-14.0F, -7.0F, -1.0F, 28.0F, 6.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 4.0F, -9.0F, 0.0F, (float)Math.PI, 0.0F));
        partDefinition.addOrReplaceChild("left", CubeListBuilder.create().texOffs(0, 43).addBox(-14.0F, -7.0F, -1.0F, 28.0F, 6.0F, 2.0F), PartPose.offset(0.0F, 4.0F, 9.0F));

        partDefinition.addOrReplaceChild("left_paddle", CubeListBuilder.create().texOffs(62, 0).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F).addBox(-1.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F), PartPose.offsetAndRotation(3.0F, -5.0F, 9.0F, 0.0F, 0.0F, 0.19634955F));
        partDefinition.addOrReplaceChild("right_paddle", CubeListBuilder.create().texOffs(62, 20).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F).addBox(0.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F), PartPose.offsetAndRotation(3.0F, -5.0F, -9.0F, 0.0F, (float)Math.PI, 0.19634955F));
        partDefinition.addOrReplaceChild("water_patch", CubeListBuilder.create().texOffs(0, 0).addBox(-14.0F, -9.0F, -3.0F, 28.0F, 16.0F, 3.0F), PartPose.offsetAndRotation(0.0F, -3.0F, 1.0F, ((float)Math.PI / 2F), 0.0F, 0.0F));
    }

    public static LayerDefinition createBodyModel() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        createChildren( partDefinition );

        return LayerDefinition.create( meshDefinition, 128, 64 );
    }

    @Override
    public void setupAnim( MobBoat boat, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch ) {
        animatePaddle( boat, 0, leftPaddle, limbSwing );
        animatePaddle( boat, 1, rightPaddle, limbSwing );
    }

    @Override
    public ImmutableList<ModelPart> parts() {
        return parts;
    }

    @Override
    public ModelPart waterPatch() {
        return waterPatch;
    }


    private static void animatePaddle( MobBoat boat, int paddleIndex, ModelPart paddle, float limbSwing ) {
        float rowingTime = boat.getRowingTime( paddleIndex, limbSwing );

        paddle.xRot = Mth.clampedLerp(
                (-(float) Math.PI / 3F ),
                -0.2617994F,
                ( Mth.sin( -rowingTime ) + 1.0F ) / 2.0F
        );
        paddle.yRot = Mth.clampedLerp(
                (-(float) Math.PI / 4F ),
                ( (float) Math.PI / 4F ),
                ( Mth.sin( -rowingTime + 1.0F ) + 1.0F ) / 2.0F
        );

        if ( paddleIndex == 1 ) {
            paddle.yRot = (float) Math.PI - paddle.yRot;
        }
    }
}
