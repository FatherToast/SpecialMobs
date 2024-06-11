package fathertoast.specialmobs.datagen;

import fathertoast.specialmobs.common.core.SpecialMobs;
import fathertoast.specialmobs.common.core.register.SMDamageTypes;
import fathertoast.specialmobs.common.core.register.SMTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class SMDamageTagProvider extends DamageTypeTagsProvider {

    public SMDamageTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture, @Nullable ExistingFileHelper fileHelper) {
        super(output, completableFuture, SpecialMobs.MOD_ID, fileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag( SMTags.IS_MAGIC ).add( DamageTypes.MAGIC, DamageTypes.INDIRECT_MAGIC );

        this.tag( DamageTypeTags.BYPASSES_ARMOR ).add( SMDamageTypes.GRAB );
        this.tag( DamageTypeTags.BYPASSES_EFFECTS ).add( SMDamageTypes.GRAB );
    }
}
