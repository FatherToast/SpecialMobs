package toast.specialMobs.client;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import toast.specialMobs._SpecialMobs;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSpecialSpitball extends Render
{
    private static final ResourceLocation TEXTURE = new ResourceLocation(_SpecialMobs.TEXTURE_PATH + "projectile/spitball.png");

    /// Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return RenderSpecialSpitball.TEXTURE;
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTick) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y, (float)z);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        this.bindEntityTexture(entity);
        Tessellator tessellator = Tessellator.instance;
        GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        tessellator.addVertexWithUV(-0.5, -0.5, 0.0, 0.0F, 1.0F);
        tessellator.addVertexWithUV( 0.5, -0.5, 0.0, 1.0F, 1.0F);
        tessellator.addVertexWithUV( 0.5,  0.5, 0.0, 1.0F, 0.0F);
        tessellator.addVertexWithUV(-0.5,  0.5, 0.0, 0.0F, 0.0F);
        tessellator.draw();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }
}