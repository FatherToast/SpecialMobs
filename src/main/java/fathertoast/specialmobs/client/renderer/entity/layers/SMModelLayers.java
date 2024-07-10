package fathertoast.specialmobs.client.renderer.entity.layers;

import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;


public class SMModelLayers {


    public static final ModelLayerLocation SCOPE_CREEPER = create( "scope_creeper" );
    public static final ModelLayerLocation SLAB_GHAST = create( "slab_ghast" );

    public static final ModelLayerLocation CREEPER_OUTER_LAYER = create("creeper", "outer");
    public static final ModelLayerLocation BLAZE_OUTER_LAYER = create("blaze", "outer");
    public static final ModelLayerLocation ENDERMAN_OUTER_LAYER = create("enderman", "outer");
    public static final ModelLayerLocation GHAST_OUTER_LAYER = create("ghast", "outer");
    public static final ModelLayerLocation MAGMA_CUBE_OUTER_LAYER = create("magma_cube", "outer");
    public static final ModelLayerLocation SILVERFISH_OUTER_LAYER = create("silverfish", "outer");
    public static final ModelLayerLocation SLIME_OUTER_LAYER = create("slime", "outer");
    public static final ModelLayerLocation SPIDER_OUTER_LAYER = create("spider", "outer");

    public static final ModelLayerLocation PIGLIN = create("piglin");
    public static final ModelLayerLocation PIGLIN_OUTER_LAYER = create("piglin", "outer");
    public static final ModelLayerLocation PIGLIN_INNER_ARMOR = create("piglin", "inner_armor");
    public static final ModelLayerLocation PIGLIN_OUTER_ARMOR = create("piglin", "outer_armor");
    public static final ModelLayerLocation ZOMBIFIED_PIGLIN = create("zombified_piglin");
    public static final ModelLayerLocation ZOMBIFIED_PIGLIN_OUTER_LAYER = create("zombified_piglin", "outer");
    public static final ModelLayerLocation ZOMBIFIED_PIGLIN_INNER_ARMOR = create("zombified_piglin", "inner_armor");
    public static final ModelLayerLocation ZOMBIFIED_PIGLIN_OUTER_ARMOR = create("zombified_piglin", "outer_armor");



    private static ModelLayerLocation create(String path) {
        return create(path, "main");
    }

    private static ModelLayerLocation create(String path, String layerName) {
        return new ModelLayerLocation(SpecialMobs.resourceLoc(path), layerName);
    }


    /**
     * Below are some copies of vanilla layers, but with optional cube deformation, so they can
     * be used as outer layers and other things if needed.
     */
    // ---------------------------------- CUSTOM VANILLA LAYERS ------------------------------

    // BLAZE
    public static LayerDefinition blazeBodyLayer( CubeDeformation cubeDeformation ) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubeDeformation), PartPose.ZERO);
        float f = 0.0F;
        CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(0, 16).addBox(0.0F, 0.0F, 0.0F, 2.0F, 8.0F, 2.0F, cubeDeformation);

        for (int i = 0; i < 4; ++i) {
            float x = Mth.cos(f) * 9.0F;
            float y = -2.0F + Mth.cos((float)(i * 2) * 0.25F);
            float z = Mth.sin(f) * 9.0F;
            partDefinition.addOrReplaceChild("part" + i, cubelistbuilder, PartPose.offset(x, y, z));
            ++f;
        }

        f = ((float)Math.PI / 4F);

        for (int j = 4; j < 8; ++j) {
            float x = Mth.cos(f) * 7.0F;
            float y = 2.0F + Mth.cos((float)(j * 2) * 0.25F);
            float z = Mth.sin(f) * 7.0F;
            partDefinition.addOrReplaceChild("part" + j, cubelistbuilder, PartPose.offset(x, y, z));
            ++f;
        }

        f = 0.47123894F;

        for (int k = 8; k < 12; ++k) {
            float x = Mth.cos(f) * 5.0F;
            float y = 11.0F + Mth.cos((float)k * 1.5F * 0.5F);
            float z = Mth.sin(f) * 5.0F;
            partDefinition.addOrReplaceChild("part" + k, cubelistbuilder, PartPose.offset(x, y, z));
            ++f;
        }

        return LayerDefinition.create(meshDefinition, 64, 32);
    }


    // ENDERMAN
    public static LayerDefinition endermanBodyLayer( CubeDeformation cubeDeformation ) {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(cubeDeformation, -14.0F);
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartPose partpose = PartPose.offset(0.0F, -13.0F, 0.0F);

        partDefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-0.5F)), partpose);
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubeDeformation), partpose);
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, cubeDeformation), PartPose.offset(0.0F, -14.0F, 0.0F));
        partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 30.0F, 2.0F, cubeDeformation), PartPose.offset(-5.0F, -12.0F, 0.0F));
        partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-1.0F, -2.0F, -1.0F, 2.0F, 30.0F, 2.0F, cubeDeformation), PartPose.offset(5.0F, -12.0F, 0.0F));
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 30.0F, 2.0F, cubeDeformation), PartPose.offset(-2.0F, -5.0F, 0.0F));
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 30.0F, 2.0F, cubeDeformation), PartPose.offset(2.0F, -5.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, 64, 32);
    }


    // GHAST
    public static LayerDefinition ghastBodyLayer( CubeDeformation cubeDeformation ) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F, cubeDeformation), PartPose.offset(0.0F, 17.6F, 0.0F));
        RandomSource random = RandomSource.create(1660L);

        for(int i = 0; i < 9; ++i) {
            float xOffset = (((float)(i % 3) - (float)(i / 3 % 2) * 0.5F + 0.25F) / 2.0F * 2.0F - 1.0F) * 5.0F;
            float zOffset = ((float)(i / 3) / 2.0F * 2.0F - 1.0F) * 5.0F;
            int tentacleLength = random.nextInt(7) + 8;
            partDefinition.addOrReplaceChild("tentacle" + i, CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, (float) tentacleLength, 2.0F, cubeDeformation), PartPose.offset(xOffset, 24.6F, zOffset));
        }

        return LayerDefinition.create(meshDefinition, 64, 32);
    }


    // MAGMA CUBE
    public static LayerDefinition magmaCubeBodyLayer( CubeDeformation cubeDeformation ) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        for(int i = 0; i < 8; ++i) {
            int j = 0;
            int k = i;
            if (i == 2) {
                j = 24;
                k = 10;
            } else if (i == 3) {
                j = 24;
                k = 19;
            }
            partDefinition.addOrReplaceChild("cube" + i, CubeListBuilder.create().texOffs(j, k).addBox(-4.0F, (float)(16 + i), -4.0F, 8.0F, 1.0F, 8.0F, cubeDeformation), PartPose.ZERO);
        }
        partDefinition.addOrReplaceChild("inside_cube", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 18.0F, -2.0F, 4.0F, 4.0F, 4.0F, cubeDeformation), PartPose.ZERO);

        return LayerDefinition.create(meshDefinition, 64, 32);
    }
    

    // SILVERFISH
    public static LayerDefinition silverfishBodyLayer( CubeDeformation cubeDeformation ) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        final int[][] BODY_SIZES = new int[][]{{3, 2, 2}, {4, 3, 2}, {6, 4, 3}, {3, 3, 3}, {2, 2, 3}, {2, 1, 2}, {1, 1, 2}};
        final int[][] BODY_TEXS = new int[][]{{0, 0}, {0, 4}, {0, 9}, {0, 16}, {0, 22}, {11, 0}, {13, 4}};

        float[] zOffsets = new float[7];
        float f = -3.5F;

        for(int i = 0; i < 7; ++i) {
            partDefinition.addOrReplaceChild("segment" + i, CubeListBuilder.create().texOffs(BODY_TEXS[i][0], BODY_TEXS[i][1]).addBox((float)BODY_SIZES[i][0] * -0.5F, 0.0F, (float)BODY_SIZES[i][2] * -0.5F, (float)BODY_SIZES[i][0], (float)BODY_SIZES[i][1], (float)BODY_SIZES[i][2], cubeDeformation), PartPose.offset(0.0F, (float)(24 - BODY_SIZES[i][1]), f));
            zOffsets[i] = f;
            if (i < 6) {
                f += (float)(BODY_SIZES[i][2] + BODY_SIZES[i + 1][2]) * 0.5F;
            }
        }
        partDefinition.addOrReplaceChild("layer0", CubeListBuilder.create().texOffs(20, 0).addBox(-5.0F, 0.0F, (float)BODY_SIZES[2][2] * -0.5F, 10.0F, 8.0F, (float)BODY_SIZES[2][2], cubeDeformation), PartPose.offset(0.0F, 16.0F, zOffsets[2]));
        partDefinition.addOrReplaceChild("layer1", CubeListBuilder.create().texOffs(20, 11).addBox(-3.0F, 0.0F, (float)BODY_SIZES[4][2] * -0.5F, 6.0F, 4.0F, (float)BODY_SIZES[4][2], cubeDeformation), PartPose.offset(0.0F, 20.0F, zOffsets[4]));
        partDefinition.addOrReplaceChild("layer2", CubeListBuilder.create().texOffs(20, 18).addBox(-3.0F, 0.0F, (float)BODY_SIZES[4][2] * -0.5F, 6.0F, 5.0F, (float)BODY_SIZES[1][2], cubeDeformation), PartPose.offset(0.0F, 19.0F, zOffsets[1]));

        return LayerDefinition.create(meshDefinition, 64, 32);
    }


    // SLIME - outer layer
    public static LayerDefinition slimeOuterBodyLayer( CubeDeformation cubeDeformation ) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 16.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubeDeformation), PartPose.ZERO);

        return LayerDefinition.create(meshDefinition, 64, 32);
    }


    // SPIDER
    public static LayerDefinition spiderBodyLayer( CubeDeformation cubeDeformation ) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 4).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F, cubeDeformation), PartPose.offset(0.0F, 15.0F, -3.0F));
        partDefinition.addOrReplaceChild("body0", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, cubeDeformation), PartPose.offset(0.0F, 15.0F, 0.0F));
        partDefinition.addOrReplaceChild("body1", CubeListBuilder.create().texOffs(0, 12).addBox(-5.0F, -4.0F, -6.0F, 10.0F, 8.0F, 12.0F, cubeDeformation), PartPose.offset(0.0F, 15.0F, 9.0F));
        CubeListBuilder rightPartsBuilder = CubeListBuilder.create().texOffs(18, 0).addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, cubeDeformation);
        CubeListBuilder leftPartsBuilder = CubeListBuilder.create().texOffs(18, 0).mirror().addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, cubeDeformation);
        partDefinition.addOrReplaceChild("right_hind_leg", rightPartsBuilder, PartPose.offset(-4.0F, 15.0F, 2.0F));
        partDefinition.addOrReplaceChild("left_hind_leg", leftPartsBuilder, PartPose.offset(4.0F, 15.0F, 2.0F));
        partDefinition.addOrReplaceChild("right_middle_hind_leg", rightPartsBuilder, PartPose.offset(-4.0F, 15.0F, 1.0F));
        partDefinition.addOrReplaceChild("left_middle_hind_leg", leftPartsBuilder, PartPose.offset(4.0F, 15.0F, 1.0F));
        partDefinition.addOrReplaceChild("right_middle_front_leg", rightPartsBuilder, PartPose.offset(-4.0F, 15.0F, 0.0F));
        partDefinition.addOrReplaceChild("left_middle_front_leg", leftPartsBuilder, PartPose.offset(4.0F, 15.0F, 0.0F));
        partDefinition.addOrReplaceChild("right_front_leg", rightPartsBuilder, PartPose.offset(-4.0F, 15.0F, -1.0F));
        partDefinition.addOrReplaceChild("left_front_leg", leftPartsBuilder, PartPose.offset(4.0F, 15.0F, -1.0F));

        return LayerDefinition.create(meshDefinition, 64, 32);
    }
}
