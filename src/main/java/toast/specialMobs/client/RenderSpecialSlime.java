package toast.specialMobs.client;

import net.minecraft.client.model.ModelSlime;
import net.minecraft.client.renderer.entity.RenderSlime;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import toast.specialMobs.entity.ISpecialMob;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSpecialSlime extends RenderSlime
{
    public RenderSpecialSlime() {
        super(new ModelSlime(16), new ModelSlime(0), 0.25F);
    }

    /// Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return ((ISpecialMob) entity).getSpecialData().getTexture();
    }

    /// Allows the render to do any OpenGL state modifications necessary before the model is rendered.
    @Override
    protected void preRenderCallback(EntityLivingBase entity, float partialTick) {
        super.preRenderCallback(entity, partialTick);
        float scale = ((ISpecialMob) entity).getSpecialData().getRenderScale();
        this.shadowSize = 0.3F * scale;
        GL11.glScalef(scale, scale, scale);
    }
}