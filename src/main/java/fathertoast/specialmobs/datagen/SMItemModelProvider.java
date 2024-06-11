package fathertoast.specialmobs.datagen;

import fathertoast.specialmobs.common.bestiary.MobFamily;
import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.core.register.SMItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.util.Objects;

public class SMItemModelProvider extends ItemModelProvider {
    
    public SMItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper ) {
        super( output, SpecialMobs.MOD_ID, existingFileHelper );
    }
    
    @Override
    protected void registerModels() {
        // Bestiary-generated spawn egg models
        final ResourceLocation spawnEggParent = modLoc( ITEM_FOLDER + "/template_sm_spawn_egg" );
        for( MobFamily.Species<?> species : MobFamily.getAllSpecies() ) {
            withExistingParent( species.spawnEgg.getId().getPath(), spawnEggParent );
        }
        
        // Simple items
        for( RegistryObject<? extends Item> regObject : SMItems.SIMPLE_ITEMS ) {
            final String name = Objects.requireNonNull( regObject.getId() ).getPath();
            
            withExistingParent( name, mcLoc( ITEM_FOLDER + "/generated" ) )
                    .texture( "layer0", modLoc( ITEM_FOLDER + "/" + name ) );
        }
    }
}