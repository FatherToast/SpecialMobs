package fathertoast.specialmobs.client.renderer.entity.species;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Function;

/** Copy of {@link net.minecraft.client.model.GhastModel} */
@OnlyIn( Dist.CLIENT )
public class CorporealShiftGhastModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart[] tentacles = new ModelPart[9];

    public CorporealShiftGhastModel(ModelPart root) {
        this.root = root;

        for ( int i = 0; i < tentacles.length; ++i ) {
            tentacles[i] = root.getChild( createTentacleName( i ) );
        }
    }

    private static String createTentacleName(int index) {
        return "tentacle" + index;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F), PartPose.offset(0.0F, 17.6F, 0.0F));
        RandomSource random = RandomSource.create(1660L);

        for(int i = 0; i < 9; ++i) {
            float f = (((float)(i % 3) - (float)(i / 3 % 2) * 0.5F + 0.25F) / 2.0F * 2.0F - 1.0F) * 5.0F;
            float f1 = ((float)(i / 3) / 2.0F * 2.0F - 1.0F) * 5.0F;
            int j = random.nextInt(7) + 8;
            partDefinition.addOrReplaceChild(createTentacleName(i), CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, (float)j, 2.0F), PartPose.offset(f, 24.6F, f1));
        }

        return LayerDefinition.create( meshDefinition, 64, 32 );
    }

    @Override
    public void setupAnim(T p_102681_, float p_102682_, float p_102683_, float p_102684_, float p_102685_, float p_102686_) {
        for(int i = 0; i < this.tentacles.length; ++i) {
            this.tentacles[i].xRot = 0.2F * Mth.sin(p_102684_ * 0.3F + (float)i) + 0.4F;
        }

    }

    @Override
    public ModelPart root() {
        return this.root;
    }


    public void setRenderType( Function<ResourceLocation, RenderType> renderTypeFunc ) {
        this.renderType = renderTypeFunc;
    }
}