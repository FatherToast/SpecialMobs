package toast.specialMobs.client;

import net.minecraft.block.Block;
import net.minecraft.client.model.ModelWitch;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderWitch;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import toast.specialMobs.entity.ISpecialMob;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSpecialWitch extends RenderWitch
{
    private final ModelWitch witchModel;

    public RenderSpecialWitch() {
        super();
        // Replace mainModel so that RenderWitch modifies a dummy model instead of the real one
        this.mainModel = this.witchModel = new ModelWitch(0.0F);
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
        this.shadowSize = 0.3F * scale;
        GL11.glScalef(scale, scale, scale);
    }

    @Override
	public void doRender(EntityWitch witch, double x, double y, double z, float time, float partialTick) {
		ItemStack held = witch.getHeldItem();
		// Set rotateNose
		this.witchModel.field_82900_g = held != null && held.getItem() instanceof ItemPotion;
        super.doRender(witch, x, y, z, time, partialTick);
    }

    @Override
	protected void renderEquippedItems(EntityWitch witch, float partialTick) {
        GL11.glColor3f(1.0F, 1.0F, 1.0F);
        ItemStack itemstack = witch.getHeldItem();

        if (itemstack != null) {
            GL11.glPushMatrix();
            float scale;

            if (this.mainModel.isChild) {
            	scale = 0.5F;
                GL11.glTranslatef(0.0F, 0.625F, 0.0F);
                GL11.glRotatef(-20.0F, -1.0F, 0.0F, 0.0F);
                GL11.glScalef(scale, scale, scale);
            }

            if (!this.witchModel.field_82900_g) {
            	this.witchModel.villagerNose.rotateAngleX = -0.9F;
            	this.witchModel.villagerNose.offsetZ = -0.09375F;
            	this.witchModel.villagerNose.offsetY = 0.1875F;
            }
			this.witchModel.villagerNose.postRender(0.0625F);
            GL11.glTranslatef(-0.0625F, 0.53125F, 0.21875F);


            if (itemstack.getItem() instanceof ItemBlock && RenderBlocks.renderItemIn3d(Block.getBlockFromItem(itemstack.getItem()).getRenderType())) {
            	scale = 0.5F;
                GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
                scale *= 0.75F;
                GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
                GL11.glScalef(scale, -scale, scale);
            }
            else if (itemstack.getItem() == Items.bow) {
            	scale = 0.625F;
                GL11.glTranslatef(0.0F, 0.125F, 0.3125F);
                GL11.glRotatef(-20.0F, 0.0F, 1.0F, 0.0F);
                GL11.glScalef(scale, -scale, scale);
                GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            }
            else if (itemstack.getItem().isFull3D()) {
            	scale = 0.625F;

                if (itemstack.getItem().shouldRotateAroundWhenRendering()) {
                    GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
                    GL11.glTranslatef(0.0F, -0.125F, 0.0F);
                }

                this.func_82410_b();
                GL11.glScalef(scale, -scale, scale);
                GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            }
            else {
            	scale = 0.375F;
                GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
                GL11.glScalef(scale, scale, scale);
                GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);
            }

            GL11.glRotatef(-15.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(40.0F, 0.0F, 0.0F, 1.0F);

            int passes = itemstack.getItem().requiresMultipleRenderPasses() ? itemstack.getItem().getRenderPasses(itemstack.getItemDamage()) : 1;
            float r, g, b;
            for (int p = 0; p < passes; p++) {
                int color = itemstack.getItem().getColorFromItemStack(itemstack, p);
                r = (color >> 16 & 255) / 255.0F;
                g = (color >> 8 & 255) / 255.0F;
                b = (color & 255) / 255.0F;
                GL11.glColor4f(r, g, b, 1.0F);
                this.renderManager.itemRenderer.renderItem(witch, itemstack, p);
            }

            GL11.glPopMatrix();
        }
    }
}