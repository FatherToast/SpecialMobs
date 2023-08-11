package fathertoast.specialmobs.client.renderer.entity.species;

import fathertoast.specialmobs.client.renderer.entity.family.SpecialSlimeRenderer;
import fathertoast.specialmobs.client.renderer.entity.layers.SpecialMobTintedLayer;
import fathertoast.specialmobs.common.entity.slime.PotionSlimeEntity;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.monster.Slime;

public class PotionSlimeRenderer extends SpecialSlimeRenderer {
    
    public PotionSlimeRenderer( EntityRendererProvider.Context context ) {
        super( context );
        addLayer( new PotionSlimeLayer( this ) );
    }
    
    private static class PotionSlimeLayer extends SpecialMobTintedLayer<Slime, SlimeModel<Slime>> {
        public PotionSlimeLayer( RenderLayerParent<Slime, SlimeModel<Slime>> renderer ) { super( renderer ); }

        @Override
        protected int getColor( Slime entity ) { return ((PotionSlimeEntity) entity).getPotionColor(); }
    }
}