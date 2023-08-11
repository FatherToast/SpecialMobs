package fathertoast.specialmobs.client.renderer.entity.species;

import fathertoast.specialmobs.client.renderer.entity.family.SpecialCreeperRenderer;
import fathertoast.specialmobs.common.entity.creeper.EnderCreeperEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

@OnlyIn( Dist.CLIENT )
public class EnderCreeperRenderer extends SpecialCreeperRenderer {
    private static final double VIBE_STR = 0.02;
    
    private final Random random = new Random();
    
    public EnderCreeperRenderer( EntityRendererProvider.Context context ) { super( context ); }
    
    /** {@link net.minecraft.client.renderer.entity.EndermanRenderer#getRenderOffset(EnderMan, float)} */
    @Override
    public Vec3 getRenderOffset(Creeper entity, float partialTicks ) {
        if( ((EnderCreeperEntity) entity).isCreepy() ) {
            return new Vec3( random.nextGaussian() * VIBE_STR,
                    0.0, random.nextGaussian() * VIBE_STR );
        }
        return super.getRenderOffset( entity, partialTicks );
    }
}