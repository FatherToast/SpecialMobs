package fathertoast.specialmobs.client.renderer.entity.species;

import fathertoast.specialmobs.client.renderer.entity.family.SpecialSilverfishRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.phys.Vec3;

public class ShortSilverfishRenderer extends SpecialSilverfishRenderer {
    
    private static final float FORWARD_OFFSET = 0.4F;
    
    public ShortSilverfishRenderer( EntityRendererProvider.Context context ) {
        super( context );
    }
    
    @Override
    public Vec3 getRenderOffset( Silverfish entity, float partialTicks ) {
        final float angle = Mth.lerp( partialTicks, entity.yRotO, entity.getYRot() ) * (float) Math.PI / 180.0F;
        final float forwardX = -Mth.sin( angle );
        final float forwardZ = Mth.cos( angle );
        return new Vec3( forwardX * FORWARD_OFFSET, 0.0, forwardZ * FORWARD_OFFSET );
    }
}