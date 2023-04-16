package fathertoast.specialmobs.common.mixin;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Note: this is a bad mixin tutorial by yours truly, Sarinsa
 *
 * This is our EntityRendererManager mixin!
 *
 * All mixin classes must be annotated with @Mixin(TargetClass.class)
 *
 * Also note that all mixin classes must exist in their own dedicated mixin package. Having a non-mixin class
 * inside the package you have specified to be your mixin package will not go well.
 *
 * Its a good rule of thumb to make your mixin classes abstract, as they should never be instantiated.
 *
 * This mixin example is not fantastic as it only scratches the surface of the worm can that is mixin,
 * but I figured it at least had some relevance since shadow render canceling was a topic earlier.
 *
 * NOTE: All mixins must be registered in the mod's mixin config.
 *       the mixin config can be found inside the mod's resources folder: "specialmobs.mixins.json".
 *       When you remove this mixin from the config, it will no longer be active.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRendererManagerMixin implements ResourceManagerReloadListener {

    /**
     *  Constructor matching target class constructor. Not required in this case, but generally nice to have
     */
    public EntityRendererManagerMixin(Minecraft game, TextureManager textureManager, ItemRenderer itemRenderer, BlockRenderDispatcher brd, Font font, Options options, EntityModelSet modelSet) {

    }

    /**
     * Simple redirect injection.
     *
     * Here we use the @Redirect annotation to make a redirect injection on the target method.
     * This annotation lets us swap out invocations in the target method with our own.
     *
     * The first parameter of annotation is the target method name; the method we will be messing around with
     * The second parameter is the "at" (where in the target method should we look for invocations to swap?)
     *
     * There are various ways to determine where we want to do things, but in this specific example,
     * we want to cancel every call to "renderShadow" in the target method.
     *
     * value = "INVOKE" - This specifies that we should only look for INVOKE opcodes
     * target = "Lnet/minecraft/client..blah..blah" - This is the full name of the member we want to redirect, in this case "renderShadow"
     *
     * As you can see, our injection method has an empty body. This is because we wish to do absolutely nothing instead of rendering entity shadows.
     *
     * NOTE: It is required that our injected method has the same parameters as our target, as shown below.
     *       the "name" of our injection method does not matter. It can be anything, but its not a bad idea to
     *       name it something intuitive.
     */
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;renderShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/Entity;FFLnet/minecraft/world/level/LevelReader;F)V"))
    public void onRender(PoseStack poseStack, MultiBufferSource bufferSource, Entity entity, float p_229096_3_, float p_229096_4_, LevelReader levelReader, float p_229096_6_) {

    }
}
