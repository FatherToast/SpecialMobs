package fathertoast.specialmobs.client.renderer.entity.projectile;

import fathertoast.specialmobs.common.entity.projectile.BoneShrapnelEntity;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn( Dist.CLIENT )
public class BoneShrapnelRenderer extends ArrowRenderer<BoneShrapnelEntity> {
    private static final ResourceLocation TEXTURE_LOCATION = References.getEntityBaseTexture( "projectile", "bone_shrapnel" );
    
    public BoneShrapnelRenderer( EntityRendererManager rendererManager ) { super( rendererManager ); }
    
    @Override
    public ResourceLocation getTextureLocation( BoneShrapnelEntity entity ) { return TEXTURE_LOCATION; }
}