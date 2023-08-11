package fathertoast.specialmobs.client.renderer.entity.projectile;

import fathertoast.specialmobs.common.entity.projectile.BoneShrapnelEntity;
import fathertoast.specialmobs.common.util.References;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BoneShrapnelRenderer extends ArrowRenderer<BoneShrapnelEntity> {
    private static final ResourceLocation TEXTURE_LOCATION = References.getEntityBaseTexture( "projectile", "bone_shrapnel" );
    
    public BoneShrapnelRenderer( EntityRendererProvider.Context context ) { super( context ); }
    
    @Override
    public ResourceLocation getTextureLocation( BoneShrapnelEntity entity ) { return TEXTURE_LOCATION; }
}