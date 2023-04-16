package fathertoast.specialmobs.common.config.util.environment.biome;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.field.EnvironmentListField;
import fathertoast.specialmobs.common.config.util.environment.TagKeyEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import javax.annotation.Nullable;
import java.util.stream.Collectors;

public class BiomeTagKeyEnvironment extends TagKeyEnvironment {
    
    public BiomeTagKeyEnvironment( ResourceLocation value, boolean invert ) { super( value, invert ); }

    public BiomeTagKeyEnvironment( AbstractConfigField field, String line ) { super( field, line ); }

    /** @return The string name of this environment, as it would appear in a config file. */
    @Override
    public String name() { return EnvironmentListField.ENV_BIOME_TAG; }
    
    /** @return Returns true if this environment matches the provided environment. */
    @Override
    public boolean matches( Level world, @Nullable BlockPos pos ) {
        if (pos == null || VALUE == null)
            return false;

        for (TagKey<Biome> tagKey : world.getBiome(pos).tags().collect(Collectors.toList())) {
            if (tagKey.location().equals(VALUE)) {
                return !INVERT;
            }
        }
        return false;
    }
}