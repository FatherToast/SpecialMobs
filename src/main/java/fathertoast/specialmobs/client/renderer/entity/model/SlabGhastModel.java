package fathertoast.specialmobs.client.renderer.entity.model;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

public class SlabGhastModel<T extends Entity> extends HierarchicalModel<T> {

    private final ModelPart root;
    private final ModelPart[] tentacles = new ModelPart[9];

    public SlabGhastModel( ModelPart root ) {
        this.root = root;

        for ( int i = 0; i < tentacles.length; ++i ) {
            tentacles[i] = root.getChild( createTentacleName( i ) );
        }
    }

    private static String createTentacleName( int index ) {
        return "tentacle" + index;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        partDefinition.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs( 0, 0 )
                .addBox( -8.0F, 0.0F, -8.0F, 16.0F, 8.0F, 16.0F ),
                PartPose.offset( 0.0F, 17.6F, 0.0F ) );

        RandomSource random = RandomSource.create( 1660L );

        for ( int index = 0; index < 9; ++index ) {
            float xOffset = ( ( (float)( index % 3 ) - (float)( index / 3 % 2 ) * 0.5F + 0.25F ) / 2.0F * 2.0F - 1.0F ) * 5.0F;
            float zOffset = ( (float)( index / 3 ) / 2.0F * 2.0F - 1.0F ) * 5.0F;
            int heightVar = random.nextInt( 7 ) + 8;

            partDefinition.addOrReplaceChild( createTentacleName( index ), CubeListBuilder.create()
                    .texOffs( 0, 0)
                    .addBox( -1.0F, 0.0F, -1.0F, 2.0F, (float) heightVar, 2.0F ),
                    PartPose.offset( xOffset, 24.6F, zOffset ) );
        }
        return LayerDefinition.create( meshDefinition, 64, 32 );
    }

    @Override
    public void setupAnim( T ghast, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch ) {
        for( int i = 0; i < tentacles.length; ++i ) {
            tentacles[i].xRot = 0.2F * Mth.sin( ageInTicks * 0.3F + (float) i ) + 0.4F;
        }
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
