package toast.specialMobs.client;

import net.minecraft.client.renderer.entity.RenderGhast;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import toast.specialMobs.entity.ISpecialMob;
import toast.specialMobs.entity.ghast.Entity_SpecialGhast;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSpecialGhast extends RenderGhast
{
    public RenderSpecialGhast() {
        super();
    }

    /// Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        if (((Entity_SpecialGhast)entity).getFireTexture() == 1)
            return ((ISpecialMob)entity).getSpecialData().getTexture(1);
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
}