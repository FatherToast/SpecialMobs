package fathertoast.specialmobs.datagen;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.core.register.SMEntities;
import fathertoast.specialmobs.common.util.AnnotationHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SMEntityTagProvider extends EntityTypeTagsProvider {

    public SMEntityTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture, @Nullable ExistingFileHelper fileHelper) {
        super(output, completableFuture, SpecialMobs.MOD_ID, fileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for( MobFamily.Species<?> species : MobFamily.getAllSpecies( ) ) {
            List<TagKey<EntityType<?>>> tags = AnnotationHelper.getEntityTags( species.entityClass );

            if ( tags != null && !tags.isEmpty() ) {
                for ( TagKey<EntityType<?>> tag : tags ) {
                    tag( tag ).add( species.entityType.get() );
                }
            }
        }

        // Manually added
        tag(EntityTypeTags.IMPACT_PROJECTILES).add(
                SMEntities.BONE_SHRAPNEL.get(),
                SMEntities.BUG_SPIT.get()
        );
    }
}
