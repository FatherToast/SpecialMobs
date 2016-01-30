package toast.specialMobs.client;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import toast.specialMobs.entity.EntitySpecialFishHook;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSpecialFishHook extends Render
{
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/particle/particles.png");

    /// Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return RenderSpecialFishHook.TEXTURE;
    }

    public void doRenderSpecialFishHook(EntitySpecialFishHook entity, double x, double y, double z, float f, float partialTicks) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y, (float)z);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        this.bindEntityTexture(entity);
        Tessellator tessellator = Tessellator.instance;
        float minU = (1 * 8 + 0) / 128.0F;
        float maxU = (1 * 8 + 8) / 128.0F;
        float minV = (2 * 8 + 0) / 128.0F;
        float maxV = (2 * 8 + 8) / 128.0F;
        GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        tessellator.addVertexWithUV(-0.5, -0.5, 0.0, minU, maxV);
        tessellator.addVertexWithUV( 0.5, -0.5, 0.0, maxU, maxV);
        tessellator.addVertexWithUV( 0.5,  0.5, 0.0, maxU, minV);
        tessellator.addVertexWithUV(-0.5,  0.5, 0.0, minU, minV);
        tessellator.draw();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();

        if (entity.angler != null) {
            float swing = MathHelper.sin(MathHelper.sqrt_float(entity.angler.getSwingProgress(partialTicks)) * (float)Math.PI);
            Vec3 vec;
            if (entity.angler instanceof EntitySilverfish) {
            	vec = Vec3.createVectorHelper(0.0, -0.2, 0.8);
            }
            else {
            	vec = Vec3.createVectorHelper(-0.5, 0.03, 0.8);
            }
            vec.rotateAroundX(-(entity.angler.prevRotationPitch + (entity.angler.rotationPitch - entity.angler.prevRotationPitch) * partialTicks) * (float)Math.PI / 180.0F);
            vec.rotateAroundY(-(entity.angler.prevRotationYaw + (entity.angler.rotationYaw - entity.angler.prevRotationYaw) * partialTicks) * (float)Math.PI / 180.0F);
            vec.rotateAroundY( swing * 0.5F);
            vec.rotateAroundX(-swing * 0.7F);

            double d5 = entity.angler.prevPosX + (entity.angler.posX - entity.angler.prevPosX) * partialTicks + vec.xCoord;
            double d6 = entity.angler.prevPosY + (entity.angler.posY - entity.angler.prevPosY) * partialTicks + vec.yCoord;
            double d8 = entity.angler.prevPosZ + (entity.angler.posZ - entity.angler.prevPosZ) * partialTicks + vec.zCoord;
            double d9 = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
            double d10 = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks + 0.25;
            double d11 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;
            double d12 = (float)(d5 - d9);
            double d13 = (float)(d6 - d10) + entity.angler.getEyeHeight();
            double d14 = (float)(d8 - d11);

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            tessellator.startDrawing(3);
            tessellator.setColorOpaque_I(0);
            byte b = 16;
            for (int i = 0; i <= b; i++) {
                float f12 = (float)i / (float)b;
                tessellator.addVertex(x + d12 * f12, y + d13 * (f12 * f12 + f12) * 0.5 + 0.25, z + d14 * f12);
            }
            tessellator.draw();
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        this.doRenderSpecialFishHook((EntitySpecialFishHook)entity, x, y, z, yaw, partialTicks);
    }
}