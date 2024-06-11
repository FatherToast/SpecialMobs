package fathertoast.specialmobs.client.renderer.entity.species;

import com.mojang.blaze3d.vertex.PoseStack;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobEyesLayer;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobOverlayLayer;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

public class SpecialZombieVillagerRenderer extends HumanoidMobRenderer<Zombie, ZombieVillagerModel<Zombie>> {
    
    private final float baseShadowRadius;

    public SpecialZombieVillagerRenderer( EntityRendererProvider.Context context ) {
        super( context, new ZombieVillagerModel<>( context.bakeLayer(ModelLayers.ZOMBIE_VILLAGER )), 0.5F );
        addLayer( new HumanoidArmorLayer<>(this, new ZombieVillagerModel<>( context.bakeLayer(ModelLayers.ZOMBIE_VILLAGER_INNER_ARMOR )), new ZombieVillagerModel<>(context.bakeLayer(ModelLayers.ZOMBIE_VILLAGER_OUTER_ARMOR ) ), context.getModelManager() ));
        
        baseShadowRadius = shadowRadius;
        addLayer( new SpecialMobEyesLayer<>( this ) );
        addLayer( new SpecialMobOverlayLayer<>( this, new ZombieVillagerModel<>( context.bakeLayer( ModelLayers.ZOMBIE_VILLAGER ) ) ) );
    }
    
    @Override
    public ResourceLocation getTextureLocation( Zombie entity ) {
        return ((ISpecialMob<?>) entity).getSpecialData().getTexture();
    }
    
    @Override
    protected void scale( Zombie entity, PoseStack poseStack, float partialTick ) {
        super.scale( entity, poseStack, partialTick );
        
        final float scale = ((ISpecialMob<?>) entity).getSpecialData().getRenderScale();
        shadowRadius = baseShadowRadius * scale;
        poseStack.scale( scale, scale, scale );
    }
}