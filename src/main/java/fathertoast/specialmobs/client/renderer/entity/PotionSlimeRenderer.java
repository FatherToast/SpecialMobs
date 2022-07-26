package fathertoast.specialmobs.client.renderer.entity;

import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobTintedLayer;
import fathertoast.specialmobs.common.entity.slime.PotionSlimeEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.SlimeModel;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn( Dist.CLIENT )
public class PotionSlimeRenderer extends SpecialSlimeRenderer {
    
    public PotionSlimeRenderer( EntityRendererManager rendererManager ) {
        super( rendererManager );
        addLayer( new PotionSlimeLayer( this ) );
    }
    
    private static class PotionSlimeLayer extends SpecialMobTintedLayer<SlimeEntity, SlimeModel<SlimeEntity>> {
        public PotionSlimeLayer( IEntityRenderer<SlimeEntity, SlimeModel<SlimeEntity>> renderer ) { super( renderer ); }
        
        @Override
        protected int getColor( SlimeEntity entity ) { return ((PotionSlimeEntity) entity).getPotionColor(); }
    }
}