package fathertoast.specialmobs.client.renderer.entity.species;

import fathertoast.specialmobs.client.renderer.entity.family.SpecialSilverfishRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.monster.SilverfishEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn( Dist.CLIENT )
public class ShortSilverfishRenderer extends SpecialSilverfishRenderer {
    
    private static final float FORWARD_OFFSET = 0.4F;
    
    public ShortSilverfishRenderer( EntityRendererManager rendererManager ) {
        super( rendererManager );
    }
    
    @Override
    public Vector3d getRenderOffset( SilverfishEntity entity, float partialTicks ) {
        final float angle = MathHelper.lerp( partialTicks, entity.yRotO, entity.yRot ) * (float) Math.PI / 180.0F;
        final float forwardX = -MathHelper.sin( angle );
        final float forwardZ = MathHelper.cos( angle );
        return new Vector3d( forwardX * FORWARD_OFFSET, 0.0, forwardZ * FORWARD_OFFSET );
    }
}