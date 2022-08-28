package fathertoast.specialmobs.client.renderer.entity.species;

import fathertoast.specialmobs.client.renderer.entity.family.SpecialCreeperRenderer;
import fathertoast.specialmobs.common.entity.creeper.EnderCreeperEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

@OnlyIn( Dist.CLIENT )
public class EnderCreeperRenderer extends SpecialCreeperRenderer {
    private static final double VIBE_STR = 0.02;
    
    private final Random random = new Random();
    
    public EnderCreeperRenderer( EntityRendererManager rendererManager ) { super( rendererManager ); }
    
    /** {@link net.minecraft.client.renderer.entity.EndermanRenderer#getRenderOffset(EndermanEntity, float)} */
    @Override
    public Vector3d getRenderOffset( CreeperEntity entity, float partialTicks ) {
        if( ((EnderCreeperEntity) entity).isCreepy() ) {
            return new Vector3d( random.nextGaussian() * VIBE_STR,
                    0.0, random.nextGaussian() * VIBE_STR );
        }
        return super.getRenderOffset( entity, partialTicks );
    }
}