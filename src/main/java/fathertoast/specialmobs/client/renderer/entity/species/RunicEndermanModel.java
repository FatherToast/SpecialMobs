package fathertoast.specialmobs.client.renderer.entity.species;

import fathertoast.specialmobs.common.entity.enderman.RunicEndermanEntity;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;

/** Epic copy-paste of {@link net.minecraft.client.renderer.entity.model.EndermanModel} */
public class RunicEndermanModel<T extends RunicEndermanEntity> extends BipedModel<T> {

    public boolean carrying;
    public boolean creepy;

    public RunicEndermanModel(float something) {
        super(0.0F, -14.0F, 64, 32);
        float f = -14.0F;
        this.hat = new ModelRenderer(this, 0, 16);
        this.hat.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, something - 0.5F);
        this.hat.setPos(0.0F, -14.0F, 0.0F);
        this.body = new ModelRenderer(this, 32, 16);
        this.body.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, something);
        this.body.setPos(0.0F, -14.0F, 0.0F);
        this.rightArm = new ModelRenderer(this, 56, 0);
        this.rightArm.addBox(-1.0F, -2.0F, -1.0F, 2.0F, 30.0F, 2.0F, something);
        this.rightArm.setPos(-3.0F, -12.0F, 0.0F);
        this.leftArm = new ModelRenderer(this, 56, 0);
        this.leftArm.mirror = true;
        this.leftArm.addBox(-1.0F, -2.0F, -1.0F, 2.0F, 30.0F, 2.0F, something);
        this.leftArm.setPos(5.0F, -12.0F, 0.0F);
        this.rightLeg = new ModelRenderer(this, 56, 0);
        this.rightLeg.addBox(-1.0F, 0.0F, -1.0F, 2.0F, 30.0F, 2.0F, something);
        this.rightLeg.setPos(-2.0F, -2.0F, 0.0F);
        this.leftLeg = new ModelRenderer(this, 56, 0);
        this.leftLeg.mirror = true;
        this.leftLeg.addBox(-1.0F, 0.0F, -1.0F, 2.0F, 30.0F, 2.0F, something);
        this.leftLeg.setPos(2.0F, -2.0F, 0.0F);
    }
    @Override
    public void setupAnim(T enderman, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(enderman, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        this.head.visible = true;
        this.body.xRot = 0.0F;
        this.body.y = -14.0F;
        this.body.z = -0.0F;
        this.rightLeg.xRot -= 0.0F;
        this.leftLeg.xRot -= 0.0F;
        this.rightArm.xRot = (float)((double)this.rightArm.xRot * 0.5D);
        this.leftArm.xRot = (float)((double)this.leftArm.xRot * 0.5D);
        this.rightLeg.xRot = (float)((double)this.rightLeg.xRot * 0.5D);
        this.leftLeg.xRot = (float)((double)this.leftLeg.xRot * 0.5D);

        if (this.rightArm.xRot > 0.4F) {
            this.rightArm.xRot = 0.4F;
        }

        if (this.leftArm.xRot > 0.4F) {
            this.leftArm.xRot = 0.4F;
        }

        if (this.rightArm.xRot < -0.4F) {
            this.rightArm.xRot = -0.4F;
        }

        if (this.leftArm.xRot < -0.4F) {
            this.leftArm.xRot = -0.4F;
        }

        if (this.rightLeg.xRot > 0.4F) {
            this.rightLeg.xRot = 0.4F;
        }

        if (this.leftLeg.xRot > 0.4F) {
            this.leftLeg.xRot = 0.4F;
        }

        if (this.rightLeg.xRot < -0.4F) {
            this.rightLeg.xRot = -0.4F;
        }

        if (this.leftLeg.xRot < -0.4F) {
            this.leftLeg.xRot = -0.4F;
        }

        if (this.carrying) {
            this.rightArm.xRot = -0.5F;
            this.leftArm.xRot = -0.5F;
            this.rightArm.zRot = 0.05F;
            this.leftArm.zRot = -0.05F;
        }
        this.rightArm.z = 0.0F;
        this.leftArm.z = 0.0F;
        this.rightLeg.z = 0.0F;
        this.leftLeg.z = 0.0F;
        this.rightLeg.y = -5.0F;
        this.leftLeg.y = -5.0F;
        this.head.z = -0.0F;
        this.head.y = -13.0F;
        this.hat.x = this.head.x;
        this.hat.y = this.head.y;
        this.hat.z = this.head.z;
        this.hat.xRot = this.head.xRot;
        this.hat.yRot = this.head.yRot;
        this.hat.zRot = this.head.zRot;

        if (this.creepy) {
            this.head.y -= 5.0F;
        }
        this.rightArm.setPos(-5.0F, -12.0F, 0.0F);
        this.leftArm.setPos(5.0F, -12.0F, 0.0F);

        if (enderman.getBeamTargetId().isPresent()) {
            leftArm.xRot = -0.5F;
            rightArm.xRot = -0.5F;
            leftArm.zRot = -0.25F;
            rightArm.zRot = 0.25F;
        }
    }
}
