package toast.specialMobs.client;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderCreeper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import toast.specialMobs.entity.ISpecialMob;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSpecialCreeper extends RenderCreeper
{
    public RenderSpecialCreeper() {
        super();
    }

    /// Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return ((ISpecialMob)entity).getSpecialData().getTexture();
    }

    /// Allows the render to do any OpenGL state modifications necessary before the model is rendered.
    @Override
    protected void preRenderCallback(EntityLivingBase entity, float partialTick) {
        super.preRenderCallback(entity, partialTick);
        float scale = ((ISpecialMob)entity).getSpecialData().getRenderScale();
        this.shadowSize = 0.5F * scale;
        GL11.glScalef(scale, scale, scale);
    }

    /// Renders the ender creeper's glowing eyes.
    @Override
    protected int shouldRenderPass(EntityCreeper entity, int renderPass, float partialTick) {
        if (super.shouldRenderPass(entity, renderPass, partialTick) == 1)
            return 1;
        if (((ISpecialMob)entity).getSpecialData().getTextureCount() > 1 && renderPass == 3) {
            this.setRenderPassModel(this.mainModel);
            this.bindTexture(((ISpecialMob)entity).getSpecialData().getTexture(1));
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDepthMask(!entity.isInvisible());
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 61680.0F, 0.0F);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            return 1;
        }
        return -1;
    }
}