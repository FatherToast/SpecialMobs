package fathertoast.specialmobs.client.renderer.entity.species;

import com.mojang.blaze3d.vertex.PoseStack;
import fathertoast.specialmobs.client.renderer.entity.family.SpecialCreeperRenderer;
import fathertoast.specialmobs.common.entity.ISpecialMob;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Creeper;

public class ImplodingCreeperRenderer extends SpecialCreeperRenderer {
    
    public ImplodingCreeperRenderer( EntityRendererProvider.Context context ) { super( context ); }
    
    @Override
    protected void scale( Creeper creeper, PoseStack poseStack, float partialTick ) {
        float swelling = creeper.getSwelling( partialTick );
        float wobble = 1.0F - Mth.sin( swelling * 100.0F ) * swelling * 0.01F;
        
        swelling = Mth.clamp( swelling, 0.0F, 1.0F );
        swelling *= swelling;
        swelling *= swelling;
        
        float xzScale = (1.0F - swelling * 0.4F) * wobble;
        float yScale = (1.0F - swelling * 0.3F) / wobble;
        
        poseStack.scale( xzScale, yScale, xzScale );
        
        final float scale = ((ISpecialMob<?>) creeper).getSpecialData().getRenderScale();
        shadowRadius = getBaseShadowRadius() * scale;
        poseStack.scale( scale, scale, scale );
    }
}
