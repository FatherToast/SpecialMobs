package fathertoast.specialmobs.client.renderer.entity.model;

import fathertoast.specialmobs.common.core.register.SMTags;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;

/**
 * Slightly modified version of {@link net.minecraft.client.model.PiglinModel}.
 * <br><br>
 * Checks if the mob is in the {@link fathertoast.specialmobs.common.core.register.SMTags#ZOMBIFIED_PIGLINS} tag
 * and animates arms to stand out like a zombie if so.
 */
public class SpecialPiglinModel<T extends Mob> extends PlayerModel<T> {

    public final ModelPart rightEar = head.getChild("right_ear");
    public final ModelPart leftEar = head.getChild("left_ear");
    private final PartPose bodyDefault = body.storePose();
    private final PartPose headDefault = head.storePose();
    private final PartPose leftArmDefault = leftArm.storePose();
    private final PartPose rightArmDefault = rightArm.storePose();


    public SpecialPiglinModel( ModelPart modelPart ) {
        super( modelPart, false );
    }


    @Override
    public void setupAnim(T piglin, float limbSwing, float limbSwingAmount, float partialTick, float netHeadYaw, float headPitch ) {
        body.loadPose( bodyDefault );
        head.loadPose( headDefault );
        leftArm.loadPose( leftArmDefault );
        rightArm.loadPose( rightArmDefault );

        super.setupAnim( piglin, limbSwing, limbSwingAmount, partialTick, netHeadYaw, headPitch );

        float f1 = partialTick * 0.1F + limbSwing * 0.5F;
        float f2 = 0.08F + limbSwingAmount * 0.4F;
        leftEar.zRot = ( -(float) Math.PI / 6F ) - Mth.cos( f1 * 1.2F ) * f2;
        rightEar.zRot = ( (float) Math.PI / 6F ) + Mth.cos( f1 ) * f2;

        if ( !piglin.getType().is( SMTags.ZOMBIFIED_PIGLINS ) ) {
            // TODO - maybe make use of these animations
            /*
            PiglinArmPose piglinArmPose = piglin.getArmPose();

            if ( piglinArmPose == PiglinArmPose.DANCING ) {
                float f3 = partialTick / 60.0F;
                rightEar.zRot = ( (float)Math.PI / 6F ) + ( (float) Math.PI / 180F ) * Mth.sin( f3 * 30.0F ) * 10.0F;
                leftEar.zRot = ( -(float)Math.PI / 6F ) - ( (float) Math.PI / 180F ) * Mth.cos( f3 * 30.0F ) * 10.0F;
                head.x = Mth.sin( f3 * 10.0F );
                head.y = Mth.sin( f3 * 40.0F ) + 0.4F;
                rightArm.zRot = ( (float)Math.PI / 180F ) * ( 70.0F + Mth.cos( f3 * 40.0F ) * 10.0F);
                leftArm.zRot = rightArm.zRot * -1.0F;
                rightArm.y = Mth.sin( f3 * 40.0F ) * 0.5F + 1.5F;
                leftArm.y = Mth.sin( f3 * 40.0F ) * 0.5F + 1.5F;
                body.y = Mth.sin( f3 * 40.0F ) * 0.35F;
            }
            else if ( piglinArmPose == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON && attackTime == 0.0F ) {
                holdWeaponHigh( piglin );
            }
            else if ( piglinArmPose == PiglinArmPose.CROSSBOW_HOLD ) {
                AnimationUtils.animateCrossbowHold( rightArm, leftArm, head, !piglin.isLeftHanded() );
            }
            else if ( piglinArmPose == PiglinArmPose.CROSSBOW_CHARGE ) {
                AnimationUtils.animateCrossbowCharge( rightArm, leftArm, piglin, !piglin.isLeftHanded() );
            }
            else if ( piglinArmPose == PiglinArmPose.ADMIRING_ITEM ) {
                head.xRot = 0.5F;
                head.yRot = 0.0F;

                if ( piglin.isLeftHanded() ) {
                    rightArm.yRot = -0.5F;
                    rightArm.xRot = -0.9F;
                }
                else {
                    leftArm.yRot = 0.5F;
                    leftArm.xRot = -0.9F;
                }
            }

             */
        }
        else {
            AnimationUtils.animateZombieArms( leftArm, rightArm, piglin.isAggressive(), attackTime, partialTick );
        }

        leftPants.copyFrom( leftLeg );
        rightPants.copyFrom( rightLeg );
        leftSleeve.copyFrom( leftArm );
        rightSleeve.copyFrom( rightArm );
        jacket.copyFrom( body );
        hat.copyFrom( head );
    }

    @Override
    protected void setupAttackAnimation(T piglin, float partialTick ) {
        if ( attackTime > 0.0F && piglin instanceof Piglin && ( (Piglin)piglin ).getArmPose() == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON ) {
            AnimationUtils.swingWeaponDown( rightArm, leftArm, piglin, attackTime, partialTick );
        }
        else {
            super.setupAttackAnimation( piglin, partialTick );
        }
    }

    private void holdWeaponHigh( T piglin ) {
        if ( piglin.isLeftHanded() ) {
            leftArm.xRot = -1.8F;
        }
        else {
            rightArm.xRot = -1.8F;
        }
    }
}
