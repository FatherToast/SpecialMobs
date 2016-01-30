package toast.specialMobs.client;

import net.minecraft.client.renderer.RenderBlocks;
import toast.specialMobs.CommonProxy;
import toast.specialMobs.entity.EntitySpecialFishHook;
import toast.specialMobs.entity.EntitySpecialSpitball;
import toast.specialMobs.entity.blaze.Entity_SpecialBlaze;
import toast.specialMobs.entity.cavespider.Entity_SpecialCaveSpider;
import toast.specialMobs.entity.creeper.Entity_SpecialCreeper;
import toast.specialMobs.entity.enderman.Entity_SpecialEnderman;
import toast.specialMobs.entity.ghast.Entity_SpecialGhast;
import toast.specialMobs.entity.pigzombie.Entity_SpecialPigZombie;
import toast.specialMobs.entity.silverfish.Entity_SpecialSilverfish;
import toast.specialMobs.entity.skeleton.Entity_SpecialSkeleton;
import toast.specialMobs.entity.slime.Entity_SpecialSlime;
import toast.specialMobs.entity.spider.Entity_SpecialSpider;
import toast.specialMobs.entity.zombie.Entity_SpecialZombie;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    // Helps this class render blocks.
	public static final RenderBlocks blockRenderer = new RenderBlocks();

    /// Registers render files if this is the client side.
    @Override
    public void registerRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(Entity_SpecialBlaze.class, new RenderSpecialBlaze());
        RenderingRegistry.registerEntityRenderingHandler(Entity_SpecialCaveSpider.class, new RenderSpecialSpider());
        RenderingRegistry.registerEntityRenderingHandler(Entity_SpecialCreeper.class, new RenderSpecialCreeper());
        RenderingRegistry.registerEntityRenderingHandler(Entity_SpecialEnderman.class, new RenderSpecialEnderman());
        RenderingRegistry.registerEntityRenderingHandler(Entity_SpecialGhast.class, new RenderSpecialGhast());
        RenderingRegistry.registerEntityRenderingHandler(Entity_SpecialPigZombie.class, new RenderSpecialZombie());
        RenderingRegistry.registerEntityRenderingHandler(Entity_SpecialSilverfish.class, new RenderSpecialSilverfish());
        RenderingRegistry.registerEntityRenderingHandler(Entity_SpecialSkeleton.class, new RenderSpecialSkeleton());
        RenderingRegistry.registerEntityRenderingHandler(Entity_SpecialSlime.class, new RenderSpecialSlime());
        RenderingRegistry.registerEntityRenderingHandler(Entity_SpecialSpider.class, new RenderSpecialSpider());
        RenderingRegistry.registerEntityRenderingHandler(Entity_SpecialZombie.class, new RenderSpecialZombie());

        RenderingRegistry.registerEntityRenderingHandler(EntitySpecialFishHook.class, new RenderSpecialFishHook());
        RenderingRegistry.registerEntityRenderingHandler(EntitySpecialSpitball.class, new RenderSpecialSpitball());
    }
}