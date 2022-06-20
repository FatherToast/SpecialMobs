package fathertoast.specialmobs.datagen;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class SMItemModelProvider extends ItemModelProvider {
    
    public SMItemModelProvider( DataGenerator gen, ExistingFileHelper existingFileHelper ) {
        super( gen, SpecialMobs.MOD_ID, existingFileHelper );
    }
    
    @Override
    protected void registerModels() {
        // Bestiary-generated spawn egg models
        final ResourceLocation spawnEggParent = modLoc( ITEM_FOLDER + "/template_sm_spawn_egg" );
        for( MobFamily.Species<?> species : MobFamily.getAllSpecies() )
            withExistingParent( species.spawnEgg.getId().getPath(), spawnEggParent );
    }
}