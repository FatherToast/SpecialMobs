package fathertoast.specialmobs.client.renderer.entity.model;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Creeper;

public class ScopeCreeperModel<T extends Creeper> extends HierarchicalModel<T> {

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart scope;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public ScopeCreeperModel( ModelPart root ) {
        this.root = root;
        this.head = root.getChild("head");
        this.scope = root.getChild("head").getChild("scope");
        this.leftHindLeg = root.getChild("right_hind_leg");
        this.rightHindLeg = root.getChild("left_hind_leg");
        this.leftFrontLeg = root.getChild("right_front_leg");
        this.rightFrontLeg = root.getChild("left_front_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition head = partDefinition.addOrReplaceChild( "head", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox( -4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F ),
                PartPose.offset( 0.0F, 6.0F, 0.0F ) );

        partDefinition.addOrReplaceChild( "body", CubeListBuilder.create()
                .texOffs( 16, 16 )
                .addBox( -4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F ),
                PartPose.offset( 0.0F, 6.0F, 0.0F ) );

        CubeListBuilder cubeListBuilder = CubeListBuilder.create()
                .texOffs( 0, 16 ).addBox( -2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F );

        partDefinition.addOrReplaceChild( "right_hind_leg", cubeListBuilder, PartPose.offset( -2.0F, 18.0F, 4.0F ) );
        partDefinition.addOrReplaceChild( "left_hind_leg", cubeListBuilder, PartPose.offset( 2.0F, 18.0F, 4.0F ) );
        partDefinition.addOrReplaceChild( "right_front_leg", cubeListBuilder, PartPose.offset( -2.0F, 18.0F, -4.0F ) );
        partDefinition.addOrReplaceChild( "left_front_leg", cubeListBuilder, PartPose.offset( 2.0F, 18.0F, -4.0F ) );


        head.addOrReplaceChild("scope", CubeListBuilder.create()
                .texOffs( 44, 10 ).addBox( -5.0F, -31.0F, -10.0F, 4.0F, 4.0F, 6.0F )
                .texOffs( 44, 0 ).addBox( 1.0F, -31.0F, -10.0F, 4.0F, 4.0F, 6.0F )
                .texOffs( 44, 0 ).addBox( -1.0F, -30.0F, -4.25F, 2.0F, 2.0F, 0.0F ),
                PartPose.offset( 0.0F, 24.0F, 0.0F ) );

        return LayerDefinition.create( meshDefinition, 64, 32 );
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim( T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch ) {
        head.yRot = netHeadYaw * ( (float) Math.PI / 180F );
        head.xRot = headPitch * ( (float) Math.PI / 180F );
        rightHindLeg.xRot = Mth.cos( limbSwing * 0.6662F ) * 1.4F * limbSwingAmount;
        leftHindLeg.xRot = Mth.cos( limbSwing * 0.6662F + (float) Math.PI ) * 1.4F * limbSwingAmount;
        rightFrontLeg.xRot = Mth.cos( limbSwing * 0.6662F + (float) Math.PI ) * 1.4F * limbSwingAmount;
        leftFrontLeg.xRot = Mth.cos( limbSwing * 0.6662F ) * 1.4F * limbSwingAmount;
    }
}
