package toast.specialMobs.client;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.entity.RenderZombie;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import toast.specialMobs.entity.INinja;
import toast.specialMobs.entity.ISpecialMob;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSpecialZombie extends RenderZombie
{
    public RenderSpecialZombie() {
        super();
    }

    /// Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        if (entity instanceof INinja && ((INinja) entity).getHidingBlock() != null)
			return TextureMap.locationBlocksTexture;

        if (!(entity instanceof EntityPigZombie) && ((EntityZombie)entity).isVillager())
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

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTick) {
        if (entity instanceof INinja) {
        	Block block = ((INinja) entity).getHidingBlock();
        	if (block != null) {
                GL11.glPushMatrix();
                GL11.glTranslatef((float) x, (float) y + 0.5F, (float) z);
                this.bindEntityTexture(entity);
                ClientProxy.blockRenderer.renderBlockAsItem(block, ((INinja) entity).getHidingData(), 1.0F);
                GL11.glPopMatrix();
				return;
        	}
        }
        super.doRender(entity, x, y, z, yaw, partialTick);
    }
}