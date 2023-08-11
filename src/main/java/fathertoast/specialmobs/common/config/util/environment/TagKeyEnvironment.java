package fathertoast.specialmobs.common.config.util.environment;

import fathertoast.specialmobs.common.config.field.AbstractConfigField;
import fathertoast.specialmobs.common.config.file.TomlHelper;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class TagKeyEnvironment extends AbstractEnvironment {

    /** If true, the condition is inverted. */
    protected final boolean INVERT;
    /** The tag id value for this environment. */
    protected final ResourceLocation[] VALUES;

    public TagKeyEnvironment( ResourceLocation value, boolean invert ) {
        this( new ResourceLocation[]{ value }, invert );
    }

    public TagKeyEnvironment( ResourceLocation[] values, boolean invert ) {
        INVERT = invert;
        VALUES = values;
    }

    public TagKeyEnvironment( AbstractConfigField field, String line ) {
        INVERT = line.startsWith( "!" );
        VALUES = parseValue( field, line, INVERT ? line.substring( 1 ) : line );
    }

    /** @return Attempts to parse the string literal as one of the valid values and returns it, or null if invalid. */
    @Nullable
    private ResourceLocation[] parseValue( AbstractConfigField field, String line, String name ) {
        List<ResourceLocation> ids = new ArrayList<>();
        String[] s = line.split(",");

        for (String string : s) {
            ResourceLocation id = ResourceLocation.tryParse(string);

            if (id != null)
                ids.add(id);
        }
        if (!ids.isEmpty()) {
            return ids.toArray(new ResourceLocation[0]);
        }
        // Value cannot be parsed
        SpecialMobs.LOG.warn( "Invalid entry for {} \"{}\"! Value not defined. Defaulting to {}. Invalid entry: {}",
                field.getClass(), field.getKey(), TomlHelper.toLiteral(), line );
        return null;
    }

    /** @return The string value of this environment, as it would appear in a config file. */
    @Override
    public final String value() { return (INVERT ? "!" : "") + VALUES.toString().toLowerCase( Locale.ROOT ); }
}
